package com.sirma.itt.cmf.workflows;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Specifies event for task transitions handling.
 *
 * @author BBonev
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.TYPE, ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD,
		ElementType.ANNOTATION_TYPE })
public @interface TaskTransition {

	/**
	 * Event transition id
	 */
	String event() default "";
}
