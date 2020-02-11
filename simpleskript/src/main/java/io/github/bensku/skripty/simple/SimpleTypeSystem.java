package io.github.bensku.skripty.simple;

import io.github.bensku.skripty.core.SkriptType;

public class SimpleTypeSystem {

	public static final SkriptType VOID = SkriptType.VOID;
	public static final SkriptType BOOLEAN = SkriptType.create(boolean.class);
	public static final SkriptType NUMBER = SkriptType.create(Number.class);
	public static final SkriptType TEXT = SkriptType.create(String.class);
	
	public static final SkriptType TIMESTAMP = SkriptType.create(long.class);
}
