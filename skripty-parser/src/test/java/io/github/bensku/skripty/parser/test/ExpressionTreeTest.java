package io.github.bensku.skripty.parser.test;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import io.github.bensku.skripty.parser.ExpressionTree;

@TestInstance(Lifecycle.PER_METHOD)
public class ExpressionTreeTest {

	private final ExpressionTree tree = new ExpressionTree();
	
	@Test
	public void simplePut() {
		tree.put("abc".getBytes(StandardCharsets.UTF_8), null);
	}
	
	@Test
	public void branchingPut() {
		tree.put("alpha beta".getBytes(StandardCharsets.UTF_8), null);
		tree.put("alpha gamma".getBytes(StandardCharsets.UTF_8), null);
	}
}
