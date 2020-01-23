package io.github.bensku.skripty.parser.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import io.github.bensku.skripty.parser.RadixTree;

@TestInstance(Lifecycle.PER_METHOD)
public class RadixTreeTest {

	private final RadixTree<Object> tree = new RadixTree<>();
	
	@Test
	public void noBranches() {
		Object marker = new Object();
		tree.put("abc", marker);
		assertEquals(marker, tree.get("abc").get(0));
	}
	
	@Test
	public void simpleBranch() {
		Object marker1 = new Object();
		tree.put("alpha beta", marker1);
		assertEquals(marker1, tree.get("alpha beta").get(0));
		
		Object marker2 = new Object();
		tree.put("alpha gamma", marker2);
		assertEquals(marker2, tree.get("alpha gamma abcdefg").get(0));
		assertEquals(marker1, tree.get("alpha beta 12345").get(0));
	}
	
	@Test
	public void duplicateBranch() {
		Object marker1 = new Object();
		tree.put("alpha", marker1);
		
		Object marker2 = new Object();
		tree.put("beta", marker2);
		assertEquals(marker1, tree.get("alpha").get(0));
		assertEquals(marker2, tree.get("beta").get(0));
		
		Object marker3 = new Object();
		tree.put("gamma", marker3);
		
		assertEquals(marker1, tree.get("alpha").get(0));
		assertEquals(marker2, tree.get("beta").get(0));
		assertEquals(marker3, tree.get("gamma").get(0));
	}
	
	@Test
	public void manyValues() {
		Object marker1 = new Object();
		tree.put("alpha", marker1);
		Object marker2 = new Object();
		tree.put("alpha beta", marker2);
		Object marker3 = new Object();
		tree.put("alpha beta gamma", marker3);
		
		List<Object> results = tree.get("alpha beta gamma");
		assertTrue(results.contains(marker1));
		assertTrue(results.contains(marker2));
		assertTrue(results.contains(marker3));
		
		Object marker4 = new Object();
		tree.put("alpha gamma", marker4);
		List<Object> results2 = tree.get("alpha gamma");
		assertTrue(results2.contains(marker1));
		assertTrue(results2.contains(marker4));
		assertEquals(results, tree.get("alpha beta gamma"));
	}
}
