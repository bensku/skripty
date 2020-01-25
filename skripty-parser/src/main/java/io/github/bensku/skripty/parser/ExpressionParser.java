package io.github.bensku.skripty.parser;

import io.github.bensku.skripty.core.AstNode;

/**
 * Parses expressions into {@link AstNode}s.
 *
 */
public class ExpressionParser {

	/**
	 * Parsers for all known literal types.
	 */
	private final LiteralParser[] literalParsers;

	private ExpressionParser(LiteralParser[] literalParsers) {
		this.literalParsers = literalParsers;
	}
	
	public AstNode parse(byte[] input, int start, int end) {
		throw new UnsupportedOperationException("TODO");
	}
}
