package com.sirma.itt.emf.search.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;


/**
 * Search life cycle event
 * 
 * @author BBonev
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
public @interface SearchEvent {

	/**
	 * Determines the search type
	 */
	String type() default "";

	/**
	 * When in the life cycle of the search is the event.
	 */
	SearchEventType when() default SearchEventType.NONE;
}
