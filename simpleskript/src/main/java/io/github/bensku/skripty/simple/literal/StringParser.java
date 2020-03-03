package io.github.bensku.skripty.simple.literal;

import java.nio.charset.StandardCharsets;

import io.github.bensku.skripty.core.AstNode;
import io.github.bensku.skripty.parser.expression.LiteralParser;
import io.github.bensku.skripty.parser.expression.ParserState;
import io.github.bensku.skripty.simple.SimpleTypes;

public class StringParser implements LiteralParser {

	@Override
	public Result parse(ParserState state, byte[] input, int start) {
		if (input[start] != '"') {
			return null; // Not a string literal
		}
		for (int i = start + 1; i < input.length; i++) {
			if (input[i] == '"') {
				String str = new String(input, start + 1, i - start - 1, StandardCharsets.UTF_8);
				return new Result(new AstNode.Literal(SimpleTypes.TEXT, str), i + 1);
			}
		}
		
		// TODO unterminated string, maybe throw something here?
		return null;
	}

}
