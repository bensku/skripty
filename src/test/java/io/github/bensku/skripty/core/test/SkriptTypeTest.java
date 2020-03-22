package io.github.bensku.skripty.core.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.bensku.skripty.core.type.SkriptType;

public class SkriptTypeTest {

	@Test
	public void concrete() {
		SkriptType.Concrete type = SkriptType.create(String.class);
		assertEquals(type, type.materialize());
		assertEquals(String.class, type.getBackingClass());
		assertFalse(type.isList());
		assertEquals(type.listOf(), type.listOf());
	}
	
	private void checkVirtual(Class<?> c) throws ClassNotFoundException {
		SkriptType.Virtual type = SkriptType.create(c.getName());
		assertEquals(c, type.materialize().getBackingClass());
		assertFalse(type.isList());
		assertEquals(type, type.materialize()); // Identity is kept when materializing
	}
	
	@Test
	public void virtual() throws ClassNotFoundException {
		checkVirtual(String.class);
		checkVirtual(void.class);
		checkVirtual(byte.class);
		checkVirtual(short.class);
		checkVirtual(char.class);
		checkVirtual(int.class);
		checkVirtual(long.class);
		checkVirtual(float.class);
		checkVirtual(double.class);
	}
	
	@Test
	public void listOf() throws ClassNotFoundException {
		assertTrue(SkriptType.create(String.class).listOf().isList());
		assertTrue(SkriptType.create(String.class.getName()).listOf().materialize().isList());
	}
}
