package io.github.bensku.skripty.core.expression;

import java.lang.invoke.MethodHandle;

import io.github.bensku.skripty.core.SkriptType;

/**
 * An expression that is not a {@link Literal}.
 *
 */
public class CallableExpression extends Expression {

	/**
	 * Instance of the expression implementation.
	 */
	private final Object instance;

	private final SkriptType[][] inputTypes;
	private final SkriptType returnType;
	
	/**
	 * Possible call targets for this expression type. The one that is closest
	 * match to provided inputs is selected. Failing that, entry points at
	 * start of this array have higher priority.
	 */
	private final CallTarget[] callTargets;

	private CallableExpression(Object instance, SkriptType[][] inputTypes, SkriptType returnType,
			CallTarget[] callTargets) {
		this.instance = instance;
		this.inputTypes = inputTypes;
		this.returnType = returnType;
		this.callTargets = callTargets;
	}

	@Override
	public Object call(Object[] inputs) {
		Class<?>[] inputClasses = new Class<?>[inputs.length];
		for (int i = 0; i < inputClasses.length; i++) {
			inputClasses[i] = inputs[i].getClass();
		}
		MethodHandle target = findTarget(inputClasses, false);
		assert target != null : "no call targets found";
		try {
			return target.bindTo(instance).asSpreader(Object[].class, inputs.length).invoke(inputs);
		} catch (Throwable e) {
			throw new AssertionError(e); // TODO handle this better
		}
	}

	@Override
	public SkriptType[][] getInputTypes() {
		return inputTypes;
	}

	@Override
	public SkriptType getReturnType() {
		return returnType;
	}

	/**
	 * Attempts to find a suitable call target for inputs of given types.
	 * @param inputClasses Classes of input values.
	 * @param exact Whether or not to require input classes to exactly match
	 * those of the call site. If this is enabled,
	 * {@link MethodHandle#invokeExact(Object...)} can be called on the target,
	 * provided that return type is set correctly.
	 * @return A target method.
	 */
	public MethodHandle findTarget(Class<?>[] inputClasses, boolean exact) {
		for (CallTarget candidate : callTargets) {
			if (candidate.accepts(inputClasses, exact)) {
				return candidate.getMethod();
			}
		}
		return null;
	}
}
