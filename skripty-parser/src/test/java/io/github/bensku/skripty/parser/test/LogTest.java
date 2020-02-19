package io.github.bensku.skripty.parser.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.bensku.skripty.core.ScriptBlock;
import io.github.bensku.skripty.core.ScriptUnit;
import io.github.bensku.skripty.parser.log.ParseResult;
import io.github.bensku.skripty.parser.log.ParserMessage;
import io.github.bensku.skripty.parser.script.SourceNode;

public class LogTest {

	@Test
	public void parserMessages() {
		SourceNode node = new SourceNode.Statement("test stuff");
		
		ParserMessage error1 = ParserMessage.msg(ParserMessage.Type.ERROR, "error 1").at(node, 0, 4);
		assertEquals(ParserMessage.Type.ERROR, error1.getType());
		assertEquals("error 1", error1.getMessage());
		assertEquals(node, error1.getSource());
		assertEquals(0, error1.getStart());
		assertEquals(4, error1.getEnd());
		assertTrue(error1.format().contains("error 1"));
		
		ParserMessage error2 = ParserMessage.error("error 2").at(node, 5, 10);
		assertEquals(ParserMessage.Type.ERROR, error2.getType());
		assertEquals("error 2", error2.getMessage());
		assertEquals(node, error2.getSource());
		assertEquals(5, error2.getStart());
		assertEquals(10, error2.getEnd());
		assertTrue(error2.format().contains("error 2"));
		
		ParserMessage warn = ParserMessage.warn("warning 1").at(node, 5, 10);
		assertEquals(ParserMessage.Type.WARNING, warn.getType());
		assertEquals("warning 1", warn.getMessage());
		assertEquals(node, warn.getSource());
		assertEquals(5, warn.getStart());
		assertEquals(10, warn.getEnd());
		assertTrue(warn.format().contains("warning 1"));
	}
	
	@Test
	public void parseResults() {
		SourceNode node = new SourceNode.Statement("A Bad End");
		ScriptBlock block = new ScriptBlock(null, new ScriptUnit[0]);
		ParserMessage msg = ParserMessage.warn("Turn back").at(node, 0, 4);
		
		ParseResult<ScriptBlock> success = ParseResult.success(block, msg);
		assertTrue(success.isSuccess());
		assertEquals(block, success.getResult());
		assertEquals(msg, success.getMessages()[0]);
		
		ParseResult<ScriptBlock> failure = ParseResult.failure(msg);
		assertFalse(failure.isSuccess());
		assertThrows(IllegalStateException.class, () -> failure.getResult());
		assertEquals(msg, failure.getMessages()[0]);
	}
}
