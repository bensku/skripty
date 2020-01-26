package io.github.bensku.skripty.parser;

import io.github.bensku.skripty.core.AstNode;

/**
 * Parses expressions into {@link AstNode}s.
 *
 */
public class ExpressionParser {

	/**
	 * Parsers for literal types. These are applied basically everywhere before
	 * trying to parse expressions. As such, having more than a few of them has
	 * a big impact on performance.
	 */
	private final LiteralParser[] literalParsers;
	
	/**
	 * Expression layers in the order of preference.
	 */
	private final ExpressionLayer[] expressions;

	private ExpressionParser(LiteralParser[] literalParsers, ExpressionLayer[] expressions) {
		this.literalParsers = literalParsers;
		this.expressions = expressions;
	}
	
	public AstNode parse(byte[] input, int start, int end) {
		throw new UnsupportedOperationException("TODO");
	}
}
