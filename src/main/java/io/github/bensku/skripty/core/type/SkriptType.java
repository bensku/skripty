package io.github.bensku.skripty.core.type;

/**
 * A type that is visible to scripts.
 *
 */
public abstract class SkriptType {
	
	/**
	 * A type that represents nothing (Java/JVM void).
	 */
	public static final SkriptType.Concrete VOID = create(void.class);
	
	public static SkriptType.Virtual create(String className) {
		return new SkriptType.Virtual(className, false, null);
	}
	
	public static SkriptType.Concrete create(Class<?> backingClass) {
		return new SkriptType.Concrete(backingClass, false, null);
	}
	
	/**
	 * If this represents a list
	 */
	private final boolean isList;
	
	/**
	 * Type used in {@link #equals(Object)} and {@link #hashCode()} for
	 * identity.
	 */
	private final SkriptType identity;
	
	private SkriptType(boolean isList, SkriptType identity) {
		this.isList = isList;
		this.identity = identity != null ? identity : this;
	}
	
	/**
	 * Creates a concrete version of this type.
	 * @return A concrete type.
	 * @throws ClassNotFoundException When a {@link Virtual virtual} type could
	 * not be materialized because the backing class cannot be found.
	 */
	public abstract SkriptType.Concrete materialize() throws ClassNotFoundException;
	
	/**
	 * Creates a type for a list with components of this type.
	 * @return A list type.
	 */
	public abstract SkriptType listOf();
	
	/**
	 * Checks if this type represents a list.
	 * @return If this is a list type.
	 */
	public boolean isList() {
		return isList;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof SkriptType)) {
			return false;
		}
		SkriptType type = (SkriptType) o;
		return identity == type.identity && isList == type.isList;
	}
	
	@Override
	public int hashCode() {
		return 31 * System.identityHashCode(identity) + (isList ? 1 : 0);
	}

	/**
	 * A type that does not necessarily have its backing {@link Class class}
	 * available in this JVM.
	 *
	 */
	public static class Virtual extends SkriptType {

		/**
		 * Name of class backing this type. Not necessarily loaded in this JVM!
		 */
		private final String className;
		
		private Virtual(String className, boolean isList, SkriptType identity) {
			super(isList, identity);
			this.className = className;
		}
		
		@Override
		public Concrete materialize() throws ClassNotFoundException {
			return new SkriptType.Concrete(parseClassName(className), isList(), this);
		}
		
		private Class<?> parseClassName(String name) throws ClassNotFoundException {
			switch (name) {
			case "void":
				return void.class;
			case "byte":
				return byte.class;
			case "short":
				return short.class;
			case "char":
				return char.class;
			case "int":
				return int.class;
			case "long":
				return long.class;
			case "float":
				return float.class;
			case "double":
				return double.class;
			default:
				return Class.forName(name);
			}
		}

		@Override
		public SkriptType listOf() {
			if (isList()) {
				throw new UnsupportedOperationException("already a list");
			}
			return new Virtual(className, true, this);
		}
		
	}
	
	/**
	 * A type that has the {@link Class class} backing it available in
	 * currently running JVM.
	 *
	 */
	public static class Concrete extends SkriptType {

		/**
		 * The backing class for this type.
		 */
		private final Class<?> backingClass;
		
		private Concrete(Class<?> backingClass, boolean isList, SkriptType identity) {
			super(isList, identity);
			this.backingClass = backingClass;
		}
		
		@Override
		public Concrete materialize() {
			return this; // We're already concrete, no need to change anything!
		}
		
		/**
		 * Gets backing class of this concrete type.
		 * @return A backing class.
		 */
		public Class<?> getBackingClass() {
			return backingClass;
		}

		@Override
		public SkriptType.Concrete listOf() {
			if (isList()) {
				throw new UnsupportedOperationException("already a list");
			}
			return new Concrete(backingClass, true, this);
		}
		
	}
}
