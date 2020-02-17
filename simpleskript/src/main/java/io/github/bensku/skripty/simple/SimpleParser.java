package io.github.bensku.skripty.simple;


import io.github.bensku.skripty.core.ScriptBlock;
import io.github.bensku.skripty.core.expression.Expression;
import io.github.bensku.skripty.core.expression.ExpressionRegistry;
import io.github.bensku.skripty.parser.expression.ExpressionLayer;
import io.github.bensku.skripty.parser.expression.ExpressionParser;
import io.github.bensku.skripty.parser.expression.LiteralParser;
import io.github.bensku.skripty.parser.script.BlockParser;
import io.github.bensku.skripty.parser.script.Scope;
import io.github.bensku.skripty.parser.script.ScopeRegistry;
import io.github.bensku.skripty.parser.script.SectionParser;
import io.github.bensku.skripty.parser.script.SourceNode;
import io.github.bensku.skripty.simple.expr.ExprCrash;
import io.github.bensku.skripty.simple.expr.ExprEquals;
import io.github.bensku.skripty.simple.expr.ExprPrint;
import io.github.bensku.skripty.simple.expr.ExprRunnerState;
import io.github.bensku.skripty.simple.expr.ExprTime;
import io.github.bensku.skripty.simple.literal.StringParser;
import io.github.bensku.skripty.simple.scope.ScopeIf;

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
		ExpressionLayer expressions = ExpressionLayer.forAnnotatedRegistry(expressions());
		ExpressionParser exprParser = new ExpressionParser(literalParsers, expressions);
		
		ExpressionRegistry scopeExprs = new ExpressionRegistry();
		Expression scopeIf = scopeExprs.makeCallable(SimpleTypes.class, new ScopeIf());
		
		ScopeRegistry scopes = new ScopeRegistry(scopeExprs);		
		Scope defaultScope = new Scope(new ExpressionParser(literalParsers, expressions,
				ExpressionLayer.forAnnotatedRegistry(scopeExprs)), scopes, exprParser);
		
		scopes.register(scopeIf, defaultScope);
		
		this.blockParser = new BlockParser(defaultScope);
	}
	
	private LiteralParser[] literalParsers() {
		return new LiteralParser[] {new StringParser()};
	}
	
	private ExpressionRegistry expressions() {
		ExpressionRegistry exprs = new ExpressionRegistry();
		Class<?> types = SimpleTypes.class;
		exprs.makeCallable(types, new ExprCrash());
		exprs.makeCallable(types, new ExprEquals());
		exprs.makeCallable(types, new ExprPrint());
		exprs.makeCallable(types, new ExprRunnerState());
		exprs.makeCallable(types, new ExprTime());
		
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
