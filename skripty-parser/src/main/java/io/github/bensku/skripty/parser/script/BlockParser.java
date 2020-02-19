package io.github.bensku.skripty.parser.script;

import io.github.bensku.skripty.core.AstNode;
import io.github.bensku.skripty.core.ScriptBlock;
import io.github.bensku.skripty.core.ScriptUnit;
import io.github.bensku.skripty.parser.log.ParseResult;

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
	 * @return Parse result.
	 */
	public ParseResult<ScriptBlock> parse(SourceNode.Section section) {
		return parse(rootScope, null, section);
	}
	
	/**
	 * Parses a source section.
	 * @param scope Current scope.
	 * @param titleExpr Title of current scope. May be null.
	 * @param section Source section.
	 * @return Parse result.
	 */
	private ParseResult<ScriptBlock> parse(Scope scope, AstNode.Expr titleExpr, SourceNode.Section section) {
		SourceNode[] sourceNodes = section.getNodes();
		ScriptUnit[] units = new ScriptUnit[sourceNodes.length];
		
		// Parse source nodes into source units
		boolean errored = false;
		for (int i = 0; i < units.length; i++) {
			SourceNode source = sourceNodes[i];
			if (source instanceof SourceNode.Section) { // Recursively parse sub-blocks
				ParseResult<Scope.InnerScope> subscope = scope.parseScope(((SourceNode.Section) source).getTitle());
				
				if (subscope.isSuccess()) {
					Scope.InnerScope innerScope = subscope.getResult();
					ParseResult<ScriptBlock> block = parse(innerScope.getScope(), innerScope.getTitle(), (SourceNode.Section) source);
					if (block.isSuccess()) {
						units[i] = block.getResult();
					} else {
						errored = true;
					}
				} else {
					errored = true;
				}
			} else { // Parse statements using current scope
				assert source instanceof SourceNode.Statement;
				ParseResult<AstNode.Expr> statement = scope.parseStatement((SourceNode.Statement) source);
				if (statement.isSuccess()) {
					units[i] = statement.getResult();
				} else {
					errored = true;
				}
			}
		}
		
		if (errored) { // Looks like there were some errors
			return ParseResult.failure(); // TODO collect messages so we have them here
		} else { // Everything went well, we have a script block
			return ParseResult.success(new ScriptBlock(titleExpr, units));
		}
	}
}
