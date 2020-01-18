package io.github.bensku.skripty.parser.pattern;

import io.github.bensku.skripty.core.SkriptType;
import io.github.bensku.skripty.core.expression.Expression;

public interface PatternPart {

	/**
	 * A literal pattern part that consists of some text.
	 *
	 */
	public static class Literal {
		
		/**
		 * Text that must be matched.
		 */
		private final String text;

		Literal(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}
		
	}
	
	/**
	 * An expression input.
	 *
	 */
	public static class Input {
		
		private final SkriptType[] inputTypes;

		private Input(SkriptType[] inputTypes) {
			this.inputTypes = inputTypes;
		}

		public SkriptType[] getInputTypes() {
			return inputTypes;
		}

	}
}
