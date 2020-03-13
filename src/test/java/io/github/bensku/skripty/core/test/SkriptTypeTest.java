package io.github.bensku.skripty.core.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.github.bensku.skripty.core.type.SkriptType;

public class SkriptTypeTest {

	@Test
	public void concrete() {
		SkriptType.Concrete type = SkriptType.create(String.class);
		assertEquals(type, type.materialize());
		assertEquals(String.class, type.getBackingClass());
	}
	
	private void checkVirtual(Class<?> c) throws ClassNotFoundException {
		SkriptType.Virtual type = SkriptType.create(c.getName());
		assertEquals(c, type.materialize().getBackingClass());
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
}
