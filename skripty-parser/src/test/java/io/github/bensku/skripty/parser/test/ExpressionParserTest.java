package io.github.bensku.skripty.parser.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import io.github.bensku.skripty.core.AstNode;
import io.github.bensku.skripty.core.SkriptType;
import io.github.bensku.skripty.core.expression.Expression;
import io.github.bensku.skripty.core.expression.ExpressionRegistry;
import io.github.bensku.skripty.core.expression.InputType;
import io.github.bensku.skripty.parser.expression.ExpressionLayer;
import io.github.bensku.skripty.parser.expression.ExpressionParser;
import io.github.bensku.skripty.parser.expression.LiteralParser;
import io.github.bensku.skripty.parser.pattern.Pattern;

@TestInstance(Lifecycle.PER_METHOD) // Clean state for every test, just in case...
public class ExpressionParserTest {
	
	private static final SkriptType VOID = SkriptType.VOID;
	private static final SkriptType TEXT = SkriptType.create(String.class);

	private ExpressionParser parser;
	
	private Expression constantStr;
	private Expression exprConsume;
	private Expression exprFirst;
	private Expression exprSecond;
	
	@BeforeEach
	public void initExpressions() {
		ExpressionRegistry registry = new ExpressionRegistry();
		constantStr = registry.makeConstant(TEXT, "Hello, world!");
		exprConsume = registry.makeCallable(this)
				.inputTypes(new InputType(true, TEXT))
				.returnType(VOID)
				.callTargets()
				.create();
		exprFirst = registry.makeCallable(this)
				.inputTypes(new InputType(true, TEXT))
				.returnType(TEXT)
				.callTargets()
				.create();
		exprSecond = registry.makeCallable(this)
				.inputTypes(new InputType(true, TEXT))
				.returnType(TEXT)
				.callTargets()
				.create();
	}
	
	@BeforeEach
	public void initParser() {
		LiteralParser[] literalParsers = new LiteralParser[] {};
		
		ExpressionLayer basicLayer = new ExpressionLayer();
		basicLayer.register(constantStr, Pattern.create("string constant"));
		basicLayer.register(exprConsume, Pattern.create("consume ", 0));
		basicLayer.register(exprFirst, Pattern.create("first-expr ", 0));
		basicLayer.register(exprSecond, Pattern.create(0, " second-expr"));
		ExpressionLayer[] layers = new ExpressionLayer[] {basicLayer};
		
		parser = new ExpressionParser(literalParsers, layers);
	}
	
	private void parseAll(String input, Expression expected) {
		byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
		ExpressionParser.Result[] results = parser.parse(bytes, 0, new SkriptType[] {expected.getReturnType()});
		
		for (ExpressionParser.Result result : results) {
			if (bytes.length == result.getEnd()) {
				AstNode.Expr node = (AstNode.Expr) result.getNode();
				assertEquals(expected, node.getExpression());
				return;
			}
		}
		
		assertTrue(false, "results did not contain expected expression");

	}
	
	@Test
	public void simpleConstant() {
		parseAll("string constant", constantStr);
	}
	
	@Test
	public void simpleCallable() {
		parseAll("consume string constant", exprConsume);
	}
	
	@Test
	public void recursiveExprs() {
		parseAll("first-expr first-expr string constant", exprFirst);
	}
	
	@Test
	public void reverseSay() {
		parseAll("string constant second-expr", exprSecond);
		
		// Things below are technically ambiguous
		// Current parser prefers to consume as much characters as possible
		// If this changes (e.g. by matchPattern() redesign), these WILL need to be updated
		// say (greeting shouted)
		parseAll("consume string constant second-expr", exprConsume);
		// say (greeting shouted shouted)
		parseAll("consume string constant second-expr second-expr", exprConsume);
	}
}
