package io.github.bensku.skripty.parser.script;

import io.github.bensku.skripty.core.AstNode;
import io.github.bensku.skripty.core.ScriptBlock;
import io.github.bensku.skripty.core.ScriptUnit;

/**
 * Parses blocks of executable code.
 *
 */
public class BlockParser {

	/**
	 * The scope to use for parsing {@link SourceNode source nodes} at root of
	 * blocks.
	 */
	private final Scope rootScope;
	
	public BlockParser(Scope rootScope) {
		this.rootScope = rootScope;
	}
	
	/**
	 * Parses a source section and all its subsections into a source block.
	 * @param section Source section.
	 * @return A source block representing the section.
	 */
	public ScriptBlock parse(SourceNode.Section section) {
		return parse(rootScope, null, section);
	}
	
	/**
	 * Parses a source section.
	 * @param scope Current scope.
	 * @param titleExpr Title of current scope. May be null.
	 * @param section Source section.
	 * @return Block of the section.
	 */
	private ScriptBlock parse(Scope scope, AstNode.Expr titleExpr, SourceNode.Section section) {
		SourceNode[] sourceNodes = section.getNodes();
		ScriptUnit[] units = new ScriptUnit[sourceNodes.length];
		
		// Parse source nodes into source units
		for (int i = 0; i < units.length; i++) {
			SourceNode source = sourceNodes[i];
			if (source instanceof SourceNode.Section) { // Recursively parse sub-blocks
				Scope.ParseResult subscope = scope.parseScope(((SourceNode.Section) source).getTitle());
				units[i] = parse(subscope.getScope(), subscope.getTitle(), (SourceNode.Section) source);
			} else { // Parse statements using current scope
				assert source instanceof SourceNode.Statement;
				units[i] = scope.parseStatement(((SourceNode.Statement) source).getText());
			}
		}
		
		return new ScriptBlock(titleExpr, units);
	}
}
