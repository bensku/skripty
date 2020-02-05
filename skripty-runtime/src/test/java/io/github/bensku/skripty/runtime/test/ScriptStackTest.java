package io.github.bensku.skripty.runtime.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.github.bensku.skripty.runtime.ScriptStack;

public class ScriptStackTest {

	@Test
	public void stackOps() {
		ScriptStack stack = new ScriptStack(64);
		Object o1 = new Object();
		stack.push(o1);
		assertEquals(o1, stack.peek());
		assertEquals(o1, stack.pop());
	}
}
