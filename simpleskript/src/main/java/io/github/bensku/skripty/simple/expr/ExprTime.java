package io.github.bensku.skripty.simple.expr;

import io.github.bensku.skripty.core.annotation.CallTarget;
import io.github.bensku.skripty.core.annotation.Inputs;
import io.github.bensku.skripty.core.annotation.Returns;
import io.github.bensku.skripty.parser.annotation.Pattern;

@Inputs({})
@Returns("timestamp")
@Pattern("current time")
public class ExprTime {

	@CallTarget
	public long getNow() {
		return System.currentTimeMillis();
	}
}
