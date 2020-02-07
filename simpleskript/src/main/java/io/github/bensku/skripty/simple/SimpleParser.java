package io.github.bensku.skripty.simple;


import io.github.bensku.skripty.core.ScriptBlock;
import io.github.bensku.skripty.core.expression.ExpressionRegistry;
import io.github.bensku.skripty.parser.expression.ExpressionLayer;
import io.github.bensku.skripty.parser.expression.ExpressionParser;
import io.github.bensku.skripty.parser.expression.LiteralParser;
import io.github.bensku.skripty.parser.script.BlockParser;
import io.github.bensku.skripty.parser.script.Scope;
import io.github.bensku.skripty.parser.script.ScopeRegistry;
import io.github.bensku.skripty.parser.script.SectionParser;
import io.github.bensku.skripty.parser.script.SourceNode;

/**
 * Simply parses scripts by using the lower level APIs from other components.
 *
 */
public class SimpleParser {
	
	// Script-level parsers
	private final SectionParser sectionParser;
	private final BlockParser blockParser;
		
	public SimpleParser() {
		this.sectionParser = new SectionParser();
		
		LiteralParser[] literalParsers = literalParsers();
		ExpressionParser exprParser = new ExpressionParser(literalParsers,
				ExpressionLayer.forAnnotatedRegistry(expressions()));
		
		ScopeRegistry scopes = scopes();
		Scope rootScope = new Scope(new ExpressionParser(literalParsers,
				ExpressionLayer.forAnnotatedRegistry(scopes.getExpressions())), scopes,
				exprParser);
		this.blockParser = new BlockParser(rootScope);
	}
	
	private LiteralParser[] literalParsers() {
		return new LiteralParser[] {};
	}
	
	private ScopeRegistry scopes() {
		ExpressionRegistry exprs = new ExpressionRegistry();
		
		ScopeRegistry scopes = new ScopeRegistry(exprs);
		
		return scopes;
	}
	
	private ExpressionRegistry expressions() {
		ExpressionRegistry exprs = new ExpressionRegistry();
		
		
		return exprs;
	}

	/**
	 * Parses a script.
	 * @param script Script source code.
	 * @return A script block that has been parsed and can be compiled.
	 */
	public ScriptBlock parse(String script) {
		SourceNode.Section section = sectionParser.parse(script);
		return blockParser.parse(section);
	}
}
