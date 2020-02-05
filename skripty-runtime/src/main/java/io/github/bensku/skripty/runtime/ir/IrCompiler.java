package io.github.bensku.skripty.runtime.ir;

import java.lang.invoke.MethodHandle;

import io.github.bensku.skripty.core.AstNode;
import io.github.bensku.skripty.core.ScriptBlock;
import io.github.bensku.skripty.core.ScriptUnit;
import io.github.bensku.skripty.core.expression.CallableExpression;
import io.github.bensku.skripty.core.expression.ConstantExpression;
import io.github.bensku.skripty.core.expression.Expression;
import io.github.bensku.skripty.core.flow.ScopeEntry;

/**
 * Compiles scripts into a flat, intermediate format that can be interpreted
 * or, in future, compiled to e.g. JVM bytecode.
 *
 */
public class IrCompiler {

	/**
	 * Compiles given script block.
	 * @param source Parsed script block.
	 * @return Executable block of IR nodes.
	 */
	public IrBlock compile(ScriptBlock source) {
		IrBlock block = new IrBlock();
		compile(block, source);
		return block;
	}
	
	private void compile(IrBlock block, ScriptBlock source) {
		int start = block.size();
		
		AstNode.Expr title = source.getTitleExpr();
		int jumpAfterNode = -1;
		if (title != null) { // If this might be conditional block, reserve space for initial jump
			emitNode(block, title);
			jumpAfterNode = block.skip();
		}
		
		// Emit contents of block
		for (ScriptUnit unit : source.getUnits()) {
			emitUnit(block, unit);
		}
		
		if (title != null) { // Jump back to calling title if this might be a loop
			block.append(new IrNode.Jump(ScopeEntry.YES, start)); // Jump back to start to check again
			// Add the initial check to space we reserved at start (NO -> skip this block)
			block.set(jumpAfterNode, new IrNode.Jump(ScopeEntry.NO, block.size()));
			// We end there after executing block or jump there
			block.append(new IrNode.Pop()); // In any case, the ScopeEntry goes away
		}
	}
	
	private void emitUnit(IrBlock block, ScriptUnit unit) {
		if (unit instanceof ScriptBlock) { // Flatten block in block
			compile(block, (ScriptBlock) unit);
		} else { // Flatten and compile AST node
			emitNode(block, (AstNode.Expr) unit);
		}
	}
	
	private void emitNode(IrBlock block, AstNode.Expr node) {
		AstNode[] inputs = node.getInputs();
		Class<?>[] inputClasses = new Class[inputs.length];
		for (int i = 0; i < inputs.length; i++) { // Emit nodes that load inputs to stack
			AstNode input = inputs[i];
			try {
				inputClasses[i] = input.getReturnType().materialize().getBackingClass();
			} catch (ClassNotFoundException e) {
				assert false : "class we're compiling IR for is not loaded"; // TODO API usage error, handle better
			}
			if (input instanceof AstNode.Literal) { // Literal -> constant
				block.append(new IrNode.LoadConstant(((AstNode.Literal) input).getValue()));
			} else { // Handle expressions recursively
				emitNode(block, (AstNode.Expr) input);
			}
		}
		
		// Emit call to implementation of this node
		emitExpression(block, node.getExpression(), inputClasses);
	}
	
	private void emitExpression(IrBlock block, Expression expr, Class<?>[] inputClasses) {
		if (expr instanceof ConstantExpression) { // Constant expression -> constant
			// Do not allocate unnecessary vararg array
			block.append(new IrNode.LoadConstant(expr.call((Object[]) null)));
		} else { // Resolve call target, emit call to it
			CallableExpression callable = (CallableExpression) expr;
			MethodHandle handle = callable.findTarget(inputClasses, true);
			if (handle != null) {
				block.append(new IrNode.CallExact(handle));
			}
			handle = callable.findTarget(inputClasses, false);
			if (handle != null) {
				block.append(new IrNode.CallVirtual(handle));
			} else {
				assert false : "call target not found"; // TODO handle this API usage error better
			}
		}
	}
}
