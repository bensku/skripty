package io.github.bensku.skripty.runtime.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.github.bensku.skripty.core.ScriptBlock;
import io.github.bensku.skripty.core.ScriptUnit;
import io.github.bensku.skripty.runtime.ir.IrBlock;
import io.github.bensku.skripty.runtime.ir.IrCompiler;

public class IrCompilerTest {

	private IrCompiler compiler = new IrCompiler();
	
	@Test
	public void emptyScript() {
		IrBlock block = compiler.compile(new ScriptBlock(null, new ScriptUnit[0]));
		assertEquals(0, block.size());
	}
}
