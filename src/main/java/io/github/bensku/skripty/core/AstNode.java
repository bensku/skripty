package io.github.bensku.skripty.core;

import io.github.bensku.skripty.core.expression.Expression;
import io.github.bensku.skripty.core.type.SkriptType;

/**
 * An abstract syntax tree node that represents a single expression.
 *
 */
public abstract class AstNode {
	
	public abstract SkriptType getReturnType();
	
	public static class Literal extends AstNode {
		
		/**
		 * Type of the value, as exposed to scripts.
		 */
		private final SkriptType type;
		
		/**
		 * The literal value.
		 */
		private final Object value;
		
		public Literal(SkriptType type, Object value) {
			this.type = type;
			this.value = value;
		}

		public Object getValue() {
			return value;
		}

		@Override
		public SkriptType getReturnType() {
			return type;
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

		@Override
		public SkriptType getReturnType() {
			return expression.getReturnType();
		}
	}
	
	private AstNode() {}
	
}
