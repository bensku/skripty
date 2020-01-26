package io.github.bensku.skripty.core.expression;

import io.github.bensku.skripty.core.SkriptType;

/**
 * An expression used in scripts.
 *
 */
public abstract class Expression {
	
	/**
	 * Id of this expression.
	 */
	private final int id;
	
	protected Expression(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}

	/**
	 * Calls the expression with given inputs.
	 * @param inputs Inputs for the expression.
	 * @return Values returned by the expression.
	 */
	public abstract Object call(Object... inputs);
	
	/**
	 * Gets input types that this expression accepts.
	 * @return Accepted input types. Length of this array indicates how many
	 * inputs are accepted.
	 */
	public abstract InputType[] getInputTypes();
	
	public int getInputCount() {
		return getInputTypes().length;
	}
	
	/**
	 * Gets return type of this expression. When it is called, a value
	 * of this type is returned.
	 * @return Return type.
	 */
	public abstract SkriptType getReturnType();

}
