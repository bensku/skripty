package skriptyio.github.bensku.skripty.core.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import org.junit.jupiter.api.Test;

import io.github.bensku.skripty.core.SkriptType;
import io.github.bensku.skripty.core.expression.CallableExpression;
import io.github.bensku.skripty.core.expression.ConstantExpression;
import io.github.bensku.skripty.core.expression.InputType;

public class ExpressionTest {
	
	private SkriptType type = SkriptType.create(Object.class);
	
	@Test
	public void constantExpr() {
		ConstantExpression expr = new ConstantExpression(type, "test string");
		assertEquals("test string", expr.call());
		assertEquals(0, expr.getInputTypes().length);
		assertEquals(type, expr.getReturnType());
	}
	
	public Object callTargetA() {
		return "hello, world";
	}
	
	public Object callTargetB(String arg) {
		return arg;
	}
	
	@Test
	public void findCallTarget() throws Throwable {
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		MethodHandle targetA = lookup.findVirtual(getClass(), "callTargetA", MethodType.methodType(Object.class));
		MethodHandle targetB = lookup.findVirtual(getClass(), "callTargetB", MethodType.methodType(Object.class, String.class));
		
		CallableExpression expr = new CallableExpression(this, new InputType[] {new InputType(new SkriptType[] {SkriptType.create(String.class)}, true)},
				type, new MethodHandle[] {targetA, targetB});
		assertEquals(1, expr.getInputTypes().length);
		assertEquals(type, expr.getReturnType());
		
		// Test that correct call targets are found
		assertEquals("hello, world", expr.findTarget(new Class[0], true).invokeExact());
		assertEquals("abc", expr.findTarget(new Class[] {String.class}, true).invokeExact("abc"));
		
		// And then that calling executes them, too
		assertEquals("hello, world", expr.call());
		assertEquals("abc", expr.call("abc"));
	}
}
