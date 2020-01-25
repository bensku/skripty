package io.github.bensku.skripty.core.expression;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import io.github.bensku.skripty.core.SkriptType;

/**
 * An expression that is not a {@link Literal}.
 *
 */
public class CallableExpression extends Expression {
	
	public static class Builder {
		
		private final ExpressionRegistry registry;
		private final int id;
		private final Object instance;
		
		private InputType[] inputTypes;
		private SkriptType returnType;
		private MethodHandle[] callTargets;
		
		Builder(ExpressionRegistry registry, int id, Object instance) {
			this.registry = registry;
			this.id = id;
			this.instance = instance;
		}
		
		public Builder inputTypes(InputType... inputTypes) {
			this.inputTypes = inputTypes;
			return this;
		}
		
		public Builder returnType(SkriptType returnType) {
			this.returnType = returnType;
			return this;
		}
		
		public Builder callTargets(MethodHandle... callTargets) {
			this.callTargets = callTargets;
			return this;
		}
		
		public CallableExpression create() {
			CallableExpression expr = new CallableExpression(id, instance, inputTypes, returnType, callTargets);
			registry.addExpression(expr);
			return expr;
		}
	}

	/**
	 * Types of the inputs that this expression takes.
	 */
	private final InputType[] inputTypes;
	
	/**
	 * Type of data that this expression returns.
	 */
	private final SkriptType returnType;
	
	/**
	 * Possible call targets for this expression type. The one that is closest
	 * match to provided inputs is selected. Failing that, entry points at
	 * start of this array have higher priority.
	 */
	private final MethodHandle[] callTargets;

	private CallableExpression(int id, Object instance, InputType[] inputTypes, SkriptType returnType,
			MethodHandle[] callTargets) {
		super(id);
		this.inputTypes = inputTypes;
		this.returnType = returnType;
		this.callTargets = callTargets;
		for (int i = 0; i < callTargets.length; i++) {
			callTargets[i] = callTargets[i].bindTo(instance);
		}
	}

	@Override
	public Object call(Object... inputs) {
		Class<?>[] inputClasses = new Class<?>[inputs.length];
		for (int i = 0; i < inputClasses.length; i++) {
			inputClasses[i] = inputs[i].getClass();
		}
		MethodHandle target = findTarget(inputClasses, false);
		assert target != null : "no call targets found";
		try {
			return target.invokeWithArguments(inputs);
		} catch (Throwable e) {
			throw new AssertionError(e); // TODO handle this better
		}
	}

	@Override
	public InputType[] getInputTypes() {
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
		for (MethodHandle candidate : callTargets) {
			if (doParametersMatch(candidate.type(), inputClasses, exact)) {
				return candidate; // Parameters match
			}
		}
		return null;
	}
	
	private boolean doParametersMatch(MethodType type, Class<?>[] inputClasses, boolean exact) {
		if (inputClasses.length != type.parameterCount()) {
			return false; // Wrong number of parameters
		}
		for (int i = 0; i < type.parameterCount(); i++) {
			if (exact) { // Exact match required, check if classes are exactly same
				if (!type.parameterType(i).equals(inputClasses[i])) {
					return false;
				}
			} else { // Input just needs to be convertible to requested class
				if (!type.parameterType(i).isAssignableFrom(inputClasses[i])) {
					return false;
				}
			}
		}
		return true;
	}
}
