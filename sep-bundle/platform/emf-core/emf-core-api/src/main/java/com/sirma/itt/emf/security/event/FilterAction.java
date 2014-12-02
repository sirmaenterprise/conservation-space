package com.sirma.itt.emf.security.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Qualifier for {@link ActionEvaluatedEvent} event object.
 * 
 * @author BBonev
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE })
public @interface FilterAction {

	/**
	 * The type of the handled instance
	 */
	String value() default "";

	/**
	 * Placeholder.
	 */
	String placeholder() default "";
}
