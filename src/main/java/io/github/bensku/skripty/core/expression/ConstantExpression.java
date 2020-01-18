package io.github.bensku.skripty.core.expression;

import io.github.bensku.skripty.core.SkriptType;

/**
 * An expression with a constant value.
 *
 */
public class ConstantExpression extends Expression {
	
	private static final InputType[] EMPTY_INPUTS = new InputType[0];

	/**
	 * The type of this that is visible to scripts.
	 */
	private final SkriptType type;
	
	/**
	 * The constant value.
	 */
	private final Object value;
	
	public ConstantExpression(SkriptType type, Object value) {
		this.type = type;
		this.value = value;
	}
	
	@Override
	public Object call(Object... inputs) {
		assert inputs.length == 0 : "constants don't take inputs";
		return value;
	}

	@Override
	public InputType[] getInputTypes() {
		return EMPTY_INPUTS;
	}

	@Override
	public SkriptType getReturnType() {
		return type;
	}

}
