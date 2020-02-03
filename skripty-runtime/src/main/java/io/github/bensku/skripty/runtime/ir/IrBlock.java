package io.github.bensku.skripty.runtime.ir;

public class IrBlock {

	/**
	 * The IR nodes in this block.
	 */
	private final IrNode[] nodes;

	public IrBlock() {
		this.nodes = new IrNode[0]; // TODO
	}
	
	public int size() {
		throw new UnsupportedOperationException();
	}
	
	public void append(IrNode node) {
		throw new UnsupportedOperationException();
	}
	
	public int skip() {
		throw new UnsupportedOperationException();
	}
	
	public void set(int index, IrNode node) {
		nodes[index] = node;
	}
}
