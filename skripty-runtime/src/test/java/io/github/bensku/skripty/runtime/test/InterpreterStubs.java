package io.github.bensku.skripty.runtime.test;

import io.github.bensku.skripty.core.RunnerState;
import io.github.bensku.skripty.core.flow.ScopeEntry;

public class InterpreterStubs {

	public void nothing() {}
	
	public void withState(RunnerState state) {}
	
	public String concat(String a, String b) {
		return a + b;
	}
	
	public String concat(RunnerState state, String a, String b) {
		return a + b;
	}
	
	public ScopeEntry enterScope(String when) {
		return ScopeEntry.valueOf(when);
	}
}
