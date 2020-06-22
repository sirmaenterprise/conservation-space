package com.sirma.itt.seip.plugin;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

/**
 * Marks a bean as a plugin to a given extension point.
 *
 * @author Adrian Mitev
 */
@Qualifier
@Target({ TYPE, METHOD, PARAMETER, FIELD })
@Retention(RUNTIME)
@Documented
@Repeatable(value = Extensions.class)
public @interface Extension {

	/**
	 * Name of the extension point the current plugin is bound to.
	 */
	@Nonbinding
	String target();

	/**
	 * Order of the current plugin.
	 */
	@Nonbinding
	double order() default 0;

	/**
	 * Priority of the current plugin. If two plugins with the same order a found, the one with higher priority will be
	 * used.
	 */
	@Nonbinding
	int priority() default 0;

	/**
	 * If false the plugin will be perged after all plugins are found and the merge operation completes. This might be
	 * used as a mechanism to drop existing plugins by defining a new one with the same order but with higher priority
	 * and enabled=false.
	 */
	@Nonbinding
	boolean enabled() default true;

}
