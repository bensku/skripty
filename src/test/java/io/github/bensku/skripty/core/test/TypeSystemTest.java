package io.github.bensku.skripty.core.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.github.bensku.skripty.core.flow.ScopeEntry;
import io.github.bensku.skripty.core.type.SkriptType;
import io.github.bensku.skripty.core.type.TypeSystem;

public class TypeSystemTest {

	@Test
	public void defaultTypes() {
		TypeSystem types = new TypeSystem();
		assertEquals(SkriptType.VOID, types.resolve("void"));
		assertEquals(ScopeEntry.TYPE, types.resolve("scope_entry"));
	}
	
	@Test
	public void register() {
		TypeSystem types = new TypeSystem();
		SkriptType foo = SkriptType.create("foo.bar.Baz");
		types.registerType("foo", foo);
		assertEquals(foo, types.resolve("foo"));
	}
	
	public static class TestTypes {
		
		// Should register
		public static final SkriptType FIRST = SkriptType.create("sample.First");
		public static final SkriptType SECOND = SkriptType.create("sample.Second");
		public static SkriptType THIRD = SkriptType.create("sample.Third");
		
		// Should NOT register
		public final SkriptType FOURTH = SkriptType.create("sample.Fourth"); // Not static
		public static final Object FIFTH = new Object(); // Not a type
	}
	
	@Test
	public void registerClass() {
		TypeSystem types = new TypeSystem();
		types.registerTypes(TestTypes.class);
		
		assertEquals(TestTypes.FIRST, types.resolve("first"));
		assertEquals(TestTypes.SECOND, types.resolve("second"));
		assertEquals(TestTypes.THIRD, types.resolve("third"));
		
		assertThrows(IllegalArgumentException.class, () -> types.resolve("fourth"));
		assertThrows(IllegalArgumentException.class, () -> types.resolve("fifth"));
	}
}
