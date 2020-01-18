package io.github.bensku.skripty.core.expression;

import java.lang.invoke.MethodHandle;

/**
 * A call target for a {@link CallableExpression callable expression}.
 * It describes a method that, depending on <i>classes</i> of inputs,
 * might be usable as expression implementation.
 *
 */
public class CallTarget {
	
	/**
	 * Classes that need to be assignable from the inputs for this entry point
	 * to be used.
	 */
	private final Class<?>[] classes;
	
	/**
	 * The method that is used as this entry point.
	 */
	private final MethodHandle method;

	private CallTarget(Class<?>[] classes, MethodHandle method) {
		this.classes = classes;
		this.method = method;
	}

	public Class<?>[] getClasses() {
		return classes;
	}

	public MethodHandle getMethod() {
		return method;
	}
	
	/**
	 * Checks if this call target accepts inputs with given classes.
	 * @param inputClasses Classes of inputs.
	 * @param exact Whether exact match is required. If this is true,
	 * using {@link MethodHandle#invokeExact(Object...)} on
	 * {@link #getMethod()} is safe with given parameters, provided that
	 * the return type is set properly.
	 * @return Whether or not this call targets accepts inputs of given types.
	 */
	public boolean accepts(Class<?>[] inputClasses, boolean exact) {
		if (classes.length != inputClasses.length) {
			return false; // Wrong number of arguments
		}
		for (int i = 0; i < classes.length; i++) {
			if (exact) {
				if (!classes[i].equals(inputClasses[i])) {
					return false;
				}
			} else { // Be a bit more lenient, allow type casting
				if (!classes[i].isAssignableFrom(inputClasses[i])) {
					return false;
				}
			}
		}
		return true;
	}
	
}
