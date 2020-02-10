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
		public Builder callTargets(MethodHandle... targets) {
			if (inputTypes == null || returnType == null) {
				throw new IllegalStateException("must specify input and return types before call targets");
			}
			
			// Validate that the call targets match input and return types
			for (MethodHandle target : targets) {
				MethodType type = target.type();
				// First parameter is 'this' of expression, skip it
				for (int i = 1; i < type.parameterCount(); i++) {
					Class<?> paramClass = type.parameterType(i);
					// TODO if we inject more parameters to call targets, filter them here
					
					InputType input = inputTypes[i - 1];
					for (SkriptType option : input.getTypes()) {
						try {
							if (!paramClass.isAssignableFrom(option.materialize().getBackingClass())) {
								throw new IllegalArgumentException("input type of call target doesn't match");
							}
						} catch (ClassNotFoundException e) {
							throw new IllegalArgumentException("failed to materialize input type", e);
						}
					}
				}
				
				try {
					if (!type.returnType().isAssignableFrom(returnType.materialize().getBackingClass())) {
						throw new IllegalArgumentException("return type of call target doesn't match");
					}
				} catch (ClassNotFoundException e) {
					throw new IllegalArgumentException("failed to materialize return type", e);
				}
				
			}
			this.callTargets = targets;
			return this;
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
	 * Possible call targets for this expression type. The one that is closest
	 * match to provided inputs is selected. Failing that, entry points at
	 * start of this array have higher priority.
	 */
	private final MethodHandle[] callTargets;

	private CallableExpression(int id, Object instance, InputType[] inputTypes, SkriptType returnType,
			MethodHandle[] callTargets) {
		super(id);
		this.instance = instance;
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
	
	public Object getInstance() {
		return instance;
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
