package io.github.bensku.skripty.core.expression;

import io.github.bensku.skripty.core.SkriptType;

/**
 * Type of an input taken by an {@link Expression expression}
 *
 */
public class InputType {

	/**
	 * Types we're accepting, in order of preference.
	 */
	private final SkriptType[] types;
	
	/**
	 * If the type can be omitted.
	 */
	private final boolean optional;

	public InputType(SkriptType[] types, boolean optional) {
		this.types = types;
		this.optional = optional;
	}

	public SkriptType[] getTypes() {
		return types;
	}

	public boolean isOptional() {
		return optional;
	}

}
