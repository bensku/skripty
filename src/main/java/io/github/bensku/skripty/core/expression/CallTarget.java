package io.github.bensku.skripty.core.expression;

import java.lang.invoke.MethodHandle;

import io.github.bensku.skripty.core.SkriptType;

/**
 * Represents a potential call target of a
 * {@link CallableExpression callable expression}.
 *
 */
public class CallTarget {

	/**
	 * Handle of method to be called.
	 */
	private final MethodHandle method;
	
	/**
	 * If this call target should receive runner state as first parameter.
	 */
	private final boolean injectState;

	/**
	 * Accepted input {@link SkriptType types} this call target can take.
	 * Nulls in the array indicate that all inputs with compatible JVM types
	 * may be taken.
	 */
	private final SkriptType[] inputTypes;

	public CallTarget(MethodHandle method, boolean injectState, SkriptType... inputTypes) {
		this.method = method;
		this.injectState = injectState;
		this.inputTypes = inputTypes;
	}

	public MethodHandle getMethod() {
		return method;
	}
	
	public boolean shouldInjectState() {
		return injectState;
	}

	public SkriptType[] getInputTypes() {
		return inputTypes;
	}

}
