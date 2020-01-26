package io.github.bensku.skripty.core.expression;

import java.util.ArrayList;
import java.util.List;

import io.github.bensku.skripty.core.SkriptType;

/**
 * A registry for expressions.
 *
 */
public class ExpressionRegistry {

	private final List<Expression> expressions;
	
	public ExpressionRegistry() {
		this.expressions = new ArrayList<>(); // TODO replace
	}
	
	void addExpression(Expression expr) {
		expressions.add(expr);
	}
	
	public CallableExpression.Builder makeCallable(Object instance) {
		return new CallableExpression.Builder(this, expressions.size(), instance);
	}
	
	public ConstantExpression makeConstant(SkriptType type, Object value) {
		ConstantExpression expr = new ConstantExpression(expressions.size(), type, value);
		addExpression(expr);
		return expr;
	}
}
