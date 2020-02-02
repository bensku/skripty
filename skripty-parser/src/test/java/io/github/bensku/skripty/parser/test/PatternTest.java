package io.github.bensku.skripty.parser.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import io.github.bensku.skripty.parser.pattern.Pattern;
import io.github.bensku.skripty.parser.pattern.PatternPart;

public class PatternTest {

	@Test
	public void validPatterns() {
		Pattern p1 = Pattern.create("test", 0, "pattern");
		assertEquals("test", new String(((PatternPart.Literal) p1.partAt(0)).getText(), StandardCharsets.UTF_8));
		assertEquals(0, ((PatternPart.Input) p1.partAt(1)).getSlot());
		assertEquals("pattern", new String(((PatternPart.Literal) p1.partAt(2)).getText(), StandardCharsets.UTF_8));
		
		Pattern.create("test", 0, 1); // Shouldn't throw IAE, first part is literal!
		Pattern.create(0, "test", 1); // Second part literal
	}
	
	@Test
	public void faultyPatterns() {
		assertThrows(IllegalArgumentException.class, () -> Pattern.create("abc", "def"));
		assertThrows(IllegalArgumentException.class, () -> Pattern.create(0, 1));
	}
}
