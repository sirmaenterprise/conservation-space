package com.sirma.cmf.web.search;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * The Interface SearchPageType.
 * 
 * @author svelikov
 */
@Qualifier
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD,
		ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchPageType {

	/**
	 * Value.
	 */
	String value() default "";

}