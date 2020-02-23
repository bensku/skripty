package io.github.bensku.skripty.runtime.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
		
		for (int i = 0; i < 10; i++) {
			stack.push(i); // Autoboxing
		}
		Object[] poked = stack.peek(10);
		Object[] popped = stack.pop(10);
		for (int i = 0; i < 10; i++) {
			assertEquals(i, poked[i]);
			assertEquals(i, popped[i]);
		}
		
		for (int i = 0; i < 10; i++) {
			stack.push(i); // Autoboxing
		}
		Object[] out = new Object[11];
		stack.popInto(out, 1, 10);
		assertNull(out[0]);
		for (int i = 0; i < 10; i++) {
			assertEquals(i, out[i + 1]);
		}
	}
}
