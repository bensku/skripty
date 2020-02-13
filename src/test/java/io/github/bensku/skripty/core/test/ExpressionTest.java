package io.github.bensku.skripty.core.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import org.junit.jupiter.api.Test;

import io.github.bensku.skripty.core.RunnerState;
import io.github.bensku.skripty.core.SkriptType;
import io.github.bensku.skripty.core.expression.CallTarget;
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
	
	public Object callTargetC(RunnerState state, Object obj) {
		return obj;
	}
	
	@Test
	public void findCallTarget() throws Throwable {
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		SkriptType stringType = SkriptType.create(String.class);
		CallTarget targetA = new CallTarget(lookup.findVirtual(getClass(), "callTargetA",
				MethodType.methodType(Object.class)), false);
		CallTarget targetB = new CallTarget(lookup.findVirtual(getClass(), "callTargetB",
				MethodType.methodType(Object.class, String.class)), false, stringType);
		CallTarget targetC = new CallTarget(lookup.findVirtual(getClass(), "callTargetC",
				MethodType.methodType(Object.class, RunnerState.class, Object.class)),
				true, type);
		
		CallableExpression expr = registry.makeCallable(this)
				.inputTypes(new InputType(true, stringType, type))
				.returnType(type)
				.callTargets(targetA, targetB, targetC)
				.create();
		assertEquals(1, expr.getInputTypes().length);
		assertEquals(type, expr.getReturnType());
		
		// Test that correct call targets are found
		assertEquals("hello, world", expr.findTarget(new SkriptType[0], new Class[0], true)
				.getMethod().invokeExact(this));
		assertEquals("abc", expr.findTarget(new SkriptType[] {stringType}, new Class[] {String.class}, true)
				.getMethod().invokeExact(this, "abc"));
		Object token = new Object();
		assertEquals(token, expr.findTarget(new SkriptType[] {type}, new Class[] {Object.class}, true)
				.getMethod().invokeExact(this, (RunnerState) null, token));
		
		// And then that calling executes them, too
		assertEquals("hello, world", expr.call());
		assertEquals("abc", expr.call("abc"));
		assertEquals(token, expr.call(token));
	}
	
	@Test
	public void builderErrors() throws NoSuchMethodException, IllegalAccessException {
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		CallTarget targetA = new CallTarget(lookup.findVirtual(getClass(), "callTargetA", MethodType.methodType(Object.class)), false);
		CallTarget targetB = new CallTarget(lookup.findVirtual(getClass(), "callTargetB", MethodType.methodType(Object.class, String.class)), false, type);
		
		assertThrows(IllegalStateException.class, () -> registry.makeCallable(this).callTargets());
		assertThrows(IllegalArgumentException.class, () -> registry.makeCallable(this)
				.inputTypes(new InputType(true, type), new InputType(false, type)));
		assertThrows(IllegalArgumentException.class, () -> registry.makeCallable(this)
				.inputTypes(new InputType(true, SkriptType.create(int.class)))
				.returnType(SkriptType.VOID)
				.callTargets(targetA, targetB));
	}
}
