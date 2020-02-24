package io.github.bensku.skripty.runtime.ir;

import java.lang.invoke.MethodHandle;

import io.github.bensku.skripty.core.AstNode;
import io.github.bensku.skripty.core.RunnerState;
import io.github.bensku.skripty.core.ScriptBlock;
import io.github.bensku.skripty.core.ScriptUnit;
import io.github.bensku.skripty.core.SkriptType;
import io.github.bensku.skripty.core.expression.CallTarget;
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
	
	/**
	 * Compiles a source block.
	 * @param block Target IR block.
	 * @param source Source block.
	 */
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
			block.append(IrNode.Pop.INSTANCE); // In any case, the ScopeEntry goes away
		}
	}
	
	/**
	 * Emits one {@link Unit unit} of a source block.
	 * @param block Target IR.
	 * @param unit Script unit.
	 */
	private void emitUnit(IrBlock block, ScriptUnit unit) {
		if (unit instanceof ScriptBlock) { // Flatten block in block
			compile(block, (ScriptBlock) unit);
		} else { // Flatten and compile AST node
			emitNode(block, (AstNode.Expr) unit);
		}
	}
	
	/**
	 * Emits an expression node.
	 * @param block Target IR.
	 * @param node Expression AST node.
	 * @return Superclass of values this node returns.
	 */
	private Class<?> emitNode(IrBlock block, AstNode.Expr node) {
		AstNode[] inputs = node.getInputs();
		SkriptType[] inputTypes = new SkriptType[inputs.length];
		Class<?>[] inputClasses = new Class[inputs.length];
		for (int i = 0; i < inputs.length; i++) { // Emit nodes that load inputs to stack
			AstNode input = inputs[i];
			inputTypes[i] = input.getReturnType();
			try {
				inputClasses[i] = input.getReturnType().materialize().getBackingClass();
			} catch (ClassNotFoundException e) {
				assert false : "class we're compiling IR for is not loaded"; // TODO API usage error, handle better
			}
			if (input instanceof AstNode.Literal) { // Literal -> constant
				Object constant = ((AstNode.Literal) input).getValue();
				block.append(new IrNode.LoadLiteral(constant));
				inputClasses[i] = constant.getClass();
			} else { // Handle expressions recursively
				inputClasses[i] = emitNode(block, (AstNode.Expr) input);
			}
		}
		
		// Emit call to implementation of this node
		return emitExpression(block, node.getExpression(), inputTypes, inputClasses);
	}
	
	/**
	 * Emits an expression.
	 * @param block Target IR.
	 * @param expr Expression to emit.
	 * @param inputTypes Types of the inputs.
	 * @param inputClasses Classes of inputs given to the expression.
	 * @return Superclass of values this expression returns.
	 */
	private Class<?> emitExpression(IrBlock block, Expression expr, SkriptType[] inputTypes, Class<?>[] inputClasses) {
		if (expr instanceof ConstantExpression) { // Constant expression -> constant
			// Do not allocate unnecessary vararg array
			block.append(new IrNode.LoadConstant((ConstantExpression) expr));
			// Constant "calls" are cheap, do one just to get the type
			return expr.call((Object[]) null).getClass();
		} else { // Resolve call target, emit call to it
			CallableExpression callable = (CallableExpression) expr;
			CallTarget target = callable.findTarget(inputTypes, inputClasses, true);
			if (target != null) {
				emitMethod(block, callable.getInstance(), target.getMethod(), true, target.shouldInjectState());
				return target.getMethod().type().returnType(); // Do not emit two calls to same method
			}
			target = callable.findTarget(inputTypes, inputClasses, false);
			if (target != null) {
				emitMethod(block, callable.getInstance(), target.getMethod(), false, target.shouldInjectState());
			} else {
				throw new AssertionError("call target not found"); // TODO handle this API usage error better
			}
			return target.getMethod().type().returnType();
		}
	}
	
	/**
	 * Emits a call to a method.
	 * @param block Target IR.
	 * @param instance Instance to use as 'this' for calls.
	 * @param handle Method handle.
	 * @param exact If handle is exactly correct (i.e. no type casting needed).
	 * @param injectState If first parameter of method should be the
	 * {@link RunnerState runner state}
	 */
	private void emitMethod(IrBlock block, Object instance, MethodHandle handle, boolean exact, boolean injectState) {
		handle = handle.bindTo(instance);
		if (injectState) {
			block.append(new IrNode.CallWithState(handle, exact));
		} else {
			block.append(new IrNode.CallPlain(handle, exact));
		}
	}
}
