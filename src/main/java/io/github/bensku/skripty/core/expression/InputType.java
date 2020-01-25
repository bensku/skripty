package io.github.bensku.skripty.core.expression;

import io.github.bensku.skripty.core.SkriptType;

/**
 * Type of an input taken by an {@link Expression expression}
 *
 */
public class InputType {
	
	/**
	 * If the type can be omitted.
	 */
	private final boolean optional;
	
	/**
	 * Types we're accepting, in order of preference.
	 */
	private final SkriptType[] types;

	public InputType(boolean optional, SkriptType... types) {
		this.optional = optional;
		this.types = types;
	}

	public SkriptType[] getTypes() {
		return types;
	}

	public boolean isOptional() {
		return optional;
	}

}
