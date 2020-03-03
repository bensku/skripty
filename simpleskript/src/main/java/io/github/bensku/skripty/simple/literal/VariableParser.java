package io.github.bensku.skripty.simple.literal;

import java.nio.charset.StandardCharsets;

import io.github.bensku.skripty.core.AstNode;
import io.github.bensku.skripty.parser.expression.LiteralParser;
import io.github.bensku.skripty.parser.expression.ParserState;
import io.github.bensku.skripty.simple.SimpleTypes;
import io.github.bensku.skripty.simple.state.SimpleParserState;

public class VariableParser implements LiteralParser {

	@Override
	public Result parse(ParserState state, byte[] input, int start) {
		if (input[start] != '{' || input.length - 1 == start) {
			return null;
		}
		int end = -1;
		String name = null;
		for (int i = start + 1; i < input.length; i++) {
			if (input[i] == '}') {
				end = i;
				name = new String(input, start + 1, end - start - 1, StandardCharsets.UTF_8);
			}
		}
		if (name == null) {
			return null;
		}
		
		int slot = ((SimpleParserState) state).getVariableSlot(name);
		return new Result(new AstNode.Literal(SimpleTypes.VARIABLE, slot), end + 1);
	}

}
