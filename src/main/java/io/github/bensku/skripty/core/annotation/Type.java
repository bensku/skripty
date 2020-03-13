package io.github.bensku.skripty.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.github.bensku.skripty.core.type.SkriptType;

/**
 * Used to indicate {@link SkriptType type} of a parameter of call target.
 *
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Type {

	String value();
}
