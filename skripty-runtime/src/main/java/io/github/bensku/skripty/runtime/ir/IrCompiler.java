package io.github.bensku.skripty.runtime.ir;

import io.github.bensku.skripty.core.AstNode;
import io.github.bensku.skripty.core.ScriptBlock;
import io.github.bensku.skripty.core.ScriptUnit;
import io.github.bensku.skripty.core.flow.ScopeEntry;

/**
 * Compiles scripts into a flat, intermediate format that can be interpreted
 * or, in future, compiled to e.g. JVM bytecode.
 *
 */
public class IrCompiler {

	public void compile(IrBlock block, ScriptBlock source) {
		int start = block.size();
		
		AstNode.Expr title = source.getTitleExpr();
		int jumpAfterNode = -1;
		if (title != null) { // If this might be conditional block, reserve space for initial jump
			jumpAfterNode = block.skip();
		}
		
		// Emit contents of block
		for (ScriptUnit unit : source.getUnits()) {
			
		}
		
		if (title != null) { // Jump back to calling title if this might be a loop
			block.append(new IrNode.Jump(ScopeEntry.YES, start)); // Jump back to start to check again
			// Add the initial check to space we reserved at start (NO -> skip this block)
			block.set(jumpAfterNode, new IrNode.Jump(ScopeEntry.NO, block.size()));
			// We end there after executing block or jump there
			block.append(new IrNode.Pop()); // In any case, the ScopeEntry goes away
		}
	}
}
