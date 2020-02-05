package io.github.bensku.skripty.runtime.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.github.bensku.skripty.runtime.ir.IrBlock;
import io.github.bensku.skripty.runtime.ir.IrNode;

public class BlockTest {

	@Test
	public void blockOps() {
		IrBlock block = new IrBlock();
		IrNode node = new IrNode.Pop();
		block.append(node);
		assertEquals(1, block.size());
		assertEquals(1, block.skip());
		assertEquals(2, block.size());
		assertDoesNotThrow(() -> block.set(1, node));
		assertThrows(IllegalArgumentException.class, () -> block.set(10, node));
		
		IrNode[] nodes = block.getNodes();
		assertEquals(2, nodes.length);
	}
}
