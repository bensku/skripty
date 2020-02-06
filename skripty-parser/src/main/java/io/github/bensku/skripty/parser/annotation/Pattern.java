package io.github.bensku.skripty.parser.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes a pattern for the expression it annotates. Multiple patterns may be
 * defined by using this annotation many times.
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Patterns.class)
public @interface Pattern {

	/**
	 * String value of the pattern. Parsed with
	 * {@link io.github.bensku.skripty.parser.pattern.Pattern#parse(String)}.
	 * @return Pattern string.
	 */
	String value();
}
