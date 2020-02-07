package io.github.bensku.skripty.parser.script;

import io.github.bensku.skripty.core.expression.Expression;
import io.github.bensku.skripty.core.expression.ExpressionRegistry;

/**
 * Allows retrieving {@link Scope scopes} with their title expressions.
 *
 */
public class ScopeRegistry {
	
	/**
	 * The expression registry this was created from.
	 */
	private final ExpressionRegistry expressions;

	/**
	 * The scopes.
	 */
	private final Scope[] scopes;
	
	/**
	 * Creates a new scope registry.
	 * @param expressions Expression registry with scope title expressions.
	 * It must contain no other expressions.
	 */
	public ScopeRegistry(ExpressionRegistry expressions) {
		this.expressions = expressions;
		this.scopes = new Scope[expressions.getExpressionCount()];
	}
	
	/**
	 * Gets the expression registry this was built from.
	 * @return An expression registry.
	 */
	public ExpressionRegistry getExpressions() {
		return expressions;
	}
	
	public void register(Expression expression, Scope scope) {
		scopes[expression.getId()] = scope;
	}
	
	public Scope resolve(Expression expression) {
		// TODO check that expression belongs in this registry, has scope, etc.
		return scopes[expression.getId()];
	}
}
