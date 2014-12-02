package com.sirma.cmf.web.form.action.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Qualifier annotation used to distinguish the task menu.
 * 
 * @author svelikov
 */
@Qualifier
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface DynamicFormActionEventType {

	/**
	 * Value.
	 */
	String value() default "";

}