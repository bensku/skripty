package io.github.bensku.skripty.core;

import io.github.bensku.skripty.core.expression.Expression;

/**
 * An abstract syntax tree node that represents a single expression.
 *
 */
public class AstNode {
	
	/**
	 * Expression that this node represents
	 */
	private final Expression expression;
	
	/**
	 * Nodes that expression represented by this node takes as inputs.
	 */
	private final AstNode[] inputs;
	
	public AstNode(Expression expression, AstNode[] inputs) {
		this.expression = expression;
		this.inputs = inputs;
	}

	public Expression getExpression() {
		return expression;
	}

	public AstNode[] getInputs() {
		return inputs;
	}
	
}
