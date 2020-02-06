package io.github.bensku.skripty.parser.expression;

import io.github.bensku.skripty.core.expression.CallableExpression;
import io.github.bensku.skripty.core.expression.Expression;
import io.github.bensku.skripty.core.expression.ExpressionRegistry;
import io.github.bensku.skripty.parser.pattern.Pattern;
import io.github.bensku.skripty.parser.pattern.PatternPart;
import io.github.bensku.skripty.parser.util.RadixTree;

/**
 * A layer contains expression definitions ready to be used by the parser.
 * Multiple layers can be used to e.g. support reloading expression syntaxes
 * runtime.
 *
 */
public class ExpressionLayer {
	
	/**
	 * Creates an expression layer for expressions defined using the annotation
	 * based API. 
	 * @param registry Expression registry.
	 * @return An expression layer.
	 */
	public static ExpressionLayer forAnnotatedRegistry(ExpressionRegistry registry) {
		ExpressionLayer layer = new ExpressionLayer();
		registry.forEach(expr -> {
			if (expr instanceof CallableExpression) {
				Class<?> implClass = ((CallableExpression) expr).getInstance().getClass();
				io.github.bensku.skripty.parser.annotation.Pattern p = implClass
						.getAnnotation(io.github.bensku.skripty.parser.annotation.Pattern.class);
				if (p == null) {
					// TODO missing pattern, what do we do
					return; // Ignore for now
				}
				
				layer.register(expr, Pattern.parse(p.value()));
			}
		});
		return layer;
	}

	/**
	 * Expressions by their first parts. Those that do not start with a literal
	 * part are in {@link #bySecondPart} instead.
	 */
	private final RadixTree<ExpressionInfo> byFirstPart;
	
	/**
	 * Expressions by their second parts. This is used for expressions that
	 * begin with inputs.
	 */
	private final RadixTree<ExpressionInfo> bySecondPart;
	
	public ExpressionLayer() {
		this.byFirstPart = new RadixTree<>(ExpressionInfo.class);
		this.bySecondPart = new RadixTree<>(ExpressionInfo.class);
	}
	
	/**
	 * Registers a pattern for an expression.
	 * @param expression An expression.
	 * @param pattern Pattern that can be used for it.
	 * @throws IllegalArgumentException If the pattern is not valid for given
	 * expression, for example because it is missing inputs.
	 */
	public void register(Expression expression, Pattern pattern) {
		PatternPart first = pattern.partAt(0);
		
		// Validate that pattern contains all required inputs and doesn't have duplicate inputs
		boolean[] foundInputs = new boolean[expression.getInputCount()];
		for (int i = 0; i < pattern.length(); i++) {
			PatternPart part = pattern.partAt(i);
			if (part instanceof PatternPart.Input) {
				int inputSlot = ((PatternPart.Input) part).getSlot();
				if (foundInputs[inputSlot]) {
					throw new IllegalArgumentException("pattern has duplicate input for slot " + i);
				}
				foundInputs[inputSlot] = true;
			}
		}
		for (int i = 0; i < foundInputs.length; i++) {
			if (!foundInputs[i] && !expression.getInputType(i).isOptional()) {
				throw new IllegalArgumentException("pattern is missing required input for slot " + i);
			}
		}
		
		ExpressionInfo info = new ExpressionInfo(pattern, expression);
		if (first instanceof PatternPart.Literal) {
			byFirstPart.put(((PatternPart.Literal) first).getText(), info);
		} else {
			if (pattern.length() > 1) {
				PatternPart second = pattern.partAt(1);
				assert second instanceof PatternPart.Literal : "first or second should be literal";
				bySecondPart.put(((PatternPart.Literal) second).getText(), info);
			} else {
				throw new UnsupportedOperationException("expressions with no literal parts are not supported at the moment");
			}
		}
	}
	
	public ExpressionInfo[] lookupFirst(byte[] input, int start) {
		return byFirstPart.get(input, start);
	}
	
	public ExpressionInfo[] lookupSecond(byte[] input, int start) {
		return bySecondPart.get(input, start);
	}
}
