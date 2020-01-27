package io.github.bensku.skripty.parser;

import java.util.Arrays;
import java.util.Collection;

import io.github.bensku.skripty.core.AstNode;
import io.github.bensku.skripty.core.SkriptType;
import io.github.bensku.skripty.parser.pattern.Pattern;
import io.github.bensku.skripty.parser.pattern.PatternPart;
import io.github.bensku.skripty.parser.util.ArrayHelpers;

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
	
	public static class ParseResult {
		
		/**
		 * The AST node resulting from parsing.
		 */
		private final AstNode node;
		
		/**
		 * End index of this parse operation in input (exclusive).
		 */
		private final int end;

		private ParseResult(AstNode node, int end) {
			this.node = node;
			this.end = end;
		}

		public AstNode getNode() {
			return node;
		}

		public int getEnd() {
			return end;
		}

	}
	
	public ParseResult[] parse(byte[] input, int start, SkriptType[] types) {
		// Search expressions from each layer
		for (ExpressionLayer layer : expressions) {
			parseWithLayer(layer, input, start, types);
		}
		return null;
	}
	
	private ParseResult[] parseWithLayer(ExpressionLayer layer, byte[] input, int start, SkriptType[] types) {
		ExpressionInfo[] candidates = layer.lookupFirst(input, start);
		ParseResult[] results = new ParseResult[candidates.length];
		
		// Go through candidate expressions, find those that might match
		for (int i = 0; i < candidates.length; i++) {
			ExpressionInfo info = candidates[i];
			if (!ArrayHelpers.contains(types, info.getExpression().getReturnType())) {
				continue; // Skip candidates with incompatible return types
			}
			
			// Current position (index) in input array
			// TODO maybe something less hacky for getting current position?
			int pos = start + ((PatternPart.Literal) info.getPattern().partAt(0)).getText().length;
			
			// Try to match pattern of the candidate
			// Success or a failure, we'll return that to caller
			results[i] = matchPattern(info, 1, input, pos);
		}
		return results;
	}
	
	private ParseResult matchPattern(ExpressionInfo info, int firstPart, byte[] input, int pos) {
		AstNode.Expr node = new AstNode.Expr(info.getExpression());
		
		// Initially empty array of this candidate's inputs
		AstNode[] inputs = node.getInputs();
		
		// Pattern of candidate expression; we match input against this
		Pattern pattern = info.getPattern();
		
		// Match pattern part-by-part against input
		partsLoop: for (int i = firstPart; i < pattern.length(); i++) {
			PatternPart part = pattern.partAt(i);
			if (part instanceof PatternPart.Literal) {
				byte[] text = ((PatternPart.Literal) part).getText();
				
				// Check if the literal text and input match
				if (!Arrays.equals(text, 0, text.length, input, pos, pos + text.length)) {
					return null; // They're not same, this candidate is not possible
				}
				
				// Next part matches after this text
				pos += text.length;
			} else {
				// Figure out the index we'll place this in inputs array
				// TODO customizable input placement
				int inputIndex = ((PatternPart.Input) part).getIndex();
				
				// Get potential inputs
				ParseResult[] potentialInputs = parse(input, pos, ((PatternPart.Input) part).getTypes());
				
				// Evaluate whether or not we can parse parts of this expression
				// AFTER this input, should it be used
				for (ParseResult result : potentialInputs) {
					ParseResult after = matchPattern(info, i + 1, input, result.getEnd());
					if (after != null) {
						// Assign this as input
						inputs[inputIndex] = result.getNode();
						// Recursive matchPattern() call has set the subsequent inputs (if any) for us
						pos = after.getEnd(); // Skip ahead what was recursively parsed
						break partsLoop; // goto return success
					}
				}
				
				// Subsequent parts of this expression were not parseable with potential inputs
				// (or maybe there were no potential inputs at all)
				return null; // Failed to parse this candidate!
			}
		}
		
		// Everything went well, got an AST node out of it
		return new ParseResult(node, pos);
	}

}
