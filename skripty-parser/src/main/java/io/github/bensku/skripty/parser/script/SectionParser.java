package io.github.bensku.skripty.parser.script;

/**
 * Parses script source code into a tree of source nodes. No expressions are
 * parsed here.
 *
 */
public class SectionParser {
	
	/**
	 * Estimated size of section node.
	 */
	private static final int SECTION_NODE_SIZE = 16;
	
	public SourceNode.Section parse(String source) {
		return parse(source, 0, null);
	}
	
	private SourceNode.Section parse(String source, int startIndex, SourceNode.Statement title) {
		int sectionIndent = -1;
		
		SourceNode[] nodes = new SourceNode[SECTION_NODE_SIZE];
		int nodeCount = 0;
		
		// Parse source line-by-line
		int start, end; // We need start to calculate section length
		for (start = startIndex, end = source.indexOf('\n', start);;
				start = end + 1, end = source.indexOf('\n', start)) {
			if (start > source.length()) {
				break; // Running out of source to parse
			} else if (end == -1) {
				end = source.length();
			}
			
			// Count indentation and leave leading whitespace out
			int nodeStart = end - 1; // By default, expect all whitespace
			if (nodeStart == -1) {
				break; // Might happen with empty input
			}
			int indentation = 0;
			for (int i = start; i < end;) {
				int c = source.codePointAt(i);
				if (!Character.isWhitespace(c)) {
					nodeStart = i;
					break;
				}
				indentation++;
				
				i += Character.charCount(c);
			}
			
			// Leave trailing whitespace out
			int nodeEnd = end;
			for (int i = nodeStart; i < end;) {
				int c = source.codePointAt(i);
				if (!Character.isWhitespace(c)) {
					nodeEnd = i + 1;
				}
				
				i += Character.charCount(c);
			}
			
			// Filter our empty nodes
			if (nodeStart + 1 >= nodeEnd) {
				continue;
			}
			
			if (sectionIndent == -1) { // First line of section determines correct indentation
				// TODO disallow indentation that is less than or equal to parent's
				sectionIndent = indentation;
			} else if (indentation < sectionIndent) { // End section
				break;
			}
			
			// Parse this node
			String line = source.substring(nodeStart, nodeEnd);
			SourceNode node;
			if (line.endsWith(":")) { // New section
				node = parse(source, end, new SourceNode.Statement(line.substring(0, line.length() - 1)));
				end = end + node.length() - 1; // Skip over it here
			} else { // Statement in current section
				node = new SourceNode.Statement(line);
			}
			
			if (nodeCount == nodes.length) {
				SourceNode[] array = new SourceNode[nodes.length + SECTION_NODE_SIZE];
				System.arraycopy(nodes, 0, array, 0, nodes.length);
				nodes = array;
			}
			nodes[nodeCount++] = node;
		}
		
		// Return section node with no null subnodes
		SourceNode[] results = new SourceNode[nodeCount];
		System.arraycopy(nodes, 0, results, 0, nodeCount);
		
		return new SourceNode.Section(title, results, start - startIndex);
	}
}
