package io.github.bensku.skripty.parser;

import io.github.bensku.skripty.core.SkriptType;

/**
 * Literal parsers are used for syntaxes that cannot be parsed as expressions.
 * Common examples of these would be strings and numbers.
 *
 */
@FunctionalInterface
public interface LiteralParser {
	
	class Result {
		
		private final SkriptType type;
		
		private final Object value;
		
		/**
		 * End index (exclusive) of the parsing operation in the input array.
		 */
		private final int end;
		
		protected Result(SkriptType type, Object value, int end) {
			this.type = type;
			this.value = value;
			this.end = end;
		}
	
		public SkriptType getType() {
			return type;
		}

		public Object getValue() {
			return value;
		}

		public int getEnd() {
			return end;
		}

	}

	Result parse(byte[] input, int start);
}