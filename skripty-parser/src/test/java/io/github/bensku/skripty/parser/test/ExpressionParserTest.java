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
	
	private final SkriptType stringType = SkriptType.create(String.class);

	private ExpressionParser parser;
	
	private Expression constantHello;
	private Expression exprSay;
	private Expression exprShouted;
	
	@BeforeEach
	public void initExpressions() {
		ExpressionRegistry registry = new ExpressionRegistry();
		constantHello = registry.makeConstant(stringType, "Hello, world!");
		exprSay = registry.makeCallable(this)
				.inputTypes(new InputType(true, stringType))
				.returnType(stringType)
				.callTargets()
				.create();
		exprShouted = registry.makeCallable(this)
				.inputTypes(new InputType(true, stringType))
				.returnType(stringType)
				.callTargets()
				.create();
	}
	
	@BeforeEach
	public void initParser() {
		LiteralParser[] literalParsers = new LiteralParser[] {};
		
		ExpressionLayer basicLayer = new ExpressionLayer();
		basicLayer.register(constantHello, Pattern.create("greeting"));
		basicLayer.register(exprSay, Pattern.create("say ", 0));
		basicLayer.register(exprShouted, Pattern.create(0, " shouted"));
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
		parseAll("greeting", constantHello);
	}
	
	@Test
	public void simpleCallable() {
		parseAll("say greeting", exprSay);
	}
	
	@Test
	public void recursiveSay() {
		parseAll("say say say greeting", exprSay);
	}
	
	@Test
	public void reverseSay() {
		parseAll("greeting shouted", exprShouted);
		
		// Things below are technically ambiguous
		// Current parser prefers to consume as much characters as possible
		// If this changes (e.g. by matchPattern() redesign), these WILL need to be updated
		// say (greeting shouted)
		parseAll("say greeting shouted", exprSay);
		// say (greeting shouted shouted)
		parseAll("say greeting shouted shouted", exprSay);
	}
}
