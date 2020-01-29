package io.github.bensku.skripty.parser.pattern;

import io.github.bensku.skripty.core.expression.InputType;

/**
 * An pattern that describes syntax of an expression.
 *
 */
public class Pattern {
	
	/**
	 * Creates a new pattern from {@link String literal parts} and
	 * {@link InputType inputs}.
	 * 
	 * <p>Either first or second part must be literals. Consecutive literal
	 * parts are not allowed; callers should concatenate strings as needed to
	 * avoid them.
	 * @param parts Pattern parts.
	 * @return A new pattern.
	 * @throws IllegalArgumentException When invalid parts are provided.
	 */
	public static Pattern create(Object... parts) {
		PatternPart[] doneParts = new PatternPart[parts.length];
		int inputIndex = 0;
		for (int i = 0; i < parts.length; i++) {
			Object p = parts[i];
			if (p instanceof String) {
				if (i != 0 && parts[i - 1] instanceof String) {
					throw new IllegalArgumentException("consecutive string parts");
				}
				doneParts[i] = new PatternPart.Literal((String) p);
			} else if (p instanceof InputType) {
				if (i == 1 && !(parts[i - 1] instanceof String)) {
					throw new IllegalArgumentException("first and second both inputs");
				}
				doneParts[i] = new PatternPart.Input(((InputType) p).getTypes(), inputIndex++);
			}
		}
		
		return new Pattern(doneParts);
	}

	/**
	 * Pattern parts.
	 */
	private final PatternPart[] parts;
	
	private Pattern(PatternPart[] parts) {
		this.parts = parts;
	}
	
	public int length() {
		return parts.length;
	}
	
	/**
	 * Gets a pattern part.
	 * @param index Index of the part.
	 * @return The part.
	 */
	public PatternPart partAt(int index) {
		return parts[index];
	}
}
