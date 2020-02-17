package io.github.bensku.skripty.runtime;

import java.lang.invoke.MethodHandle;
import java.util.function.Supplier;

import io.github.bensku.skripty.core.RunnerState;
import io.github.bensku.skripty.runtime.ir.IrBlock;
import io.github.bensku.skripty.runtime.ir.IrNode;

/**
 * Executes {@link IrBlock IR blocks}.
 *
 */
public class ScriptRunner {

	/**
	 * When new blocks are run, this provides states for them.
	 */
	private final Supplier<RunnerState> stateSupplier;
	
	/**
	 * Stack size of blocks.
	 */
	private final int stackSize;
	
	public ScriptRunner(Supplier<RunnerState> stateSupplier, int stackSize) {
		this.stateSupplier = stateSupplier;
		this.stackSize = stackSize;
	}

	public void run(IrBlock block) throws Throwable {
		IrNode[] nodes = block.nodeArray(); //  Zero-copy, but might have nulls at end
		RunnerState state = stateSupplier.get();
		ScriptStack stack = new ScriptStack(stackSize);
		
		// Execute all nodes
		for (int i = 0; i < block.size();) {
			IrNode node = nodes[i];
			if (node instanceof IrNode.LoadConstant) {
				stack.push(((IrNode.LoadConstant) node).getValue());
			} else if (node instanceof IrNode.CallPlain) {
				MethodHandle handle = ((IrNode.CallPlain) node).getHandle();
				int argCount = handle.type().parameterCount();
				stack.push(handle.invokeWithArguments(stack.pop(argCount)));
			} else if (node instanceof IrNode.CallInjectState) {
				MethodHandle handle = ((IrNode.CallInjectState) node).getHandle();
				int argCount = handle.type().parameterCount();
				Object[] args = new Object[argCount];
				args[0] = state; // Inject runner state
				stack.popInto(args, 1, argCount - 1); // Pop other arguments from stack
				stack.push(handle.invokeWithArguments(args));
			} else if (node instanceof IrNode.Jump) {
				Object expected = ((IrNode.Jump) node).getConstant();
				if (expected == stack.peek()) { // Jump to somewhere
					i = ((IrNode.Jump) node).getTarget();
					break; // Override control flow
				}
			} else if (node instanceof IrNode.Pop) {
				stack.pop();
			}
			
			i++; // Next node
		}
	}
}
