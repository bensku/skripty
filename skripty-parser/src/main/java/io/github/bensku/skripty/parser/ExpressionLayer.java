package io.github.bensku.skripty.parser;

import java.util.Collection;

import io.github.bensku.skripty.core.expression.Expression;
import io.github.bensku.skripty.parser.pattern.Pattern;
import io.github.bensku.skripty.parser.pattern.PatternPart;

/**
 * A layer contains expression definitions ready to be used by the parser.
 * Multiple layers can be used to e.g. support reloading expression syntaxes
 * runtime.
 *
 */
public class ExpressionLayer {

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
		this.byFirstPart = new RadixTree<>();
		this.bySecondPart = new RadixTree<>();
	}
	
	/**
	 * Registers a pattern for an expression.
	 * @param expression An expression.
	 * @param pattern Pattern that can be used for it.
	 */
	public void register(Expression expression, Pattern pattern) {
		PatternPart first = pattern.partAt(0);
		PatternPart second = pattern.partAt(1);
		
		ExpressionInfo info = new ExpressionInfo(pattern, expression);
		if (first instanceof PatternPart.Literal) {
			byFirstPart.put(((PatternPart.Literal) first).getText(), info);
		} else {
			assert second instanceof PatternPart.Literal : "first or second should be literal";
			bySecondPart.put(((PatternPart.Literal) second).getText(), info);
		}
	}
	
	public Collection<ExpressionInfo> lookupFirst(byte[] input, int start) {
		return byFirstPart.get(input, start);
	}
	
	public Collection<ExpressionInfo> lookupSecond(byte[] input, int start) {
		return bySecondPart.get(input, start);
	}
}
