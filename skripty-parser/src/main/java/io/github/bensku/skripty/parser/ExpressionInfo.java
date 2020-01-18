package io.github.bensku.skripty.parser;

import io.github.bensku.skripty.core.AstNode;
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
	 * Id of the expression that goes to {@link AstNode} and is used to select
	 * the proper {@link Expression} implementation for it.
	 */
	private final int expressionId;

	private ExpressionInfo(Pattern pattern, int expressionId) {
		this.pattern = pattern;
		this.expressionId = expressionId;
	}

	public Pattern getPattern() {
		return pattern;
	}

	public int getExpressionId() {
		return expressionId;
	}
	
}
