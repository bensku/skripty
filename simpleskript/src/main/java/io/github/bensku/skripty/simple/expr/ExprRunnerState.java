package io.github.bensku.skripty.simple.expr;

import io.github.bensku.skripty.core.RunnerState;
import io.github.bensku.skripty.core.annotation.CallTarget;
import io.github.bensku.skripty.core.annotation.Inputs;
import io.github.bensku.skripty.core.annotation.Returns;
import io.github.bensku.skripty.parser.annotation.Pattern;

@Inputs({})
@Returns("text") // Maybe make a separate type for it later
@Pattern("runner state")
public class ExprRunnerState {

	@CallTarget
	public String get(RunnerState state) {
		return "" + state;
	}
}
