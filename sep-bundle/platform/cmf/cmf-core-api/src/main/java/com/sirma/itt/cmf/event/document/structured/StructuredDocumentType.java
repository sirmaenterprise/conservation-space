package com.sirma.itt.cmf.event.document.structured;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Document type qualifier.
 * 
 * @author svelikov
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER })
public @interface StructuredDocumentType {

	/**
	 * Document type.
	 */
	String type() default "";
}