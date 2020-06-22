package com.sirma.itt.seip.plugin;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a bean as a plugin to a given extension point. This is the repeatable version of the
 * {@link Extension} annotation.
 *
 * @author nvelkov
 */
@Documented
@Retention(RUNTIME)
@Target({ TYPE, METHOD, PARAMETER, FIELD })
public @interface Extensions {

	/**
	 * Get the extensions.
	 * 
	 * @return the extensions
	 */
	Extension[] value() default {};

}