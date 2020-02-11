package io.github.bensku.skripty.simple.expr;

import io.github.bensku.skripty.core.annotation.CallTarget;
import io.github.bensku.skripty.core.annotation.Inputs;
import io.github.bensku.skripty.core.annotation.Returns;
import io.github.bensku.skripty.parser.annotation.Pattern;

@Inputs({"text/timestamp/boolean"})
@Returns("void")
@Pattern("print {0}")
public class ExprPrint {

	@CallTarget
	public void print(String str) {
		System.out.println(str);
	}
	
	@CallTarget
	public void print(long l) {
		System.out.println(l);
	}
	
	@CallTarget
	public void print(boolean b) {
		System.out.println(b);
	}
}