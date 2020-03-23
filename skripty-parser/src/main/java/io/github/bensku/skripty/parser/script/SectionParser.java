package io.github.bensku.skripty.parser.script;

import java.util.List;
import java.util.stream.Stream;

/**
 * Parses script source code into a tree of source nodes. No expressions are
 * parsed here.
 *
 */
public class SectionParser {
	
	/**
	 * Indentation type.
	 *
	 */
	private enum IndentType {
		SPACE,
		TAB
	}
	
	/**
	 * Line of source code.
	 *
	 */
	private static class Line {
		
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

		public Line(IndentType indentType, int indentLevel, String code, String comment) {
			this.indentType = indentType;
			this.indentLevel = indentLevel;
			this.code = code;
			this.comment = comment;
		}
	}
	
	private int countIndentation(String line) {
		int type = -1;
		for (int i = 0; i < line.length();) {
			int c = line.codePointAt(i);
			
			if (c != ' ' && c != '\t') { // End of whitespace
				return i;
			} else if (type == -1) { // First whitespace
				type = c;
			} else if (c != type) { // Mixed whitespace not allowed
				throw new IllegalArgumentException("mixed whitespace at index " + i);
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
		return source.lines().map(line -> {
			// Figure out indentation
			int indentLevel = countIndentation(line);
			IndentType indentType = IndentType.SPACE;
			if (indentLevel > 0 && line.codePointAt(0) == '\t') {
				indentType = IndentType.TAB;
			}
			
			// Separate code and potential comment
			int commentStart = findComment(line, indentLevel);
			String code = line.substring(indentLevel, commentStart).stripTrailing();
			String comment = line.substring(commentStart);
			
			return new Line(indentType, indentLevel, code, comment);
		});
	}
	
}
