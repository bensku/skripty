package io.github.bensku.skripty.parser.pattern;

import java.nio.charset.StandardCharsets;

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
		 * Index that this input gets mapped to.
		 */
		private final int index;

		Input(int index) {
			this.index = index;
		}
		
		public int getSlot() {
			return index;
		}

	}
}
