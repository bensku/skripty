package io.github.bensku.skripty.simple.expr;

import java.util.Objects;

import io.github.bensku.skripty.core.annotation.CallTarget;
import io.github.bensku.skripty.core.annotation.Inputs;
import io.github.bensku.skripty.core.annotation.Returns;
import io.github.bensku.skripty.parser.annotation.Pattern;

@Inputs({"text/timestamp/variable", "text/timestamp/variable"})
@Returns("boolean")
@Pattern("{0} is {1}")
public class ExprEquals {

	@CallTarget
	public boolean compare(String a, String b) {
		return Objects.equals(a, b);
	}
	
	@CallTarget
	public boolean compare(int a, int b) {
		return a == b;
	}
	
	@CallTarget
	public boolean compare(long a, long b) {
		return a == b;
	}
}
