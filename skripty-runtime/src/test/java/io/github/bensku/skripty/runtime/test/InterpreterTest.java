package io.github.bensku.skripty.runtime.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import io.github.bensku.skripty.core.RunnerState;
import io.github.bensku.skripty.core.flow.ScopeEntry;
import io.github.bensku.skripty.runtime.ScriptRunner;
import io.github.bensku.skripty.runtime.ir.IrAssembler;
import io.github.bensku.skripty.runtime.ir.IrBlock;
import io.github.bensku.skripty.runtime.ir.IrNode;

public class InterpreterTest {

	private IrAssembler assembler = new IrAssembler(text -> text);
	private ScriptRunner<RunnerState> runner = new ScriptRunner<>(64);
	
	private IrBlock loadAssembly(String name) {
		try {
			String source = Files.readString(Paths.get("src", "test", "resources", "assembly", name));
			return assembler.parseBlock(source);
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}
	
	private Object runAssembly(String name) {
		try {
			return runner.run(loadAssembly(name), null);
		} catch (Throwable e) {
			throw new AssertionError(e);
		}
	}
	
	@Test
	public void assemblerCheck() {
		// Check that the assembler correctly parses things
		// TODO add LoadConstant once assembler supports it here
		IrBlock block = loadAssembly("selftest");
		IrNode[] nodes = block.getNodes();
		assertEquals(IrNode.Pop.INSTANCE, nodes[0]);
		assertEquals("foo bar", ((IrNode.LoadLiteral) nodes[1]).getValue());
		// Assume CallPlain and CallWithState are correct, because methods were resolved
		IrNode.Jump jump = (IrNode.Jump) nodes[4];
		assertEquals(ScopeEntry.YES, jump.getConstant());
		assertEquals(-2, jump.getTarget());
	}
	
	@Test
	public void empty() throws Throwable {
		assertNull(runner.run(new IrBlock(), null));
	}
	
	@Test
	public void simpleReturn() {
		assertEquals("test text", runAssembly("return1"));
		assertNull(runAssembly("return2"));
	}
	
	@Test
	public void simpleCall() {
		assertEquals("alphabetagamma", runAssembly("call"));
	}
	
	@Test
	public void condition() {
		assertEquals("correct return", runAssembly("condition"));
	}
}
