package io.github.bensku.skripty.simple.expr;

import io.github.bensku.skripty.core.annotation.CallTarget;
import io.github.bensku.skripty.core.annotation.Inputs;
import io.github.bensku.skripty.core.annotation.Returns;
import io.github.bensku.skripty.parser.annotation.Pattern;
import io.github.bensku.skripty.simple.state.SimpleRunnerState;

@Inputs({"variable", "text/variable"})
@Returns("void")
@Pattern("set {0} to {1}")
public class ExprSetVariable {

	@CallTarget
	public void set(SimpleRunnerState state, int slot, String value) {
		state.setVariable(slot, value);
	}
	
	@CallTarget
	public void set(SimpleRunnerState state, int slot, int fromSlot) {
		state.setVariable(slot, state.getVariable(fromSlot));
	}
}
