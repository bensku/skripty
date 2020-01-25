package io.github.bensku.skripty.parser;

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
}
