package io.github.bensku.skripty.core.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import org.junit.jupiter.api.Test;

import io.github.bensku.skripty.core.SkriptType;
import io.github.bensku.skripty.core.expression.CallableExpression;
import io.github.bensku.skripty.core.expression.ConstantExpression;
import io.github.bensku.skripty.core.expression.ExpressionRegistry;
import io.github.bensku.skripty.core.expression.InputType;

public class ExpressionTest {
	
	private SkriptType type = SkriptType.create(Object.class);
	private ExpressionRegistry registry = new ExpressionRegistry();
	
	@Test
	public void constantExpr() {
		ConstantExpression expr = registry.makeConstant(type, "test string");
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
		
		CallableExpression expr = registry.makeCallable(this)
				.inputTypes(new InputType(true, SkriptType.create(String.class)))
				.returnType(type)
				.callTargets(targetA, targetB)
				.create();
		assertEquals(1, expr.getInputTypes().length);
		assertEquals(type, expr.getReturnType());
		
		// Test that correct call targets are found
		assertEquals("hello, world", expr.findTarget(new Class[0], true).invokeExact());
		assertEquals("abc", expr.findTarget(new Class[] {String.class}, true).invokeExact("abc"));
		
		// And then that calling executes them, too
		assertEquals("hello, world", expr.call());
		assertEquals("abc", expr.call("abc"));
	}
	
	@Test
	public void builderErrors() throws NoSuchMethodException, IllegalAccessException {
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		MethodHandle targetA = lookup.findVirtual(getClass(), "callTargetA", MethodType.methodType(Object.class));
		MethodHandle targetB = lookup.findVirtual(getClass(), "callTargetB", MethodType.methodType(Object.class, String.class));
		
		assertThrows(IllegalStateException.class, () -> registry.makeCallable(this).callTargets());
		assertThrows(IllegalArgumentException.class, () -> registry.makeCallable(this)
				.inputTypes(new InputType(true, type), new InputType(false, type)));
		assertThrows(IllegalArgumentException.class, () -> registry.makeCallable(this)
				.inputTypes(new InputType(true, SkriptType.create(int.class)))
				.returnType(SkriptType.VOID)
				.callTargets(targetA, targetB));
	}
}
