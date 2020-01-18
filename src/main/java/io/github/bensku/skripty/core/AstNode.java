package io.github.bensku.skripty.core;

/**
 * An abstract syntax tree node that represents a single expression.
 *
 */
public class AstNode {
	
	/**
	 * Id of the expression this node represents.
	 */
	private final int expressionId;
	
	/**
	 * Nodes that expression represented by this node takes as inputs.
	 */
	private final AstNode[] inputs;
	
	public AstNode(int expressionId, AstNode[] inputs) {
		this.expressionId = expressionId;
		this.inputs = inputs;
	}

	int getExpressionId() {
		return expressionId;
	}

	public AstNode[] getInputs() {
		return inputs;
	}
	
}
