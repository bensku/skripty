package io.github.bensku.skripty.runtime.ir;

/**
 * Opcodes of IR nodes.
 *
 */
public class Opcodes {

	private Opcodes() {}
	
	/**
	 * {@link IrNode.Pop}
	 */
	public static final int POP = 0;
	
	/**
	 * {@link IrNode.LoadLiteral}
	 */
	public static final int LOAD_LITERAL = 1;
	
	/**
	 * {@link IrNode.LoadConstant}
	 */
	public static final int LOAD_CONSTANT = 2;
	
	/**
	 * {@link IrNode.CallPlain}
	 */
	public static final int CALL_PLAIN = 3;
	
	/**
	 * {@link IrNode.CallWithState}
	 */
	public static final int CALL_WITH_STATE = 4;
	
	/**
	 * {@link IrNode.Jump}
	 */
	public static final int JUMP = 5;
}
