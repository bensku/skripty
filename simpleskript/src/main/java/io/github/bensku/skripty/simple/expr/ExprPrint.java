package io.github.bensku.skripty.simple.expr;

import java.time.Instant;

import io.github.bensku.skripty.core.annotation.CallTarget;
import io.github.bensku.skripty.core.annotation.Inputs;
import io.github.bensku.skripty.core.annotation.Returns;
import io.github.bensku.skripty.parser.annotation.Pattern;

@Inputs({"text/timestamp"})
@Returns("void")
@Pattern("print {0}")
public class ExprPrint {

	@CallTarget
	public void print(String text) {
		System.out.println(text);
	}
	
	@CallTarget
	public void printTime(long time) {
		System.out.println(Instant.ofEpochMilli(time));
	}
}
