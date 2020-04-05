package io.github.bensku.skripty.parser.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

import org.junit.jupiter.api.Test;

import io.github.bensku.skripty.parser.script.SourceNode;
import io.github.bensku.skripty.parser.script.SectionParser.IndentationException;
import io.github.bensku.skripty.parser.script.SectionParser;

public class SectionParserTest {

	private final SectionParser parser = new SectionParser();
	
	private SourceNode.Section parseFile(String name) {
		try {
			String source = Files.readString(Paths.get("src", "test", "resources", "sourceNode", name));
			return parser.parse(source);
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}
	
	@Test
	public void emptySource() {
		SourceNode.Section section = parser.parse("");
		assertFalse(section.iterator().hasNext());
	}
	
	@Test
	public void blankLines() {
		SourceNode.Section section = parser.parse(" \n \n   \n ");
		assertFalse(section.iterator().hasNext());
	}
	
	private void assertContentEq(String content, SourceNode node) {
		SourceNode.Statement statement = (SourceNode.Statement) node;
		assertEquals(content, statement.getText());
	}
	
	@Test
	public void flat() {
		SourceNode.Section section = parseFile("flat");
		Iterator<SourceNode> it = section.iterator();
		assertContentEq("first line", it.next());
		assertContentEq("second line", it.next());
		assertContentEq("third line", it.next());
		assertContentEq("fourth line", it.next());
	}
	
	@Test
	public void nested() {
		SourceNode.Section section = parseFile("nested");
		Iterator<SourceNode> it = section.iterator();
		
		SourceNode.Section section1 = (SourceNode.Section) it.next();
		Iterator<SourceNode> innerIt = section1.iterator();
		assertContentEq("first statement", innerIt.next());
		assertContentEq("second statement", innerIt.next());
		assertContentEq("third statement", innerIt.next());
		assertContentEq("fourth statement", innerIt.next());
		
		SourceNode.Section section2 = (SourceNode.Section) it.next();
		SourceNode.Section inner = (SourceNode.Section) section2.getNodes()[0];
		assertContentEq("second-innermost first", inner.getNodes()[0]);
		SourceNode.Section inner2 = (SourceNode.Section) inner.getNodes()[1];
		assertContentEq("innermost statement", inner2.iterator().next());
		assertContentEq("second-innermost second", inner.getNodes()[2]);
		
		assertContentEq("last statement", it.next());
	}
	
	@Test
	public void whitespaceErrors() {
		assertThrows(IndentationException.class, () -> parser.parse("foo:\n   \t"));
		assertThrows(IndentationException.class, () -> parser.parse("foo:\n\tbar:\n        baz"));
		assertThrows(IndentationException.class, () -> parser.parse("foo:\nbar"));
	}
	
	@Test
	public void commentAndLine() {
		assertEquals(" A comment!", ((SourceNode.Statement) parser.parse("at root # A comment!").getNodes()[0]).getComment());
	}
}
