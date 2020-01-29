package io.github.bensku.skripty.parser;

import java.util.Arrays;
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

	public ExpressionParser(LiteralParser[] literalParsers, ExpressionLayer[] expressions) {
		this.literalParsers = literalParsers;
		this.expressions = expressions;
	}
	
	/**
	 * A result from an {@link ExpressionParser expression parser} operation.
	 *
	 */
	public static class Result {
		
		/**
		 * The AST node resulting from parsing.
		 */
		private final AstNode node;
		
		/**
		 * End index of this parse operation in input (exclusive).
		 */
		private final int end;

		private Result(AstNode node, int end) {
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
	
	/**
	 * Attempts to parse given input string.
	 * @param input Bytes of UTF-8 encoded input string.
	 * @param start Where to start parsing from in the input array.
	 * @param types Accepted return types of parsed expressions.
	 * @return The parse results, or an empty array if parsing failed.
	 */
	public Result[] parse(byte[] input, int start, SkriptType[] types) {
		// Try literal parsing first
		for (LiteralParser parser : literalParsers) {
			LiteralParser.Result result = parser.parse(input, start);
			if (result != null) {
				AstNode node = new AstNode.Literal(result.getValue());
				return new Result[] {new Result(node, result.getEnd())};
			}
		}
		
		// If it fails, parse expressions instead
		Result[] tempResults = new Result[128]; // TODO try to guess result count instead
		int resultCount = 0;
		
		// Search expressions from each layer
		for (ExpressionLayer layer : expressions) {
			Result[] results = parseWithLayer(layer, input, start, types);
			for (Result result : results) {
				if (result == null) {
					break; // Only nulls after this
				}
				tempResults[resultCount++] = result;
			}
		}
		
		// Ensure that the returned array has no trailing nulls
		Result[] allResults = new Result[resultCount];
		System.arraycopy(tempResults, 0, allResults, 0, resultCount);
		
		return allResults;
	}
	
	/**
	 * Parses given input with given expression layer. Note that this does
	 * not invoke {@link LiteralParser literal parsers}, which prevents ALL
	 * literals from being parsed.
	 * @param layer Layer to query expressions from.
	 * @param input Input to parse.
	 * @param start Index of byte where to start parsing from in input.
	 * @param types Accepted return types of parse results.
	 * @return Parse results, or an empty array if the input cannot be parsed
	 * in any way.
	 */
	private Result[] parseWithLayer(ExpressionLayer layer, byte[] input, int start, SkriptType[] types) {
		ExpressionInfo[] candidates = layer.lookupFirst(input, start);
		Result[] results = new Result[candidates.length];
		int resultCount = 0;
		
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
			// Success or a failure, we'll return that (result or null) to caller
			Result result = matchPattern(info, 1, input, pos);
			if (result != null) {
				results[resultCount++] = result;
			}
		}
		return results;
	}
	
	/**
	 * Matches pattern of an expression against input.
	 * @param info Expression info. This references both the pattern and
	 * expression.
	 * @param firstPart Index of first pattern part that we should evaluate.
	 * This is used to avoid evaluating parts twice when a they've been already
	 * matched by e.g {@link RadixTree the expression tree}.
	 * @param input Input (UTF-8) bytes to match against.
	 * @param pos Starting position in the input.
	 * @return A parse result if the given expression matches, null otherwise.
	 */
	private Result matchPattern(ExpressionInfo info, int firstPart, byte[] input, int pos) {
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
				Result[] potentialInputs = parse(input, pos, ((PatternPart.Input) part).getTypes());
				
				// Evaluate whether or not we can parse parts of this expression
				// AFTER this input, should it be used
				for (Result result : potentialInputs) {
					Result after = matchPattern(info, i + 1, input, result.getEnd());
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
		return new Result(node, pos);
	}

}
