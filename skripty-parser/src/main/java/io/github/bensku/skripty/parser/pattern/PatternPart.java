package io.github.bensku.skripty.parser.pattern;

import java.nio.charset.StandardCharsets;

import io.github.bensku.skripty.core.SkriptType;

public interface PatternPart {

	/**
	 * A literal pattern part that consists of some text.
	 *
	 */
	public static class Literal {
		
		/**
		 * UTF-8 bytes of text that this literal part matches.
		 */
		private final byte[] text;

		Literal(String text) {
			this.text = text.getBytes(StandardCharsets.UTF_8);
		}

		public byte[] getText() {
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
