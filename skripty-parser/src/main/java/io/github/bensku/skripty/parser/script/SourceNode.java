package io.github.bensku.skripty.parser.script;

import java.util.Iterator;

/**
 * Represents a part of source code, i.e. a section or a statement.
 *
 */
public interface SourceNode {
	
	/**
	 * Gets full length of this in source code in characters. Whitespace is
	 * included in this length.
	 * @return Node length.
	 */
	int length();
	
	class Statement implements SourceNode {
		
		/**
		 * Text content of the statement.
		 */
		private final String text;
		
		public Statement(String text) {
			this.text = text;
		}
		
		public String getText() {
			return text;
		}

		@Override
		public int length() {
			return text.length();
		}
	}
	
	class Section implements SourceNode, Iterable<SourceNode> {
		
		/**
		 * First line of this section. May be null, e.g. for root nodes.
		 */
		private final Statement title;
		
		/**
		 * Sub-nodes of this section node.
		 */
		private final SourceNode[] nodes;
		
		/**
		 * Total length of this section node in characters.
		 */
		private final int length;
		
		public Section(Statement title, SourceNode[] nodes, int length) {
			this.title = title;
			this.nodes = nodes;
			this.length = length;
		}
		
		public Statement getTitle() {
			return title;
		}
		
		public SourceNode[] getNodes() {
			return nodes;
		}

		@Override
		public Iterator<SourceNode> iterator() {
			return new Iterator<SourceNode>() {

				private int index = 0;
				
				@Override
				public boolean hasNext() {
					return index < nodes.length;
				}

				@Override
				public SourceNode next() {
					return nodes[index++];
				}
			};
		}

		@Override
		public int length() {
			return length;
		}
	}

}
