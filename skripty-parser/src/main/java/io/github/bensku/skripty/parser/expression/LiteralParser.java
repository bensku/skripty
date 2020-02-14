package io.github.bensku.skripty.parser.expression;

import io.github.bensku.skripty.core.AstNode;
import io.github.bensku.skripty.core.SkriptType;

/**
 * Literal parsers are used for syntaxes that cannot be parsed as expressions.
 * Common examples of these would be strings and numbers.
 *
 */
@FunctionalInterface
public interface LiteralParser {
	
	class Result {
		
		/**
		 * Resulting node.
		 */
		private final AstNode node;
		
		/**
		 * End index (exclusive) of the parsing operation in the input array.
		 */
		private final int end;
		
		public Result(AstNode node, int end) {
			this.node = node;
			this.end = end;
		}

		public AstNode getNode() {
			return node;
		}

		public int getEnd() {
			return end;
		}

	}

	Result parse(byte[] input, int start);
}