package io.github.bensku.skripty.core.flow;

import io.github.bensku.skripty.core.SkriptType;

/**
 * Returned by expressions that implement scopes to indicate when the scopes
 * should be entered.
 *
 */
public enum ScopeEntry {
	
	/**
	 * Enter the scope. After exiting from it, call the title expression again
	 * to see if the scope should be entered again.
	 */
	YES,
	
	/**
	 * Enter the scope once. Do not call the title expression again.
	 */
	ONCE,
	
	/**
	 * Do not enter the scope.
	 */
	NO;
	
	public static SkriptType TYPE = SkriptType.create(ScopeEntry.class);

}
