package io.github.bensku.skripty.parser.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import io.github.bensku.skripty.parser.RadixTree;
import io.github.bensku.skripty.parser.util.ArrayHelpers;

@TestInstance(Lifecycle.PER_METHOD)
public class RadixTreeTest {

	private final RadixTree<Object> tree = new RadixTree<>();
	
	@Test
	public void noBranches() {
		Object marker = new Object();
		tree.put("abc", marker);
		assertEquals(marker, tree.get("abc")[0]);
	}
	
	@Test
	public void simpleBranch() {
		Object marker1 = new Object();
		tree.put("alpha beta", marker1);
		assertEquals(marker1, tree.get("alpha beta")[0]);
		
		Object marker2 = new Object();
		tree.put("alpha gamma", marker2);
		assertEquals(marker2, tree.get("alpha gamma abcdefg")[0]);
		assertEquals(marker1, tree.get("alpha beta 12345")[0]);
	}
	
	@Test
	public void duplicateBranch() {
		Object marker1 = new Object();
		tree.put("alpha", marker1);
		
		Object marker2 = new Object();
		tree.put("beta", marker2);
		assertEquals(marker1, tree.get("alpha")[0]);
		assertEquals(marker2, tree.get("beta")[0]);
		
		Object marker3 = new Object();
		tree.put("gamma", marker3);
		
		assertEquals(marker1, tree.get("alpha")[0]);
		assertEquals(marker2, tree.get("beta")[0]);
		assertEquals(marker3, tree.get("gamma")[0]);
	}
	
	@Test
	public void manyValues() {
		Object marker1 = new Object();
		tree.put("alpha", marker1);
		Object marker2 = new Object();
		tree.put("alpha beta", marker2);
		Object marker3 = new Object();
		tree.put("alpha beta gamma", marker3);
		
		Object[] results = tree.get("alpha beta gamma");
		assertTrue(ArrayHelpers.contains(results, marker1));
		assertTrue(ArrayHelpers.contains(results, marker2));
		assertTrue(ArrayHelpers.contains(results, marker3));
		
		Object marker4 = new Object();
		tree.put("alpha gamma", marker4);
		Object[] results2 = tree.get("alpha gamma");
		assertTrue(ArrayHelpers.contains(results2, marker1));
		assertTrue(ArrayHelpers.contains(results2, marker4));
		assertTrue(Arrays.equals(results, tree.get("alpha beta gamma")));
	}
}
