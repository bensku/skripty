package io.github.bensku.skripty.parser.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
import io.github.bensku.skripty.parser.ExpressionLayer;
import io.github.bensku.skripty.parser.ExpressionParser;
import io.github.bensku.skripty.parser.LiteralParser;
import io.github.bensku.skripty.parser.pattern.Pattern;

@TestInstance(Lifecycle.PER_METHOD) // Clean state for every test, just in case...
public class ExpressionParserTest {
	
	private final SkriptType stringType = SkriptType.create(String.class);

	private ExpressionParser parser;
	
	private Expression constantHello;
	private Expression exprSay;
	
	@BeforeEach
	public void initExpressions() {
		ExpressionRegistry registry = new ExpressionRegistry();
		constantHello = registry.makeConstant(stringType, "Hello, world!");
		exprSay = registry.makeCallable(this)
				.inputTypes(new InputType(true, stringType))
				.returnType(stringType) // TODO void return type
				.callTargets()
				.create();
	}
	
	@BeforeEach
	public void initParser() {
		LiteralParser[] literalParsers = new LiteralParser[] {};
		
		ExpressionLayer basicLayer = new ExpressionLayer();
		basicLayer.register(constantHello, Pattern.create("greeting"));
		basicLayer.register(exprSay, Pattern.create("say ", new InputType(false, stringType)));
		ExpressionLayer[] layers = new ExpressionLayer[] {basicLayer};
		
		parser = new ExpressionParser(literalParsers, layers);
	}
	
	private void parseAll(String input, Expression expected) {
		byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
		ExpressionParser.Result[] results = parser.parse(bytes, 0, new SkriptType[] {expected.getReturnType()});
		
		assertEquals(1, results.length);
		assertEquals(bytes.length, results[0].getEnd());
		AstNode.Expr node = (AstNode.Expr) results[0].getNode();
		assertEquals(expected, node.getExpression());
	}
	
	@Test
	public void simpleConstant() {
		parseAll("greeting", constantHello);
	}
	
	@Test
	public void simpleCallable() {
		parseAll("say greeting", exprSay);
	}
}
