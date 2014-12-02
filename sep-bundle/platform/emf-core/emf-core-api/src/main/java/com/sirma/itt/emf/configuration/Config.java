package com.sirma.itt.emf.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

/**
 * Annotation used to inject configuration parameters.
 * 
 * @author BBonev
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
public @interface Config {

	/**
	 * The configuration key name (optional).
	 */
	@Nonbinding
	String name() default "";

	/**
	 * Default value if the key is not found (optional).
	 */
	@Nonbinding
	String defaultValue() default "";
}