package io.github.bensku.skripty.core;

import io.github.bensku.skripty.core.flow.ScopeEntry;

/**
 * Represents a block of script code.
 *
 */
public class ScriptBlock implements ParsedScript.Entry {

	/**
	 * AST node of title expression of this block. The expression should return
	 * {@link ScopeEntry} to indicate when this block should be entered.
	 */
	private final AstNode.Expr titleExpr;
	
	/**
	 * Entries in this block.
	 */
	private final ParsedScript.Entry[] entries;
	
	public ScriptBlock(AstNode.Expr titleExpr, ParsedScript.Entry[] entries) {
		this.titleExpr = titleExpr;
		this.entries = entries;
	}

	public AstNode getTitleExpr() {
		return titleExpr;
	}

	public ParsedScript.Entry[] getEntries() {
		return entries;
	}
	
}
