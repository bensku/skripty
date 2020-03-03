package io.github.bensku.skripty.simple.state;

import io.github.bensku.skripty.core.RunnerState;

public class SimpleRunnerState implements RunnerState {

	private final Object[] variables;
	
	public SimpleRunnerState() {
		this.variables = new Object[SimpleParserState.MAX_VARIABLES];
	}
	
	public Object getVariable(int slot) {
		return variables[slot];
	}
	
	public void setVariable(int slot, Object value) {
		variables[slot] = value;
	}
}
