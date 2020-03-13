package io.github.bensku.skripty.simple;

import io.github.bensku.skripty.core.type.SkriptType;

public class SimpleTypes {
	
	public static final SkriptType VARIABLE = SkriptType.create(int.class); // int is slot index
	public static final SkriptType BOOLEAN = SkriptType.create(boolean.class);
	public static final SkriptType NUMBER = SkriptType.create(Number.class);
	public static final SkriptType TEXT = SkriptType.create(String.class);
	
	public static final SkriptType TIMESTAMP = SkriptType.create(long.class);
}
