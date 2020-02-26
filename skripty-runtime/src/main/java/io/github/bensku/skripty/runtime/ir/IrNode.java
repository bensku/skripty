package io.github.bensku.skripty.runtime.ir;

import java.lang.invoke.MethodHandle;

import io.github.bensku.skripty.core.RunnerState;
import io.github.bensku.skripty.core.ScriptBlock;
import io.github.bensku.skripty.core.expression.ConstantExpression;

/**
 * IR node is a single operation in flat, stack-based representation of a
 * {@link ScriptBlock script block}.
 *
 */
public abstract class IrNode {

	private IrNode() {}
	
	/**
	 * Gets {@link Opcodes opcode} of node type of this node.
	 * @return Opcode.
	 */
	public abstract int getOpcode();
	
	/**
	 * Pop a value from the stack.
	 *
	 */
	public static class Pop extends IrNode {
	
		public static final Pop INSTANCE = new Pop();
		
		private Pop() {} // Singleton

		@Override
		public int getOpcode() {
			return Opcodes.POP;
		}
	}
	
	/**
	 * Loads a literal value to the stack.
	 *
	 */
	public static class LoadLiteral extends IrNode {
		
		/**
		 * Literal value to load to stack.
		 */
		private final Object value;

		public LoadLiteral(Object value) {
			this.value = value;
		}

		public Object getValue() {
			return value;
		}

		@Override
		public int getOpcode() {
			return Opcodes.LOAD_LITERAL;
		}
		
	}
	
	/**
	 * Loads value of a constant expression to the stack.
	 *
	 */
	public static class LoadConstant extends IrNode {
		
		/**
		 * The constant expression.
		 */
		private final ConstantExpression expr;
		
		public LoadConstant(ConstantExpression expr) {
			this.expr = expr;
		}
		
		public Object getValue() {
			// ConstantExpression doesn't need args, so don't allocate an array
			return expr.call((Object[]) null);
		}

		@Override
		public int getOpcode() {
			return Opcodes.LOAD_CONSTANT;
		}
	}
	
	/**
	 * Superclass of different method call nodes.
	 *
	 */
	public abstract static class CallMethod extends IrNode {
		
		/**
		 * Handle of method that we're going to invoke.
		 */
		private final MethodHandle handle;
		
		/**
		 * If no type conversions are needed to call use the handle.
		 */
		private final boolean exact;
		
		private CallMethod(MethodHandle handle, boolean isExact) {
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

		public CallPlain(MethodHandle handle, boolean isExact) {
			super(handle, isExact);
		}

		@Override
		public int getOpcode() {
			return Opcodes.CALL_PLAIN;
		}
		
	}
	
	/**
	 * Call method and inject {@link RunnerState runner state} as first
	 * parameter.
	 *
	 */
	public static class CallWithState extends CallMethod {

		public CallWithState(MethodHandle handle, boolean isExact) {
			super(handle, isExact);
		}

		@Override
		public int getOpcode() {
			return Opcodes.CALL_WITH_STATE;
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

		@Override
		public int getOpcode() {
			return Opcodes.JUMP;
		}
		
	}

}
