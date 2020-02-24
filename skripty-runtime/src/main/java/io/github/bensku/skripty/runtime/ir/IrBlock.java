package io.github.bensku.skripty.runtime.ir;

import io.github.bensku.skripty.runtime.ScriptRunner;

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
	 * Cached opcodes for the {@link ScriptRunner interpreter}.
	 */
	private int[] opcodes;
	
	/**
	 * Amount of nodes.
	 */
	private int nodeCount;

	public IrBlock() {
		this.nodes = new IrNode[INITIAL_NODE_COUNT];
		this.opcodes = new int[INITIAL_NODE_COUNT];
	}
	
	public IrBlock(IrNode[] nodes) {
		this.nodes = nodes;
		this.opcodes = new int[nodes.length];
		this.nodeCount = nodes.length;
		
		// Fill opcodes array
		for (int i = 0; i < nodes.length; i++) {
			opcodes[i] = nodes[i].getOpcode();
		}
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
			int[] newOpcodes = new int[opcodes.length * 2];
			System.arraycopy(opcodes, 0, newOpcodes, 0, opcodes.length);
			opcodes = newOpcodes;
			
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
		opcodes[index] = node.getOpcode();
	}
	
	public IrNode[] getNodes() {
		IrNode[] results = new IrNode[nodeCount];
		System.arraycopy(nodes, 0, results, 0, nodeCount);
		return results;
	}
	
	public IrNode[] nodeArray() {
		return nodes;
	}
	
	public int[] opcodeArray() {
		return opcodes;
	}
}
