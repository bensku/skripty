package io.github.bensku.skripty.core.type;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import io.github.bensku.skripty.core.flow.ScopeEntry;

/**
 * Type system provides access to registered {@link SkriptType}s.
 *
 */
public class TypeSystem {

	/**
	 * Registered types.
	 */
	private final Map<String, SkriptType> types;
	
	public TypeSystem() {
		this.types = new HashMap<>();
		registerBaseTypes();
	}
	
	/**
	 * Registers internal types that MUST be present in all type systems.
	 */
	private void registerBaseTypes() {
		registerType("void", SkriptType.VOID);
		registerType("scope_entry", ScopeEntry.TYPE);
	}
	
	public SkriptType resolve(String id) {
		SkriptType type = types.get(id);
		if (type == null) {
			throw new IllegalArgumentException("type not found with id " + id);
		}
		return type;
	}
	
	/**
	 * Registers a single new type.
	 * @param id Type id.
	 * @param type Definition of the type.
	 * @throws IllegalArgumentException When a type with given id is already
	 * registered.
	 */
	public void registerType(String id, SkriptType type) {
		if (types.containsKey(id)) {
			throw new IllegalArgumentException("duplicate type with id " + id);
		}
		types.put(id, type);
	}
	
	/**
	 * Registers all types defined in the given class as static fields. Field
	 * names are converted to lower case and used as ids.
	 * @param source Source class.
	 */
	public void registerTypes(Class<?> source) {
		for (Field f : source.getDeclaredFields()) {
			if (Modifier.isStatic(f.getModifiers()) // Only static fields
					&& f.getType().equals(SkriptType.class)) { // That are type definitions
				// This is a type definition field
				if (!f.canAccess(null)) {
					throw new IllegalArgumentException("cannot access type " + f.getName());
				}
				try {
					registerType(f.getName().toLowerCase(), (SkriptType) f.get(null));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new AssertionError("field should be readable", e);
				}
			}
		}
	}
}
