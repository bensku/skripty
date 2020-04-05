package io.github.bensku.skripty.parser.script;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Parses script source code into a tree of source nodes. No expressions are
 * parsed here.
 *
 */
public class SectionParser {
	
	public static class IndentationException extends RuntimeException {

		private static final long serialVersionUID = 1L;
		
		private final int line;
		
		private final int start, end;
		
		public IndentationException(int line, int start, int end, String message) {
			super(message);
			this.line = line;
			this.start = start;
			this.end = end;
		}
		
		public int getLine() {
			return line;
		}
		
		public int getStart() {
			return start;
		}
		
		public int getEnd() {
			return end;
		}
		
		@Override
		public String toString() {
			return getClass().getName() + ": line " + line
					+ ", column " + start + "-" + (end - 1) + ": " + getMessage();
		}
	}
	
	/**
	 * Indentation type.
	 *
	 */
	private enum IndentType {
		SPACE("space", "spaces"),
		TAB("tab", "tabs");
		
		private final String singular, plural;
		
		IndentType(String singular, String plural) {
			this.singular = singular;
			this.plural = plural;
		}
		
		public String format(int count) {
			if (count == 0) {
				return "no " + plural;
			} else if (count == 1) {
				return "1 " + singular;
			} else {
				return count + " " + plural;
			}
		}
	}
	
	/**
	 * Line of source code.
	 *
	 */
	private static class Line {
		
		/**
		 * Line number of this line.
		 */
		public final int lineNumber;
		
		/**
		 * Indentation type.
		 */
		public final IndentType indentType;
		
		/**
		 * How much indentation is used.
		 */
		public final int indentLevel;
		
		/**
		 * Code part of the line, i.e. everything before the comment with
		 * leading and trailing whitespace (including indentation) trimmed.
		 */
		public final String code;
		
		/**
		 * Comment at end of this line. May be empty string if there is no
		 * comment.
		 */
		public final String comment;

		public Line(int lineNumber, IndentType indentType, int indentLevel, String code, String comment) {
			this.lineNumber = lineNumber;
			this.indentType = indentType;
			this.indentLevel = indentLevel;
			this.code = code;
			this.comment = comment;
		}
	}
	
	private IndentType indentType(int codePoint) {
		return codePoint == '\t' ? IndentType.TAB : IndentType.SPACE;
	}
	
	private int countIndentation(String line, int lineNumber) {
		int type = -1;
		for (int i = 0; i < line.length();) {
			int c = line.codePointAt(i);
			
			if (c != ' ' && c != '\t') { // End of whitespace
				return i;
			} else if (type == -1) { // First whitespace
				type = c;
			} else if (c != type) { // Mixed whitespace not allowed
				throw new IndentationException(lineNumber, i - 1, i + 1, "mixed whitespace in indentation; "
						+ "found " + indentType(c) + ", but " + indentType(type) + " is used before");
			}
			
			i += Character.charCount(c);
		}
		return line.length(); // Everything is whitespace
	}
	
	private int findComment(String line, int start) {
		boolean escape = false;
		boolean textBlock = false;
		for (int i = start; i < line.length();) {
			int c = line.codePointAt(i);
			
			if (escape) { // Previous character escaped this
				escape = false;
			} else if (c == '\\') { // We escape the next character
				escape = true;
			} else if (c == '"') { // Text block start/end (not escaped)
				textBlock = !textBlock;
			} else if (!textBlock && c == '#') { // Comment start
				return i;
			}
			
			i += Character.charCount(c);
		}
		
		return line.length(); // No comment in this line
	}
	
	/**
	 * Splits given source text into lines.
	 * @param source Source text.
	 * @return A stream of source {@link Line lines}.
	 */
	private Stream<Line> split(String source) {
		AtomicInteger counter = new AtomicInteger();
		return source.lines().map(line -> {
			int lineNumber = counter.incrementAndGet();
			if (line.isEmpty()) {
				return new Line(lineNumber, IndentType.SPACE, 0, "", "");
			}
			
			// Figure out indentation
			int indentLevel = countIndentation(line, lineNumber);
			IndentType indentType = indentType(line.codePointAt(0));
			
			// Separate code and potential comment
			int commentStart = findComment(line, indentLevel);
			String code = line.substring(indentLevel, commentStart).stripTrailing();
			String comment = commentStart != line.length() ? line.substring(commentStart + 1) : "";
			
			return new Line(lineNumber, indentType, indentLevel, code, comment);
		});
	}
	
	/**
	 * Takes and parses the next line from queue. If it is title of a section,
	 * section contents are recursively parsed.
	 * @param lines Queue of lines.
	 * @return A source node representing the next line.
	 */
	private SourceNode parseNextLine(Queue<Line> lines) {
		Line line = lines.poll();
		if (line.code.endsWith(":")) {
			return parseSection(line, lines);
		} else {
			return new SourceNode.Statement(line.lineNumber, line.code, line.comment);
		}
	}
	
	/**
	 * Parses a section.
	 * @param title Line with section title.
	 * @param lines Queue of lines, starting immediately after section title.
	 * @return A source node representing the section.
	 */
	private SourceNode.Section parseSection(Line title, Queue<Line> lines) {
		List<SourceNode> nodes = new ArrayList<>();
		
		// Indentation level of title; when there is no title, it is just one level lower
		int titleLevel = title != null ? title.indentLevel : -1;
		int expectedIndent = -1;
		while (!lines.isEmpty()) {
			Line next = lines.peek();
			
			if (title != null && titleLevel != 0 && next.indentLevel != 0 && title.indentType != next.indentType) {
				// We can't safely compare indentation levels because different whitespace are used
				throw new IndentationException(next.lineNumber, 0, next.indentLevel,
						"section title (at line " + title.lineNumber + ") uses " + title.indentType.plural + ", "
						+ "but found " + next.indentType.format(next.indentLevel) + " instead");
			} else if (expectedIndent == -1) { // First node in this section sets indentation
				if (next.indentLevel > titleLevel) { // It must be more than title, though
					expectedIndent = next.indentLevel;
				} else { // Section has no nodes, which is not allowed
					throw new IndentationException(title.lineNumber, 0, 0, "empty sections are not allowed");
				}
			} else if (next.indentLevel <= titleLevel) { // End of this section, and maybe even parent sections
				break;
			} else if (next.indentLevel != expectedIndent) { // Wrong indentation level
				throw new IndentationException(next.lineNumber, 0, next.indentLevel,
						"expected " + title.indentType.format(expectedIndent) + ", but found "
						+ title.indentType.format(next.indentLevel) + " instead");
			}
			
			// Looks like nothing is wrong with this line (see above for what could go wrong)
			nodes.add(parseNextLine(lines));
		}
		
		SourceNode.Statement titleNode = null;
		if (title != null) {
			titleNode = new SourceNode.Statement(title.lineNumber, title.code.substring(title.code.length() - 2), title.comment);
		}
		return new SourceNode.Section(titleNode, nodes.toArray(new SourceNode[0]));
	}
	
	/**
	 * Parses a blob of source code into a section node.
	 * @param source Source code.
	 * @return Section node.
	 */
	public SourceNode.Section parse(String source) {
		Stream<Line> lines = split(source).filter(line -> !line.code.isEmpty());
		return parseSection(null, lines.collect(Collectors.toCollection(ArrayDeque::new)));
	}
	
}
