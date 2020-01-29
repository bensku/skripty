package io.github.bensku.skripty.parser;

import io.github.bensku.skripty.core.expression.Expression;
import io.github.bensku.skripty.parser.pattern.Pattern;

/**
 * Information about an expression that the parser needs.
 * These are stored in expression lookup tree.
 *
 */
public class ExpressionInfo {

	/**
	 * Pattern to use for matching this expression.
	 */
	private final Pattern pattern;
	
	/**
	 * The expression instance.
	 */
	private final Expression expression;

	public ExpressionInfo(Pattern pattern, Expression expression) {
		this.pattern = pattern;
		this.expression = expression;
	}

	public Pattern getPattern() {
		return pattern;
	}

	public Expression getExpression() {
		return expression;
	}
	
}
