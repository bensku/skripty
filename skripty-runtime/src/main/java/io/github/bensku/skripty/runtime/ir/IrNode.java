package io.github.bensku.skripty.runtime.ir;

import java.lang.invoke.MethodHandle;

import io.github.bensku.skripty.core.ScriptBlock;

/**
 * IR node is a single operation in flat, stack-based representation of a
 * {@link ScriptBlock script block}.
 *
 */
public class IrNode {

	/**
	 * Loads a constant value to stack.
	 *
	 */
	public static class LoadConstant extends IrNode {
		
		/**
		 * Constant value to load to stack.
		 */
		private final Object value;

		public LoadConstant(Object value) {
			this.value = value;
		}

		public Object getValue() {
			return value;
		}
		
	}
	
	/**
	 * Superclass of different method call nodes.
	 *
	 */
	private static class CallMethod extends IrNode {
		
		/**
		 * Handle of method that we're going to invoke.
		 */
		private final MethodHandle handle;
		
		public CallMethod(MethodHandle handle) {
			this.handle = handle;
		}
		
		public MethodHandle getHandle() {
			return handle;
		}
	}
	
	/**
	 * Call to a method using {@link MethodHandle#invokeExact(Object...)}.
	 * Pop stack slots to use as arguments.
	 *
	 */
	public static class CallExact extends CallMethod {

		public CallExact(MethodHandle handle) {
			super(handle);
		}
		
	}
	
	/**
	 * Call to a method using {@link MethodHandle#invoke(Object...)}.
	 * Pop stack slots to use as arguments.
	 *
	 */
	public static class CallVirtual extends CallMethod {

		public CallVirtual(MethodHandle handle) {
			super(handle);
		}
		
	}
	
	/**
	 * Peek at the top value from the stack and compare it to a constant.
	 * If they're same object, jump based on specified offset. Otherwise,
	 * continue from next node as if nothing had happened.
	 * 
	 */
	public static class Jump extends IrNode {
		
		/**
		 * If the value is same as this (reference equality), jump.
		 */
		private final Object constant;
		
		/**
		 * Index of jump target node.
		 */
		private final int target;

		public Jump(Object constant, int offset) {
			this.constant = constant;
			this.target = offset;
		}

		public Object getConstant() {
			return constant;
		}

		public int getTarget() {
			return target;
		}
		
	}
	
	/**
	 * Pop a value from the stack.
	 *
	 */
	public static class Pop extends IrNode {
		
	}
}
