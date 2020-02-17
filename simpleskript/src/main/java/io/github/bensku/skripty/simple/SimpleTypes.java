package io.github.bensku.skripty.simple;

import io.github.bensku.skripty.core.SkriptType;
import io.github.bensku.skripty.core.flow.ScopeEntry;

public class SimpleTypes {

	public static final SkriptType VOID = SkriptType.VOID;
	public static final SkriptType SCOPE_ENTRY = ScopeEntry.TYPE;
	
	public static final SkriptType BOOLEAN = SkriptType.create(boolean.class);
	public static final SkriptType NUMBER = SkriptType.create(Number.class);
	public static final SkriptType TEXT = SkriptType.create(String.class);
	
	public static final SkriptType TIMESTAMP = SkriptType.create(long.class);
}
