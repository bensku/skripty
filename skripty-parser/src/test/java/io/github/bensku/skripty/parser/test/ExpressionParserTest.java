package io.github.bensku.skripty.parser.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import io.github.bensku.skripty.core.AstNode;
import io.github.bensku.skripty.core.expression.Expression;
import io.github.bensku.skripty.core.expression.ExpressionRegistry;
import io.github.bensku.skripty.core.expression.InputType;
import io.github.bensku.skripty.core.type.SkriptType;
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
	private Expression exprInputs;
	private Expression exprInputs2;
	private Expression exprLiteralParts;
	
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
		exprInputs = registry.makeCallable(this)
				.inputTypes(new InputType(true, TEXT), new InputType(true, TEXT))
				.returnType(TEXT)
				.callTargets()
				.create();
		exprInputs2 = registry.makeCallable(this)
				.inputTypes(new InputType(true, TEXT), new InputType(true, TEXT))
				.returnType(TEXT)
				.callTargets()
				.create();
		exprLiteralParts = registry.makeCallable(this)
				.inputTypes(new InputType(true, TEXT))
				.returnType(TEXT)
				.callTargets()
				.create();
	}
	
	@BeforeEach
	public void initParser() {
		LiteralParser[] literalParsers = new LiteralParser[] {
				(state, input, start) -> {
					byte[] wanted = "literal".getBytes(StandardCharsets.UTF_8);
					for (int i = 0; i < wanted.length; i++) {
						int inputIndex = start + i;
						if (inputIndex == input.length) {
							return null; // Ran out of input
						} else if (input[inputIndex] != wanted[i]) {
							return null; // Didn't match the string we wanted it to match
						}
					}
					return new LiteralParser.Result(new AstNode.Literal(TEXT, "this is literal"), start + wanted.length);
				}
		};
		
		ExpressionLayer basicLayer = new ExpressionLayer();
		basicLayer.register(constantStr, Pattern.create("string constant"));
		basicLayer.register(exprConsume, Pattern.create("consume ", 0));
		basicLayer.register(exprFirst, Pattern.create("first-expr ", 0));
		basicLayer.register(exprSecond, Pattern.create(0, " second-expr"));
		basicLayer.register(exprInputs, Pattern.create(0, " input ", 1));
		basicLayer.register(exprInputs2, Pattern.create("two inputs ", 0, 1));
		basicLayer.register(exprLiteralParts, Pattern.create("first ", 0, " second"));
		ExpressionLayer[] layers = new ExpressionLayer[] {basicLayer};
		
		parser = new ExpressionParser(literalParsers, layers);
	}
	
	private void parseAll(String input, Expression expected) {
		byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
		List<ExpressionParser.Result> results = parser.parse(null, bytes, 0, expected.getReturnType());
		
		for (ExpressionParser.Result result : results) {
			if (bytes.length == result.getEnd()) {
				AstNode.Expr node = (AstNode.Expr) result.getNode();
				assertEquals(expected, node.getExpression());
				return;
			}
		}
		
		assertTrue(false, "results did not contain expected expression");
	}
	
	private void parseAll(String input, SkriptType type, Object literalValue) {
		byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
		List<ExpressionParser.Result> results = parser.parse(null, bytes, 0, type);
		
		for (ExpressionParser.Result result : results) {
			if (bytes.length == result.getEnd()) {
				AstNode.Literal node = (AstNode.Literal) result.getNode();
				assertEquals(literalValue, node.getValue());
				return;
			}
		}
		
		assertTrue(false, "results did not contain expected node");
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
	public void reversed() {
		parseAll("string constant second-expr", exprSecond);
		
		// Things below are technically ambiguous
		// Current parser prefers to consume as much characters as possible
		// If this changes (e.g. by matchPattern() redesign), these WILL need to be updated
		// say (greeting shouted)
		parseAll("consume string constant second-expr", exprConsume);
		// say (greeting shouted shouted)
		parseAll("consume string constant second-expr second-expr", exprConsume);
	}
	
	@Test
	public void deepRecursive() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 100; i++) {
			sb.append("first-expr ");
		}
		sb.append("string constant");
		for (int i = 0; i < 100; i++) {
			sb.append(" second-expr");
		}
	}
	
	@Test
	public void twoInputs() {
		parseAll("string constant input string constant", exprInputs);
		parseAll("consume string constant input string constant", exprConsume); // simpleskript helped find this
		
		// Space between inputs would be a literal part, in this case there isn't one
		parseAll("two inputs string constantstring constant", exprInputs2);
		parseAll("consume two inputs string constantstring constant", exprConsume);
	}
	
	@Test
	public void withoutTypes() {
		byte[] expr = "consume consume string constant".getBytes(StandardCharsets.UTF_8);
		List<ExpressionParser.Result> results = parser.parse(null, expr, 0, new SkriptType[] {VOID});
		assertTrue(results.isEmpty()); // Should fail parsing, types are a mess
		
		// Patterns are matched, types are just incompatible; what about without them?
		ExpressionParser typeless = parser.withFlags(ExpressionParser.IGNORE_TYPES);
		assertTrue(typeless.hasFlag(ExpressionParser.IGNORE_TYPES));
		List<ExpressionParser.Result> results2 = typeless.parse(null, expr, 0, new SkriptType[] {VOID});
		assertEquals(exprConsume, ((AstNode.Expr) results2.get(0).getNode()).getExpression());
	}
	
	@Test
	public void literalParts() {
		parseAll("first string constant second", exprLiteralParts);
		parseAll("consume first string constant second-expr second", exprConsume);
	}
	
	@Test
	public void simpleLiteral() {
		parseAll("literal", TEXT, "this is literal");
		parseAll("consume literal", exprConsume);
		parseAll("consume first literal second-expr second", exprConsume);
	}
}
