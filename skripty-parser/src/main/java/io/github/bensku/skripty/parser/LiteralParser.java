package io.github.bensku.skripty.parser;

import io.github.bensku.skripty.core.SkriptType;

/**
 * Literal parsers are used for syntaxes that cannot be parsed as expressions.
 * Common examples of these would be strings and numbers.
 *
 */
@FunctionalInterface
public interface LiteralParser {
	
	public static class Result {
		
		private final SkriptType type;
		
		private final Object value;
		
		/**
		 * How many bytes were parsed.
		 */
		private final int bytesConsumed;
		
		protected Result(SkriptType type, Object value, int bytesConsumed) {
			this.type = type;
			this.value = value;
			this.bytesConsumed = bytesConsumed;
		}
	
		public SkriptType getType() {
			return type;
		}

		public Object getValue() {
			return value;
		}

		public int getBytesConsumed() {
			return bytesConsumed;
		}

	}

	Result parse(byte[] input);
}