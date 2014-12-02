package com.sirma.cmf.web.search.facet.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * The Interface UpdatedSearchFilter.
 * 
 * @author svelikov
 */
@Qualifier
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD,
		ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface UpdatedSearchFilter {

	/**
	 * Value.
	 */
	String value() default "";

}
