package io.github.bensku.skripty.parser.script;

import java.nio.charset.StandardCharsets;

import io.github.bensku.skripty.core.AstNode;
import io.github.bensku.skripty.core.SkriptType;
import io.github.bensku.skripty.core.flow.ScopeEntry;
import io.github.bensku.skripty.parser.expression.ExpressionParser;
import io.github.bensku.skripty.parser.expression.ParserState;
import io.github.bensku.skripty.parser.log.ParseResult;
import io.github.bensku.skripty.parser.log.ParserMessage;

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
	
	/**
	 * Represents an inner scope. Returned by {@link Scope#parseScope(String)}
	 * when parsing succeeds.
	 *
	 */
	public static class InnerScope {
		
		/**
		 * Node of title expression of the inner scope.
		 */
		private final AstNode.Expr title;

		/**
		 * The inner scope definition.
		 */
		private final Scope scope;

		private InnerScope(AstNode.Expr title, Scope scope) {
			this.title = title;
			this.scope = scope;
		}

		public AstNode.Expr getTitle() {
			return title;
		}

		public Scope getScope() {
			return scope;
		}
		
	}
	
	public ParseResult<InnerScope> parseScope(ParserState state, SourceNode.Statement title) {
		ExpressionParser.Result[] results = scopeParser.parse(state, title.getText().getBytes(StandardCharsets.UTF_8), 0, ScopeEntry.TYPE);
		if (results.length == 0) {
			ParserMessage error = ParserMessage.error("failed to parse scope").at(title, 0, title.length());
			return ParseResult.failure(error);
		}
		AstNode node = results[0].getNode();
		InnerScope scope = new InnerScope((AstNode.Expr) results[0].getNode(),
				scopeRegistry.resolve(((AstNode.Expr) node).getExpression()));
		return ParseResult.success(scope);
	}
	
	public ParseResult<AstNode.Expr> parseStatement(ParserState state, SourceNode.Statement statement) {
		byte[] bytes = statement.getText().getBytes(StandardCharsets.UTF_8);
		ExpressionParser.Result[] results = statementParser.parse(state, bytes, 0, SkriptType.VOID);
		if (results.length == 0) {
			ParserMessage error = ParserMessage.error("failed to parse statement").at(statement, 0, statement.length());
			return ParseResult.failure(error);
		}
		for (ExpressionParser.Result result : results) {
			if (result.getEnd() == bytes.length) {
				return ParseResult.success((AstNode.Expr) result.getNode());
			}
		}
		
		// TODO more detailed error
		ParserMessage error = ParserMessage.error("failed to parse statement").at(statement, 0, statement.length());
		return ParseResult.failure(error);
	}
}
