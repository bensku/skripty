package io.github.bensku.skripty.parser.expression;

import java.util.Arrays;

import io.github.bensku.skripty.core.AstNode;
import io.github.bensku.skripty.core.SkriptType;
import io.github.bensku.skripty.core.expression.InputType;
import io.github.bensku.skripty.parser.pattern.Pattern;
import io.github.bensku.skripty.parser.pattern.PatternPart;
import io.github.bensku.skripty.parser.util.ArrayHelpers;
import io.github.bensku.skripty.parser.util.RadixTree;

/**
 * Parses expressions into {@link AstNode}s.
 *
 */
public class ExpressionParser {
	
	/**
	 * Parsers with this flag ignore {@link SkriptType types}. The results
	 * will not be compilable, but could be used to produce better error
	 * messages after compiling with types has failed.
	 */
	public static final int IGNORE_TYPES = 1;
	
	/**
	 * Temporary limit for amount of parse results. TODO do not use fixed-size arrays
	 */
	private static final int PARSE_MAX_RESULTS = 128;

	/**
	 * Parser flags.
	 */
	private final int flags;
	
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

	public ExpressionParser(LiteralParser[] literalParsers, ExpressionLayer... expressions) {
		this(0, literalParsers, expressions);
	}
	
	private ExpressionParser(int flags, LiteralParser[] literalParsers, ExpressionLayer... expressions) {
		this.flags = flags;
		this.literalParsers = literalParsers;
		this.expressions = expressions;
	}
	
	/**
	 * Creates a copy of this parser with given flags.
	 * @param newFlags Expression parser flags.
	 * @return A new expression parser.
	 */
	public ExpressionParser withFlags(int newFlags) {
		return new ExpressionParser(newFlags, literalParsers, expressions);
	}
	
	/**
	 * Checks if this parser has a flag.
	 * @param flag Flag to check for.
	 * @return
	 */
	public boolean hasFlag(int flag) {
		return (flags & flag) != 0;
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
		
		public SkriptType getReturnType() {
			return node.getReturnType();
		}

	}
	
	/**
	 * Attempts to parse given input string.
	 * @param state Parser state.
	 * @param input Bytes of UTF-8 encoded input string.
	 * @param start Where to start parsing from in the input array.
	 * @param types Accepted return types of parsed expressions.
	 * @return The parse results, or an empty array if parsing failed.
	 */
	public Result[] parse(ParserState state, byte[] input, int start, SkriptType... types) {
		Result[] tempResults = new Result[PARSE_MAX_RESULTS]; // TODO try to guess result count instead
		int resultCount = 0;
		
		// Try literal parsing first
		for (LiteralParser parser : literalParsers) {
			LiteralParser.Result literal = parser.parse(state, input, start);
			if (literal != null) { // This is a literal!
				Result result = new Result(literal.getNode(), literal.getEnd());
				if (hasFlag(IGNORE_TYPES) || ArrayHelpers.contains(types, result.getReturnType())) {
					tempResults[resultCount++] = result;
				}
				
				// Even though result is literal, it could be used as input to something else
				resultCount = wrapAsFirstInput(state, input, tempResults, resultCount, result, types);
			}
		}
		
		// Search expressions from each layer
		for (ExpressionLayer layer : expressions) {
			Result[] results = parseFirst(state, layer, input, start);
			for (Result result : results) {
				if (result == null) {
					break; // Only nulls after this
				}
				
				// Add this result if it is of correct type
				if (hasFlag(IGNORE_TYPES) || ArrayHelpers.contains(types, result.getReturnType())) {
					tempResults[resultCount++] = result;
				}
				
				// Try using it as first input to expressions
				resultCount = wrapAsFirstInput(state, input, tempResults, resultCount, result, types);
			}
		}
		
		// Ensure that the returned array has no trailing nulls
		Result[] allResults = new Result[resultCount];
		System.arraycopy(tempResults, 0, allResults, 0, resultCount);
		
		return allResults;
	}
	
	/**
	 * Attempts to wrap an expression as first input to another expression.
	 * This is done recursively as long as parsing against input succeeds.
	 * @param state Parser state.
	 * @param input Input string.
	 * @param out Array where we write results.
	 * @param resultCount Current result count.
	 * @param original Original parse result with expression we'll try to wrap.
	 * @param types Accepted return types of the resulting expressions.
	 * @return New result count in out array.
	 */
	private int wrapAsFirstInput(ParserState state, byte[] input, Result[] out, int resultCount, Result original, SkriptType[] types) {
		for (ExpressionLayer layer : expressions) {
			Result[] secondResults = parseSecond(state, layer, original.getNode(), input, original.getEnd());
			for (Result result : secondResults) {
				if (result == null) {
					break;
				}
				
				// If this is of correct type, add it to results
				if (hasFlag(IGNORE_TYPES) || ArrayHelpers.contains(types, result.getReturnType())) {
					out[resultCount++] = result;
				}
				
				// Check if that could be used as first input to something else
				resultCount = wrapAsFirstInput(state, input, out, resultCount, result, types);
			}
		}
		return resultCount;
	}
	
