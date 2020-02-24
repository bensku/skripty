package io.github.bensku.skripty.runtime;

import java.lang.invoke.MethodHandle;
import java.util.function.Supplier;

import io.github.bensku.skripty.core.RunnerState;
import io.github.bensku.skripty.runtime.ir.IrBlock;
import io.github.bensku.skripty.runtime.ir.IrNode;
import io.github.bensku.skripty.runtime.ir.Opcodes;

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
		int[] opcodes = block.opcodeArray(); // Zero-copy, zeroes at end
		RunnerState state = stateSupplier.get();
		ScriptStack stack = new ScriptStack(stackSize);
		
		// Execute all nodes
		for (int i = 0; i < block.size();) {
			int opcode = opcodes[i];
			IrNode node = nodes[i];
			
			// Select what to execute based on opcode
			// Should compile to tableswitch bytecode, which is O(1)
			switch (opcode) {
			case Opcodes.POP:
				stack.pop();
				break;
			case Opcodes.LOAD_LITERAL:
				stack.push(((IrNode.LoadLiteral) node).getValue());
				break;
			case Opcodes.LOAD_CONSTANT:
				stack.push(((IrNode.LoadConstant) node).getValue());
				break;
			case Opcodes.CALL_PLAIN:
				MethodHandle handle = ((IrNode.CallPlain) node).getHandle();
				int argCount = handle.type().parameterCount();
				stack.push(handle.invokeWithArguments(stack.pop(argCount)));
				break;
			case Opcodes.CALL_WITH_STATE:
				handle = ((IrNode.CallWithState) node).getHandle();
				argCount = handle.type().parameterCount();
				Object[] args = new Object[argCount];
				args[0] = state; // Inject runner state
				stack.popInto(args, 1, argCount - 1); // Pop other arguments from stack
				stack.push(handle.invokeWithArguments(args));
				break;
			case Opcodes.JUMP:
				Object expected = ((IrNode.Jump) node).getConstant();
				if (expected == stack.peek()) { // Jump to somewhere
					i = ((IrNode.Jump) node).getTarget();
					continue; // Override control flow
				}
				break;
			}
			i++; // Next node
		}
	}
}
