package io.github.bensku.skripty.parser.pattern;

/**
 * A pattern for e.g. an expression matching.
 *
 */
public class Pattern {

	private final PatternPart[] parts;
	
	public Pattern(PatternPart[] parts) {
		this.parts = parts;
	}
	
	public PatternPart[] getParts() {
		return parts;
	}
}
