package com.sirma.itt.seip.configuration.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;

/**
 * Range class to define a configuration value boundaries.
 *
 * @author BBonev
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD })
public @interface ConfigRange {

	/**
	 * Range start
	 *
	 * @return the string
	 */
	@Nonbinding
	String from() default "";

	/**
	 * Range end.
	 *
	 * @return the string
	 */
	@Nonbinding
	String to() default "";
}
