package io.github.bensku.skripty.parser.script;

import java.nio.charset.StandardCharsets;

import io.github.bensku.skripty.core.AstNode;
import io.github.bensku.skripty.core.SkriptType;
import io.github.bensku.skripty.core.flow.ScopeEntry;
import io.github.bensku.skripty.parser.expression.ExpressionParser;

/**
 * Scopes are types of sections/blocks found in scripts.
 *
 */
public class Scope {

	/**
	 * Parser for titles of inner scopes.
	 */
	private final ExpressionParser scopeParser;
	
	/**
	 * Registry of inner scopes.
	 */
	private final ScopeRegistry scopeRegistry;
	
	/**
	 * Parser for statements in this scope.
	 */
	private final ExpressionParser statementParser;
	
	public Scope(ExpressionParser scopeParser, ScopeRegistry scopeRegistry, ExpressionParser statementParser) {
		this.scopeParser = scopeParser;
		this.scopeRegistry = scopeRegistry;
		this.statementParser = statementParser;
	}
	
	public static class ParseResult {
		
		/**
		 * Node of title expression of the inner scope.
		 */
		private final AstNode title;

		/**
		 * The inner scope definition.
		 */
		private final Scope scope;

		private ParseResult(AstNode title, Scope scope) {
			this.title = title;
			this.scope = scope;
		}

		public AstNode getTitle() {
			return title;
		}

		public Scope getScope() {
			return scope;
		}
		
	}
	
	public ParseResult parseScope(String title) {
		ExpressionParser.Result[] results = scopeParser.parse(title.getBytes(StandardCharsets.UTF_8), 0, ScopeEntry.TYPE);
		if (results.length == 0) {
			// TODO error handling :)
		}
		AstNode node = results[0].getNode();
		return new ParseResult(results[0].getNode(), scopeRegistry.resolve(((AstNode.Expr) node).getExpression()));
	}
	
	public AstNode parseStatement(String statement) {
		ExpressionParser.Result[] results = statementParser.parse(statement.getBytes(StandardCharsets.UTF_8), 0, SkriptType.VOID);
		if (results.length == 0) {
			// TODO error handling :)
		}
		return results[0].getNode();
	}
}
