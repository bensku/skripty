package io.github.bensku.skripty.runtime.ir;

import java.lang.invoke.MethodHandle;

import io.github.bensku.skripty.core.RunnerState;
import io.github.bensku.skripty.core.ScriptBlock;

/**
 * IR node is a single operation in flat, stack-based representation of a
 * {@link ScriptBlock script block}.
 *
 */
public class IrNode {

	private IrNode() {}
	
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
	public static class CallMethod extends IrNode {
		
		/**
		 * Handle of method that we're going to invoke.
		 */
		private final MethodHandle handle;
		
		/**
		 * If no type conversions are needed to call use the handle.
		 */
		private final boolean exact;
		
		CallMethod(MethodHandle handle, boolean isExact) {
			this.handle = handle;
			this.exact = isExact;
		}
		
		public MethodHandle getHandle() {
			return handle;
		}
		
		public boolean isExact() {
			return exact;
		}
	}
	
	/**
	 * Just call the method.
	 *
	 */
	public static class CallPlain extends CallMethod {

		CallPlain(MethodHandle handle, boolean isExact) {
			super(handle, isExact);
		}
		
	}
	
	/**
	 * Call method and inject {@link RunnerState runner state} as first
	 * parameter.
	 *
	 */
	public static class CallInjectState extends CallMethod {

		CallInjectState(MethodHandle handle, boolean isExact) {
			super(handle, isExact);
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
