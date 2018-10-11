package com.sirma.itt.emf.security.action;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Qualifier annotation used to distinguish the allowed action events.
 *
 * @author svelikov
 */
@Qualifier
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface EMFAction {

	/**
	 * Action id.
	 */
	String value() default "";

	/**
	 * Type of the object instance, the action is attached to. I.e. DocumentInstance.
	 */
	Class<?>target();

}