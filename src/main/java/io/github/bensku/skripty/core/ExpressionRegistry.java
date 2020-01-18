package io.github.bensku.skripty.core;

import java.util.List;

import io.github.bensku.skripty.core.expression.Expression;

public class ExpressionRegistry {

	public List<Expression> expressions;
	
	public int register(Expression expression) {
		expressions.add(expression);
		return expressions.size() - 1;
	}
	
	public Expression getForNode(AstNode node) {
		// TODO wrap bounds check exceptions
		return expressions.get(node.getExpressionId());
	}
}
