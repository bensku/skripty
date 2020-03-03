package io.github.bensku.skripty.simple.state;

import io.github.bensku.skripty.parser.expression.ParserState;

public class SimpleParserState implements ParserState {

	public static final int MAX_VARIABLES = 64;
	
	/**
	 * Variable names. Who needs hashtables, O(n) is fine :)
	 */
	private final String[] variableNames;
	
	public SimpleParserState() {
		this.variableNames = new String[MAX_VARIABLES];
	}
	
	public int getVariableSlot(String name) {
		for (int i = 0; i < variableNames.length; i++) {
			String n = variableNames[i];
			if (n == null) { // Assign new variable here
				variableNames[i] = name;
				return i;
			} else if (name.equals(n)) { // Use existing variable
				return i;
			}
		}
		throw new AssertionError("out of variables, error not yet handled");
	}
}
