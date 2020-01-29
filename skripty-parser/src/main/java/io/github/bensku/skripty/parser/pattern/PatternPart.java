package io.github.bensku.skripty.parser.pattern;

import java.nio.charset.StandardCharsets;

import io.github.bensku.skripty.core.SkriptType;

/**
 * Part of {@link Pattern}
 *
 */
public interface PatternPart {

	/**
	 * A literal pattern part.
	 *
	 */
	class Literal implements PatternPart {
		
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
	 * An input slot.
	 *
	 */
	class Input implements PatternPart {
		
		/**
		 * Types accepted by this input slot.
		 */
		private final SkriptType[] types;
		
		/**
		 * Index of this input.
		 */
		private final int index;

		Input(SkriptType[] inputTypes, int index) {
			this.types = inputTypes;
			this.index = index;
		}

		public SkriptType[] getTypes() {
			return types;
		}
		
		public int getIndex() {
			return index;
		}

	}
}
