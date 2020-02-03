package io.github.bensku.skripty.core;

import io.github.bensku.skripty.core.expression.Expression;

/**
 * An abstract syntax tree node that represents a single expression.
 *
 */
public class AstNode {
	
	public static class Literal extends AstNode {
		
		/**
		 * The literal value.
		 */
		private final Object value;
		
		public Literal(Object value) {
			this.value = value;
		}

		public Object getValue() {
			return value;
		}
	}
	
	public static class Expr extends AstNode implements ScriptUnit {
		
		/**
		 * Expression that this node represents
		 */
		private final Expression expression;
		
		/**
		 * Nodes that expression represented by this node takes as inputs.
		 */
		private final AstNode[] inputs;
		
		public Expr(Expression expression, AstNode[] inputs) {
			this.expression = expression;
			this.inputs = inputs;
		}

		public Expr(Expression expression) {
			this(expression, new AstNode[expression.getInputCount()]);
		}

		public Expression getExpression() {
			return expression;
		}

		public AstNode[] getInputs() {
			return inputs;
		}
	}
	
	private AstNode() {}
	
}
