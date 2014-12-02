package com.sirma.itt.cmf.workflows;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Defines a task qualifier when implementing task handlers in combination with
 * {@link WorkflowTransition}
 *
 * @author BBonev
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.TYPE, ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD,
		ElementType.ANNOTATION_TYPE })
public @interface TaskType {

	/**
	 * Task id that the observer need to be implemented
	 */
	String task() default "";
}
