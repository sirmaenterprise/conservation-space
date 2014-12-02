package com.sirma.itt.emf.resources.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Qualifier annotation.
 * 
 * @author svelikov
 */
@Qualifier
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD,
		ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ItemsFilter {

	/**
	 * Filter string.
	 */
	String value() default "";

}