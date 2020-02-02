package io.github.bensku.skripty.parser.script;

import java.nio.charset.StandardCharsets;

import io.github.bensku.skripty.core.AstNode;
import io.github.bensku.skripty.core.SkriptType;
import io.github.bensku.skripty.parser.expression.ExpressionParser;

/**
 * Scopes are types of sections/blocks found in scripts.
 *
 */
public class Scope {

	/**
	 * Parser for titles of inner scopes.
	 */
	private ExpressionParser innerScopeParser;
	
	/**
	 * Parser for statements in this scope.
	 */
	private ExpressionParser statementParser;
	
	public AstNode parseScope(String title) {
		ExpressionParser.Result[] results = innerScopeParser.parse(title.getBytes(StandardCharsets.UTF_8), 0, SkriptType.VOID);
		if (results.length == 0) {
			// TODO error handling :)
		}
		return results[0].getNode();
	}
	
	public AstNode parseStatement(String statement) {
		ExpressionParser.Result[] results = statementParser.parse(statement.getBytes(StandardCharsets.UTF_8), 0, SkriptType.VOID);
		if (results.length == 0) {
			// TODO error handling :)
		}
		return results[0].getNode();
	}
}
