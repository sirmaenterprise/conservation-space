package com.sirma.itt.seip.plugin;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

/**
 * Used for injecting plugins for a particular extension point.
 *
 * @author Adrian Mitev
 */
@Qualifier
@Target({ TYPE, METHOD, PARAMETER, FIELD })
@Retention(RUNTIME)
@Documented
public @interface ExtensionPoint {

	/**
	 * Name of the extension point.
	 */
	@Nonbinding
	String value();

	/**
	 * If true, all the beans with @Dependent scope for the particular extension point will be cached unless there is a
	 * bean with a scope other than @Dependent.
	 */
	@Nonbinding
	boolean singleton() default false;

	/**
	 * If true the extensions will be served in reverse order.
	 */
	@Nonbinding
	boolean reverseOrder() default false;

}
