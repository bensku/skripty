package io.github.bensku.skripty.core;

/**
 * A type that is visible to scripts.
 *
 */
public interface SkriptType {
	
	/**
	 * A type that represents nothing (Java/JVM void).
	 */
	SkriptType VOID = create(void.class);
	
	static SkriptType.Virtual create(String className) {
		return new SkriptType.Virtual(className);
	}
	
	static SkriptType.Concrete create(Class<?> backingClass) {
		return new SkriptType.Concrete(backingClass);
	}
	
	/**
	 * Creates a concrete version of this type.
	 * @return A concrete type.
	 * @throws ClassNotFoundException When a {@link Virtual virtual} type could
	 * not be materialized because the backing class cannot be found.
	 */
	SkriptType.Concrete materialize() throws ClassNotFoundException;

	/**
	 * A type that does not necessarily have its backing {@link Class class}
	 * available in this JVM.
	 *
	 */
	class Virtual implements SkriptType {

		/**
		 * Name of class backing this type. Not necessarily loaded in this JVM!
		 */
		private final String className;
		
		private Virtual(String className) {
			this.className = className;
		}
		
		@Override
		public Concrete materialize() throws ClassNotFoundException {
			return new SkriptType.Concrete(parseClassName(className));
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
		
	}
	
	/**
	 * A type that has the {@link Class class} backing it available in
	 * currently running JVM.
	 *
	 */
	class Concrete implements SkriptType {

		/**
		 * The backing class for this type.
		 */
		private final Class<?> backingClass;
		
		private Concrete(Class<?> backingClass) {
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
		
	}
}
