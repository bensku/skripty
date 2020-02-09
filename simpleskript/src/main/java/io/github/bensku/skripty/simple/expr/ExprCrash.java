package io.github.bensku.skripty.simple.expr;

import io.github.bensku.skripty.core.annotation.CallTarget;
import io.github.bensku.skripty.core.annotation.Inputs;
import io.github.bensku.skripty.core.annotation.Returns;
import io.github.bensku.skripty.parser.annotation.Pattern;

@Inputs({})
@Returns("void")
@Pattern("crash")
public class ExprCrash {

	@CallTarget
	public void crash() {
		throw new RuntimeException("script asked us to crash");
	}
}
