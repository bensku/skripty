package io.github.bensku.skripty.parser.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import io.github.bensku.skripty.core.SkriptType;
import io.github.bensku.skripty.core.annotation.CallTarget;
import io.github.bensku.skripty.core.annotation.Inputs;
import io.github.bensku.skripty.core.annotation.Returns;
import io.github.bensku.skripty.core.expression.CallableExpression;
import io.github.bensku.skripty.core.expression.ConstantExpression;
import io.github.bensku.skripty.core.expression.ExpressionRegistry;
import io.github.bensku.skripty.parser.expression.ExpressionLayer;
import io.github.bensku.skripty.parser.pattern.Pattern;

@TestInstance(Lifecycle.PER_METHOD)
public class ExpressionLayerTest {

	private ExpressionRegistry registry = new ExpressionRegistry();
	
	@Test
	public void lookup() {
		ExpressionLayer layer = new ExpressionLayer();
		ConstantExpression nullExpr = registry.makeConstant(SkriptType.create(Object.class), null);
		layer.register(nullExpr, Pattern.create("null"));
		assertEquals(nullExpr, layer.lookupFirst("null".getBytes(StandardCharsets.UTF_8), 0)[0].getExpression());
	}
	
	// Two different types backed by same Java class
	public static final SkriptType VOID = SkriptType.VOID;
	public static final SkriptType BAR = SkriptType.create(String.class);
	
	@Inputs({})
	@io.github.bensku.skripty.parser.annotation.Pattern("foo")
	@Returns("void")
	public static class TestExpr {
		
		@CallTarget
		public String first() {
			return "foo";
		}
	}
	
	@Test
	public void annotatedLayer() {
		CallableExpression fooExpr = registry.makeCallable(getClass(), new TestExpr());
		ExpressionLayer layer = ExpressionLayer.forAnnotatedRegistry(registry);
		assertEquals(fooExpr, layer.lookupFirst("foo".getBytes(StandardCharsets.UTF_8), 0)[0].getExpression());
	}
}
