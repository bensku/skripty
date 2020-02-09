package io.github.bensku.skripty.core.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.github.bensku.skripty.core.SkriptType;
import io.github.bensku.skripty.core.annotation.CallTarget;
import io.github.bensku.skripty.core.annotation.Inputs;
import io.github.bensku.skripty.core.annotation.Returns;
import io.github.bensku.skripty.core.expression.CallableExpression;
import io.github.bensku.skripty.core.expression.ExpressionRegistry;
import io.github.bensku.skripty.core.expression.InputType;

public class ExpressionRegistryTest {
	
	// Two different types backed by same Java class
	public static final SkriptType VOID = SkriptType.VOID;
	public static final SkriptType FOO = SkriptType.create(String.class);
	public static final SkriptType BAR = SkriptType.create(String.class);

	private ExpressionRegistry registry = new ExpressionRegistry();
	
	@Inputs({"foo", "foo/bar?"})
	@Returns("foo")
	public static class TestExpr {
		
		@CallTarget
		public String first(String a, String b) {
			return a + b;
		}
		
		@CallTarget
		public String first(String a) {
			return "foo" + a;
		}
	}
	
	@Test
	public void annotatedCallable() {
		CallableExpression expr = registry.makeCallable(getClass(), new TestExpr());
		InputType[] inputs = expr.getInputTypes();
		assertEquals(FOO, inputs[0].getTypes()[0]);
		assertEquals(FOO, inputs[1].getTypes()[0]);
		assertEquals(BAR, inputs[1].getTypes()[1]);
		assertEquals(FOO, expr.getReturnType());
		assertEquals("alphabeta", expr.call("alpha", "beta"));
		assertEquals("fooalpha", expr.call("alpha"));
	}
}
