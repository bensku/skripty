package io.github.bensku.skripty.core.expression;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.function.Consumer;

import io.github.bensku.skripty.core.RunnerState;
import io.github.bensku.skripty.core.annotation.Inputs;
import io.github.bensku.skripty.core.annotation.Returns;
import io.github.bensku.skripty.core.annotation.Type;
import io.github.bensku.skripty.core.type.SkriptType;
import io.github.bensku.skripty.core.type.TypeSystem;

/**
 * A registry for expressions.
 *
 */
public class ExpressionRegistry {

	private static final int INITIAL_SIZE = 16;
	
	private Expression[] expressions;
	
	private int expressionCount;
	
	public ExpressionRegistry() {
		this.expressions = new Expression[INITIAL_SIZE];
	}
	
	/**
	 * Directly adds an expression to this registry.
	 * @param expr Expression.
	 */
	void addExpression(Expression expr) {
		if (expressionCount == expressions.length) {
			Expression[] exprs = new Expression[expressionCount * 2];
			System.arraycopy(expressions, 0, exprs, 0, expressionCount);
			expressions = exprs;
		}
		expressions[expressionCount++] = expr;
	}

	/**
	 * Creates a builder for a {@link CallableExpression callable expression}.
	 * @param instance Object that the call targets will receive as 'this'.
	 * @return Callable expression builder.
	 */
	public CallableExpression.Builder makeCallable(Object instance) {
		return new CallableExpression.Builder(this, expressionCount, instance);
	}
	
	/**
	 * Makes a callable expression from based on annotations of given class.
	 * @param typeSystem Type system with which type names should be resolved.
	 * @param impl Implementation class.
	 * @return A callable expression.
	 * @throws IllegalArgumentException If required annotations are missing, or
	 * their values are incorrect.
	 */
	public CallableExpression makeCallable(TypeSystem typeSystem, Object instance) {
		Class<?> impl = instance.getClass();
		
		// Input types
		Inputs inputsAnn = impl.getAnnotation(Inputs.class);
		if (inputsAnn == null) {
			throw new IllegalArgumentException("impl lacks @Inputs");
		}
		String[] inputStrs = inputsAnn.value();
		InputType[] inputs = new InputType[inputStrs.length];
		for (int i = 0; i < inputs.length; i++) { // Input slots
			String slotDesc = inputStrs[i];
			boolean optional = false;
			if (slotDesc.endsWith("?")) { // Optional input slot
				slotDesc = slotDesc.substring(0, slotDesc.length() - 1);
				optional = true;
			}
			
			// Go through all accepted input types
			String[] options = slotDesc.split("/");
			SkriptType[] types = new SkriptType[options.length];
			for (int j = 0; j < types.length; j++) {
				types[j] = typeSystem.resolve(options[j]);
			}
			inputs[i] = new InputType(optional, types);
		}
		
		// Return type
		Returns returnsAnn = impl.getAnnotation(Returns.class);
		if (returnsAnn == null) {
			throw new IllegalArgumentException("impl lacks @Returns");
		}
		SkriptType returnType = typeSystem.resolve(returnsAnn.value());
		
		// All potential call targets
		Method[] methods = impl.getDeclaredMethods();
		CallTarget[] callTargets = new CallTarget[methods.length];
		int targetCount = 0;
		MethodHandles.Lookup lookup = MethodHandles.publicLookup();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			if (method.getAnnotation(io.github.bensku.skripty.core.annotation.CallTarget.class) != null) {
				try {
					MethodHandle handle = lookup.unreflect(method); // Core reflection -> MethodHandle
					Parameter[] params = method.getParameters();
					// If method accepts a RunnerState as first parameter, assume that it should be injected
					boolean injectState = params.length > 0 && RunnerState.class.isAssignableFrom(params[0].getType());
					int injectedCount = injectState ? 1 : 0; // Skip runner state parameter
					SkriptType[] inputTypes = new SkriptType[params.length - injectedCount];
					for (int j = 0; j < inputTypes.length; j++) {
						Type type = params[j + injectedCount].getAnnotation(Type.class);
						if (type != null) { // That parameter wants to limit accepted SkriptTypes
							inputTypes[j] = typeSystem.resolve(type.value());
						}
					}
					
					callTargets[targetCount++] = new CallTarget(handle, injectState, inputTypes);
				} catch (IllegalAccessException e) {
					throw new IllegalArgumentException("cannot access call target '" + method.getName() + "'", e);
				}
			}
		}
		
		CallTarget[] targets = new CallTarget[targetCount];
		System.arraycopy(callTargets, 0, targets, 0, targetCount);
		return makeCallable(instance)
				.inputTypes(inputs)
				.returnType(returnType)
				.callTargets(targets)
				.create();
	}
	
	/**
	 * Creates a constant expression.
	 * @param type Return type of the constant.
	 * @param value Constant value of the expression.
	 * @return A constant expression.
	 */
	public ConstantExpression makeConstant(SkriptType type, Object value) {
		ConstantExpression expr = new ConstantExpression(expressionCount, type, value);
		addExpression(expr);
		return expr;
	}

	/**
	 * Gets how many expressions have been registered.
	 * @return Expression count.
	 */
	public int getExpressionCount() {
		return expressionCount;
	}
	
	public void forEach(Consumer<Expression> func) {
		for (int i = 0; i < expressionCount; i++) {
			func.accept(expressions[i]);
		}
	}
}
