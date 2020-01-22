package io.github.bensku.skripty.parser.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import io.github.bensku.skripty.parser.RadixTree;

@TestInstance(Lifecycle.PER_METHOD)
public class ExpressionTreeTest {

	private final RadixTree<Object> tree = new RadixTree<>();
	
	@Test
	public void simplePut() {
		Object marker = new Object();
		tree.put("abc", marker);
		assertEquals(marker, tree.get("abc").get(0));
	}
	
	@Test
	public void branchingPut() {
		Object marker1 = new Object();
		tree.put("alpha beta".getBytes(StandardCharsets.UTF_8), marker1);
		assertEquals(marker1, tree.get("alpha beta").get(0));
		
		Object marker2 = new Object();
		tree.put("alpha gamma".getBytes(StandardCharsets.UTF_8), marker2);
		assertEquals(marker2, tree.get("alpha gamma").get(0));
		assertEquals(marker1, tree.get("alpha beta").get(0));
	}
}
