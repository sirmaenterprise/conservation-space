/**
 * Copyright (c) 2013 06.06.2013 , Sirma ITT. /* /**
 */
package com.sirma.itt.seip.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Defines a HTML documentation for the annotated resource.
 *
 * @author Adrian Mitev
 */
@Target({ TYPE, FIELD, ElementType.METHOD })
@Retention(RUNTIME)
public @interface Documentation {

	/**
	 * Resource documentation as HTML.
	 * 
	 * @return documentation as HTML
	 */
	String value();

}
