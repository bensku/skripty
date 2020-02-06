package io.github.bensku.skripty.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes the input types of the expression it annotates. Each value in array
 * represents an input slot. Each input slot may accept multiple types by
 * separating them with '/'. Slots may be made optional by adding '?' after
 * all type names.
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Inputs {

	String[] value();
}
