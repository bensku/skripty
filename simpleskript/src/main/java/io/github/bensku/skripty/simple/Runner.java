package io.github.bensku.skripty.simple;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import io.github.bensku.skripty.core.ScriptBlock;
import io.github.bensku.skripty.parser.log.ParseResult;
import io.github.bensku.skripty.runtime.ScriptRunner;
import io.github.bensku.skripty.runtime.ir.IrCompiler;

public class Runner {
	
	/**
	 * Max stack size of scripts.
	 */
	private static final int SCRIPT_STACK_SIZE = 64;

	public static void main(String... args) throws Throwable {
		SimpleParser parser = new SimpleParser();
		IrCompiler compiler = new IrCompiler();
		// We're not using script state for anything, but might do so in future
		ScriptRunner runner = new ScriptRunner(() -> null, SCRIPT_STACK_SIZE);
		
		if (args.length == 0) { // Launch a simple REPL
			try (Scanner scan = new Scanner(System.in)) {
				while (true) {
					System.out.print("> ");
					String line = scan.nextLine();
					ParseResult<ScriptBlock> block = parser.parse(line);
					if (block.isSuccess()) {
						runner.run(compiler.compile(block.getResult()));
					} else {
						System.err.println("Can't parse that");
					}
				}
			}
		}
		
		// Assume all arguments are file names
		for (String name : args) {
			// Bubble up IOException and any throwables that might be thrown by expressions
			// TODO reconsider this, it is a pretty bad practise
			String content = Files.readString(Paths.get(name));
			ParseResult<ScriptBlock> block = parser.parse(content);
			if (block.isSuccess()) {
				runner.run(compiler.compile(block.getResult()));
			} else {
				System.err.println("Failed to parse the script contents");
			}
		}
	}
}
