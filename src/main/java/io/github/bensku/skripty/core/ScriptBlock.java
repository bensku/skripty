package io.github.bensku.skripty.core;

import io.github.bensku.skripty.core.flow.ScopeEntry;

/**
 * Represents a block of script code.
 *
 */
public class ScriptBlock implements ScriptUnit {

	/**
	 * AST node of title expression of this block. The expression should return
	 * {@link ScopeEntry} to indicate when this block should be entered.
	 */
	private final AstNode.Expr titleExpr;
	
	/**
	 * Units in this block.
	 */
	private final ScriptUnit[] units;
	
	public ScriptBlock(AstNode.Expr titleExpr, ScriptUnit[] units) {
		this.titleExpr = titleExpr;
		this.units = units;
	}

	public AstNode.Expr getTitleExpr() {
		return titleExpr;
	}

	public ScriptUnit[] getUnits() {
		return units;
	}
	
}