	/**
	 * Parses all matching expressions by using input as a key to their first
	 * parts.
	 * @param state Parser state.
	 * @param layer Layer to query expressions from.
	 * @param input Input to parse.
	 * @param start Index of byte where to start parsing from in input.
	 * @return Parse results, or an empty array if the input cannot be parsed
	 * in any way.
	 */
	private Result[] parseFirst(ParserState state, ExpressionLayer layer, byte[] input, int start) {
		ExpressionInfo[] candidates = layer.lookupFirst(input, start);
		Result[] results = new Result[candidates.length];
		int resultCount = 0;
		
		// Go through candidate expressions, find those that might match
		for (int i = 0; i < candidates.length; i++) {
			ExpressionInfo info = candidates[i];

			// Current position (index) in input array
			// TODO maybe something less hacky for getting current position?
			int pos = start + ((PatternPart.Literal) info.getPattern().partAt(0)).getText().length;
			
			// Try to match pattern of the candidate
			// Success or a failure, we'll return that (result or null) to caller
			Result result = matchPattern(state, info, 1, input, pos);
			if (result != null) {
				results[resultCount++] = result;
			}
		}
		return results;
	}
	
	/**
	 * Parses all matching expressions by using input as a key to their second
	 * parts. The first parts must be inputs that accept the given type.
	 * @param state Parser state.
	 * @param layer Layer to query expressions from.
	 * @param firstNode First input node.
	 * @param input Input to parse.
	 * @param start Index of byte where to start parsing from in input.
	 * @return Parse results, or an empty array if the input cannot be parsed
	 * in any way.
	 */
	private Result[] parseSecond(ParserState state, ExpressionLayer layer, AstNode firstNode, byte[] input, int start) {
		ExpressionInfo[] candidates = layer.lookupSecond(input, start);
		Result[] results = new Result[candidates.length];
		int resultCount = 0;
		
		// Go through candidate expressions, find those that might match
		for (int i = 0; i < candidates.length; i++) {
			ExpressionInfo info = candidates[i];
			Pattern pattern = info.getPattern();
			
			// Filter based on return type of expression we already have
			int inputSlot = ((PatternPart.Input) pattern.partAt(0)).getSlot();
			InputType inputType = info.getExpression().getInputType(inputSlot);
			if (!hasFlag(IGNORE_TYPES) && !ArrayHelpers.contains(inputType.getTypes(), firstNode.getReturnType())) {
				continue; // Doesn't accept our type as argument
			}

			// Current position (index) in input array
			// TODO maybe something less hacky for getting current position?
			int pos = start + ((PatternPart.Literal) info.getPattern().partAt(1)).getText().length;
			
			// Try to match pattern of the candidate
			// Success or a failure, we'll return that (result or null) to caller
			Result result = matchPattern(state, info, 2, input, pos);
			if (result != null) {
				// Populate input corresponding to first pattern part
				((AstNode.Expr) result.getNode()).getInputs()[inputSlot] = firstNode;
				
				results[resultCount++] = result;
			}
		}
		return results;
	}
	
	/**
	 * Matches pattern of an expression against input.
	 * @param state Parser state.
	 * @param info Expression info. This references both the pattern and
	 * expression.
	 * @param firstPart Index of first pattern part that we should evaluate.
	 * This is used to avoid evaluating parts twice when a they've been already
	 * matched by e.g {@link RadixTree the expression tree}.
	 * @param input Input (UTF-8) bytes to match against.
	 * @param pos Starting position in the input.
	 * @return A parse result if the given expression matches, null otherwise.
	 */
	private Result matchPattern(ParserState state, ExpressionInfo info, int firstPart, byte[] input, int pos) {
		AstNode.Expr node = new AstNode.Expr(info.getExpression());
		
		// Initially empty array of this candidate's inputs
		AstNode[] inputs = node.getInputs();
		
		// Pattern of candidate expression; we match input against this
		Pattern pattern = info.getPattern();
		
		// Match pattern part-by-part against input
		for (int i = firstPart; i < pattern.length(); i++) {
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
				int inputSlot = ((PatternPart.Input) part).getSlot();
				
				// Get potential inputs
				InputType inputType = info.getExpression().getInputType(inputSlot);
				Result[] potentialInputs = parse(state, input, pos, inputType.getTypes());
				
				// Evaluate whether or not we can parse parts of this expression
				// AFTER this input, should it be used
				for (Result result : potentialInputs) {
					Result after = matchPattern(state, info, i + 1, input, result.getEnd());
					if (after != null && after.getEnd() > pos) { // Doesn't conflict with this expression
						// Copy inputs parsed recursively after current one here
						AstNode[] afterInputs = ((AstNode.Expr) after.getNode()).getInputs();
						for (int j = inputSlot + 1; j < inputs.length; j++) {
							inputs[j] = afterInputs[j];
						}
						
						// Assign this as input
						inputs[inputSlot] = result.getNode();
						// Recursive matchPattern() call has set the subsequent inputs (if any) for us
						pos = after.getEnd(); // Skip ahead what was recursively parsed
					}
				}
				
				if (inputs[inputSlot] == null) {
					// Subsequent parts of this expression were not parseable with potential inputs
					// (or maybe there were no potential inputs at all)
					return null; // Failed to parse this candidate!
				}
				break; // Latter parts, if any, have been recursively processed
			}
		}
		
		return new Result(node, pos);
	}

}
