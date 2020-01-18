package io.github.bensku.skripty.core;

/**
 * An abstract syntax tree node that represents a single expression.
 *
 */
public class AstNode {

	/**
	 * Type of data returned by the expression represented by this node.
	 */
	private final SkriptType returnType;
	
	/**
	 * Nodes that expression represented by this node takes as inputs.
	 */
	private final AstNode[] inputs;
	
	public AstNode(SkriptType returnType, AstNode[] inputs) {
		this.returnType = returnType;
		this.inputs = inputs;
	}

	public SkriptType getReturnType() {
		return returnType;
	}

	public AstNode[] getInputs() {
		return inputs;
	}
	
}
