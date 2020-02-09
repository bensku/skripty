package io.github.bensku.skripty.core.expression;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Consumer;

import io.github.bensku.skripty.core.SkriptType;
import io.github.bensku.skripty.core.annotation.CallTarget;
import io.github.bensku.skripty.core.annotation.Inputs;
import io.github.bensku.skripty.core.annotation.Returns;

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
	 * @param typeSystem Class that has types available to scripts as
	 * constants.
	 * @param impl Implementation class.
	 * @return A callable expression.
	 * @throws IllegalArgumentException If required annotations are missing, or
	 * their values are incorrect.
	 */
	public CallableExpression makeCallable(Class<?> typeSystem, Object instance) {
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
				types[j] = resolveType(typeSystem, options[j]);
			}
			inputs[i] = new InputType(optional, types);
		}
		
		// Return type
		Returns returnsAnn = impl.getAnnotation(Returns.class);
		if (returnsAnn == null) {
			throw new IllegalArgumentException("impl lacks @Returns");
		}
		SkriptType returnType = resolveType(typeSystem, returnsAnn.value());
		
		// All potential call targets
		Method[] methods = impl.getDeclaredMethods();
		MethodHandle[] callTargets = new MethodHandle[methods.length];
		int targetCount = 0;
		MethodHandles.Lookup lookup = MethodHandles.publicLookup();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			if (method.getAnnotation(CallTarget.class) != null) {
				try {
					callTargets[targetCount++] = lookup.unreflect(method);
				} catch (IllegalAccessException e) {
					throw new IllegalArgumentException("cannot access call target '" + method.getName() + "'");
				}
			}
		}
		
		MethodHandle[] targets = new MethodHandle[targetCount];
		System.arraycopy(callTargets, 0, targets, 0, targetCount);
		return makeCallable(instance)
				.inputTypes(inputs)
				.returnType(returnType)
				.callTargets(targets)
				.create();
	}
	
	private SkriptType resolveType(Class<?> typeSystem, String name) {
		// Search for field with name from "type system" class
		Field field;
		try {
			field = typeSystem.getField(name.toUpperCase());
		} catch (NoSuchFieldException | SecurityException e) {
			throw new IllegalArgumentException("cannot resolve type '" + name, e);
		}
		try {
			return (SkriptType) field.get(null);
		} catch (ClassCastException | IllegalArgumentException | IllegalAccessException e) {
			throw new IllegalArgumentException("cannot access type '" + name, e);
		}
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
