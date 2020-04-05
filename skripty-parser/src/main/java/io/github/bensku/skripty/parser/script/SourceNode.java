package io.github.bensku.skripty.parser.script;

import java.util.Iterator;

/**
 * Represents a part of source code, i.e. a section or a statement.
 *
 */
public interface SourceNode {
	
	class Statement implements SourceNode {
		
		/**
		 * Line in source code that this statement is defined in.
		 */
		private final int line;
		
		/**
		 * Text content of the statement.
		 */
		private final String text;
		
		/**
		 * Comment text at end of this line.
		 */
		private final String comment;
		
		public Statement(int line, String text, String comment) {
			this.line = line;
			this.text = text;
			this.comment = comment;
		}
		
		public int getLine() {
			return line;
		}
		
		public String getText() {
			return text;
		}
		
		public String getComment() {
			return comment;
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
		
		public Section(Statement title, SourceNode[] nodes) {
			this.title = title;
			this.nodes = nodes;
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
	}

}
