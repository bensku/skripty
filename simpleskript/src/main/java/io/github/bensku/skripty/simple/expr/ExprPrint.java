package io.github.bensku.skripty.simple.expr;

import java.time.Instant;

import io.github.bensku.skripty.core.annotation.CallTarget;
import io.github.bensku.skripty.core.annotation.Inputs;
import io.github.bensku.skripty.core.annotation.Returns;
import io.github.bensku.skripty.core.annotation.Type;
import io.github.bensku.skripty.parser.annotation.Pattern;

@Inputs({"text/timestamp/boolean"})
@Returns("void")
@Pattern("print {0}")
public class ExprPrint {

	@CallTarget
	public void printString(String str) {
		System.out.println(str);
	}
	
	@CallTarget
	public void printNumber(long l) {
		System.out.println(l);
	}
	
	@CallTarget
	public void printTime(@Type("timestamp") long millis) {
		System.out.println(Instant.ofEpochMilli(millis));
	}
	
	@CallTarget
	public void printBoolean(boolean b) {
		System.out.println(b);
	}
}
