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
		private CallTarget[] callTargets;
		
		Builder(ExpressionRegistry registry, int id, Object instance) {
			this.registry = registry;
			this.id = id;
			this.instance = instance;
		}
		
		/**
		 * Sets types of inputs that the expression takes.
		 * @param types Input types.
		 * @return This builder.
		 * @throws IllegalArgumentException When there are required inputs
		 * after optional ones.
		 */
		public Builder inputTypes(InputType... types) {
			// Check that there are no required inputs after optional ones
			boolean hasOptional = false;
			for (InputType type : types) {
				if (!type.isOptional() && hasOptional) {
					throw new IllegalArgumentException("required types must be before optional ones");
				}
				hasOptional = type.isOptional();
			}
			this.inputTypes = types;
			return this;
		}
		
		/**
		 * Sets return type of the expression.
		 * @param type Return type.
		 * @return This builder.
		 */
		public Builder returnType(SkriptType type) {
			this.returnType = type;
			return this;
		}
		
		/**
		 * Sets call targets of the expression. One of then will be
		 * called based on classes of actual inputs. This method must not be
		 * called before both {@link #inputTypes(InputType...) input types} and
		 * the {@link #returnType(SkriptType) return type} have been set.
		 * @param targets Call targets.
		 * @return This builder.
		 * @throws IllegalArgumentException When one of given call targets does
		 * would never be called, because its return type or parameter types
		 * are incompatible with this expression's return type or input types.
		 * @throws IllegalStateException When called too early.
		 */
		public Builder callTargets(CallTarget... targets) {
			if (inputTypes == null || returnType == null) {
				throw new IllegalStateException("must specify input and return types before call targets");
			}
			
			// Validate that the call targets match input and return types
			for (CallTarget target : targets) {
				SkriptType[] filterTypes = target.getInputTypes();
				MethodType type = target.getMethod().type();
				
				// Validate method parameters
				// 'this' is always implicit first parameter
				// After it, there might be injected RunnerState
				int injectedCount = target.shouldInjectState() ? 2 : 1;
				for (int i = injectedCount; i < type.parameterCount(); i++) {
					Class<?> paramClass = type.parameterType(i);
					
					int inputIndex = i - injectedCount;
					InputType input = inputTypes[inputIndex];
					boolean oneCompatibleType = false;
					for (SkriptType option : input.getTypes()) {
						try {
							// Must be implemented by JVM type that we can actually take
							if (checkCompatible(option.materialize().getBackingClass(), paramClass)) {
								// Must also match type filter, if one is set
								if (filterTypes[inputIndex] == null || filterTypes[inputIndex].equals(option)) {
									oneCompatibleType = true;
								}
							}
						} catch (ClassNotFoundException e) {
							throw new IllegalArgumentException("failed to materialize input type", e);
						}
					}
					if (!oneCompatibleType) { // Throw if parameter type isn't any of accepted types
						throw new IllegalArgumentException("parameter " + inputIndex + " (" + paramClass.getName() + ") does not match any of input types");
					}
				}
				
				// Check the return type
				try {
					Class<?> expected = returnType.materialize().getBackingClass();
					Class<?> actual = type.returnType();
					if (!checkCompatible(expected, actual)) {
						throw new IllegalArgumentException(expected + " is not assignable from return type " + actual);
					}
				} catch (ClassNotFoundException e) {
					throw new IllegalArgumentException("failed to materialize return type", e);
				}
				
			}
			this.callTargets = targets;
			return this;
		}
		
		private boolean checkCompatible(Class<?> expected, Class<?> actual) {
			if (!expected.isAssignableFrom(actual)) {
				if (actual.isPrimitive()) { // Try if boxed type would be assignable
					// Replace primitives with boxed types to allow e.g. j.l.Number in SkriptTypes
					if (actual.equals(boolean.class)) {
						actual = Boolean.class;
					} else if (actual.equals(byte.class)) {
						actual = Byte.class;
					} else if (actual.equals(short.class)) {
						actual = Short.class;
					} else if (actual.equals(char.class)) {
						actual = Character.class;
					} else if (actual.equals(int.class)) {
						actual = Integer.class;
					} else if (actual.equals(long.class)) {
						actual = Long.class;
					} else if (actual.equals(float.class)) {
						actual = Float.class;
					} else if (actual.equals(double.class)) {
						actual = Double.class;
					}
					
					if (expected.isAssignableFrom(actual)) {
						return true; // Succeeded with boxing, no problem here
					}
				}
				return false;
			}
			return true;
		}
		
		/**
		 * Creates a callable expression. This causes it to be registered to
		 * the registry that created this builder.
		 * @return The callable expression.
		 */
		public CallableExpression create() {
			CallableExpression expr = new CallableExpression(id, instance, inputTypes, returnType, callTargets);
			registry.addExpression(expr);
			return expr;
		}
	}
	
	/**
	 * Instance of the object call targets of this expression are bound to.
	 */
	private final Object instance;

	/**
	 * Types of the inputs that this expression takes.
	 */
	private final InputType[] inputTypes;
	
	/**
	 * Type of data that this expression returns.
	 */
	private final SkriptType returnType;
	
	/**
	 * Possible call targets for this expression.
	 */
	private final CallTarget[] callTargets;

	private CallableExpression(int id, Object instance, InputType[] inputTypes, SkriptType returnType,
			CallTarget[] callTargets) {
		super(id);
		this.instance = instance;
		this.inputTypes = inputTypes;
		this.returnType = returnType;
		this.callTargets = callTargets;
	}

	@Override
	public Object call(Object... inputs) {
		Class<?>[] inputClasses = new Class<?>[inputs.length];
		for (int i = 0; i < inputClasses.length; i++) {
			inputClasses[i] = inputs[i].getClass();
		}
		
		CallTarget target = findTarget(null, inputClasses, false);
		assert target != null : "no call targets found";
		try {
			int injectedCount = target.shouldInjectState() ? 2 : 1;
			Object[] newInputs = new Object[inputs.length + injectedCount];
			newInputs[0] = instance;
			if (target.shouldInjectState()) {
				newInputs[1] = null; // We don't have a runner, so no state either
			}
			System.arraycopy(inputs, 0, newInputs, injectedCount, inputs.length);
			inputs = newInputs;
			return target.getMethod().invokeWithArguments(inputs);
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
	
	public Object getInstance() {
		return instance;
	}

	/**
	 * Attempts to find a suitable call target for inputs of given types.
	 * @param foundInputs Types of inputs we're looking for a call target.
	 * If input types are not known, this can be left null; however, there is
	 * a risk that a wrong call target gets chosen if this is done.
	 * @param inputClasses Classes of input values.
	 * @param exact Whether or not to require input classes to exactly match
	 * those of the call site. If this is enabled,
	 * {@link MethodHandle#invokeExact(Object...)} can be called on the target,
	 * provided that return type is set correctly.
	 * @return A target method.
	 */
	public CallTarget findTarget(SkriptType[] foundInputs, Class<?>[] inputClasses, boolean exact) {
		for (CallTarget candidate : callTargets) {
			if (doTypesMatch(candidate.getInputTypes(), foundInputs)
					&& doParametersMatch(candidate.getMethod().type(), inputClasses, exact,
							candidate.shouldInjectState() ? 2 : 1)) {
				return candidate; // Parameters match
			}
		}
		return null;
	}
	
	private boolean doTypesMatch(SkriptType[] expected, SkriptType[] actual) {
		if (actual == null) { // Caller only cares about JVM types matching
			return true;
		}
		if (expected.length != actual.length) {
			return false;
		}
		for (int i = 0; i < expected.length; i++) {
			if (expected[i] != null && !expected[i].equals(actual[i])) {
				return false;
			}
		}
		return true;
	}
	
	private boolean doParametersMatch(MethodType type, Class<?>[] inputClasses, boolean exact,
			int injectedCount) {
		if (inputClasses.length != type.parameterCount() - injectedCount) {
			return false; // Wrong number of parameters
		}
		for (int i = injectedCount; i < type.parameterCount(); i++) {
			if (exact) { // Exact match required, check if classes are exactly same
				if (!type.parameterType(i).equals(inputClasses[i - injectedCount])) {
					return false;
				}
			} else { // Input just needs to be convertible to requested class
				if (!type.parameterType(i).isAssignableFrom(inputClasses[i - injectedCount])) {
					return false;
				}
			}
		}
		return true;
	}
}
