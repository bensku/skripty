package io.github.bensku.skripty.parser.pattern;

import io.github.bensku.skripty.core.expression.InputType;

/**
 * An pattern that describes syntax of an expression.
 *
 */
public class Pattern {
	
	private static final int BASE_10 = 10;
	
	/**
	 * Parses a pattern from a string.
	 * @param pattern Pattern string.
	 * @return A pattern.
	 * @throws IllegalArgumentException When the string is not properly
	 * formatted, or would cause an invalid pattern to be created.
	 */
	public static Pattern parse(String pattern) {
		// Count inputs and literals
		int inputs = 0;
		int literals = 0;
		int i = 0;
		while (true) {
			int inputBegin = pattern.indexOf('{', i);
			if (inputBegin == -1) {
				break;
			} else if (i < inputBegin) {
				literals++;
			}
			int inputEnd = pattern.indexOf('}', inputBegin);
			if (inputEnd == -1) {
				throw new IllegalArgumentException("input at " + inputBegin + " was not closed");
			}
			
			inputs++;
			i = inputEnd + 1;
		}
		if (i != pattern.length()) {
			literals++;
		}
		
		// Create parts of them
		Object[] parts = new Object[inputs + literals];
		int partCount = 0;
		i = 0;
		while (true) {
			int inputBegin = pattern.indexOf('{', i);
			if (inputBegin == -1) {
				break;
			} else if (i < inputBegin) {
				parts[partCount++] = pattern.substring(i, inputBegin);
			}
			int inputEnd = pattern.indexOf('}', inputBegin);
			parts[partCount++] = Integer.parseInt(pattern, inputBegin + 1, inputEnd, BASE_10);
			i = inputEnd + 1;
		}
		if (i != pattern.length()) {
			parts[partCount++] = pattern.substring(i);
		}
		
		return Pattern.create(parts);
	}
	
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
		for (int i = 0; i < parts.length; i++) {
			Object p = parts[i];
			if (p instanceof String) {
				if (i != 0 && parts[i - 1] instanceof String) {
					throw new IllegalArgumentException("consecutive string parts");
				}
				doneParts[i] = new PatternPart.Literal((String) p);
			} else if (p instanceof Integer) {
				if (i == 1 && !(parts[i - 1] instanceof String)) {
					throw new IllegalArgumentException("first and second both inputs");
				}
				doneParts[i] = new PatternPart.Input((int) p);
			} else {
				throw new IllegalArgumentException("parts must be strings or input indices");
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
