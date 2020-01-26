package io.github.bensku.skripty.parser;

import java.util.Arrays;
import java.util.Collection;

import io.github.bensku.skripty.core.AstNode;
import io.github.bensku.skripty.parser.pattern.Pattern;
import io.github.bensku.skripty.parser.pattern.PatternPart;

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
	
	public AstNode parse(byte[] input, int start) {
		for (ExpressionLayer layer : expressions) {
			Collection<ExpressionInfo> candidates = layer.lookupFirst(input, start);
			selectCandidate: for (ExpressionInfo info : candidates) {
				AstNode.Expr node = new AstNode.Expr(info.getExpression());
				AstNode[] inputs = node.getInputs();
				
				Pattern pattern = info.getPattern();
				// TODO less hacky way of getting current pos
				int pos = start + ((PatternPart.Literal) pattern.partAt(0)).getText().length;
				
				for (int i = 1; i < pattern.length(); i++) {
					PatternPart part = pattern.partAt(i);
					if (part instanceof PatternPart.Literal) {
						byte[] text = ((PatternPart.Literal) part).getText();
						
						if (!Arrays.equals(text, 0, text.length, input, pos, pos + text.length)) {
							break selectCandidate;
						}
						pos += text.length;
					} else {
						// TODO customize input placement
						int inputIndex = ((PatternPart.Input) part).getIndex();
						AstNode childNode = parse(input, pos);
						if (childNode == null) {
							break selectCandidate;
						}
						
						return node; // Success!
					}
				}
			}
		}
		return null;
	}

}
