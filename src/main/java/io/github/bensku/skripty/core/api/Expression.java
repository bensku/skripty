package io.github.bensku.skripty.core.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated class provides methods implementing
 * an expression.
 *
 */
@Target(ElementType.TYPE)
public @interface Expression {

}
