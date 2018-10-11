package com.sirma.itt.seip.permissions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

import com.sirma.itt.seip.permissions.action.ActionEvaluatedEvent;

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
	 * 
	 * @return filter id
	 */
	String value() default "";

	/**
	 * Placeholder.
	 * 
	 * @return placeholder name
	 */
	String placeholder() default "";
}
