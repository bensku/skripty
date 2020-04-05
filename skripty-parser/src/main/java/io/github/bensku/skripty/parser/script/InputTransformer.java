package io.github.bensku.skripty.parser.script;

/**
 * Transforms input after section parser has removed the indentation, but
 * before anything else is done.
 *
 */
public interface InputTransformer {
	
	/**
	 * Creates input transformers.
	 *
	 */
	@FunctionalInterface
	interface Factory {
		
		/**
		 * Makes a new input transformer.
		 * @param line Original line string.
		 * @return An input transformer.
		 */
		InputTransformer make(String line);
	}

	/**
	 * State shared with the parser.
	 *
	 */
	class State {
		
		private boolean comment;
		
		public State() {
			this.comment = false;
		}
		
		/**
		 * Tells the parser that rest of the line is a comment.
		 */
		public void beginComment() {
			comment = true;
		}
		
		/**
		 * Checks if the line comment has started.
		 * @return Whether or not the line comment has started.
		 */
		public boolean commentStarted() {
			return comment;
		}
	}
	
	/**
	 * Processes next character.
	 * @param i Index in the string.
	 * @param c Current Unicode code point (character).
	 * @param state Shared state supplied by the parser.
	 * @return Transformed character.
	 */
	int next(int i, int c, State state);
}
