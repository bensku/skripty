package io.github.bensku.skripty.simple.scope;

import io.github.bensku.skripty.core.annotation.CallTarget;
import io.github.bensku.skripty.core.annotation.Inputs;
import io.github.bensku.skripty.core.annotation.Returns;
import io.github.bensku.skripty.core.flow.ScopeEntry;
import io.github.bensku.skripty.parser.annotation.Pattern;

@Inputs({"boolean"})
@Returns("scope_entry")
@Pattern("if {0}")
public class ScopeIf {
	
	@CallTarget
	public ScopeEntry shouldEnter(boolean condition) {
		return condition ? ScopeEntry.ONCE : ScopeEntry.NO;
	}
}
