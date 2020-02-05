package io.github.bensku.skripty.runtime.ir;

/**
 * Represents a block of {@link IrNode IR nodes}.
 *
 */
public class IrBlock {
	
	private static final int INITIAL_NODE_COUNT = 64;

	/**
	 * The IR nodes in this block.
	 */
	private IrNode[] nodes;
	
	/**
	 * Amount of nodes.
	 */
	private int nodeCount;

	public IrBlock() {
		this.nodes = new IrNode[INITIAL_NODE_COUNT];
	}
	
	/**
	 * Returns amount of nodes and {@link #skip() skipped} slots in this block.
	 * @return Size of this block.
	 */
	public int size() {
		return nodeCount;
	}
	
	/**
	 * Appends a node.
	 * @param node Node.
	 */
	public void append(IrNode node) {
		set(skip(), node);
	}
	
	/**
	 * Skips over next node and returns index that can be used to
	 * {@link #set(int, IrNode) set} it later.
	 * @return Index for node.
	 */
	public int skip() {
		int slot = nodeCount++;
		if (slot == nodes.length) { // Enlarge array
			IrNode[] newNodes = new IrNode[nodes.length * 2];
			System.arraycopy(nodes, 0, newNodes, 0, nodes.length);
			nodes = newNodes;
		}
		return slot;
	}
	
	/**
	 * Sets an existing node or a {@link #skip() skipped} slot to a node.
	 * @param index Index of node or skipped slot.
	 * @param node New node.
	 */
	public void set(int index, IrNode node) {
		if (index >= nodeCount) {
			throw new IllegalArgumentException("only existing nodes or skipped slots can be set");
		}
		nodes[index] = node;
	}
	
	public IrNode[] getNodes() {
		IrNode[] results = new IrNode[nodeCount];
		System.arraycopy(nodes, 0, results, 0, nodeCount);
		return results;
	}
}
