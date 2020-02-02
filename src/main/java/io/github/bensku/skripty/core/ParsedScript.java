package io.github.bensku.skripty.core;

/**
 * Represents a parsed script.
 *
 */
public class ParsedScript {
	
	/**
	 * Single entry, e.g. {@link AstNode AST node} created form a statement.
	 *
	 */
	public interface Entry {}
	
	private final ScriptBlock rootBlock;
	
	public ParsedScript(ScriptBlock rootBlock) {
		this.rootBlock = rootBlock;
	}
	
	public ScriptBlock getRoot() {
		return rootBlock;
	}
}
