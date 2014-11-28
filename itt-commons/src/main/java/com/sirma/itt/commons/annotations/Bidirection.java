/**
 * Copyright (c) 2009 12.11.2009 , Sirma ITT. /* /**
 */
package com.sirma.itt.commons.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate the field create a bidirectional reference.
 * 
 * @author Hristo Iliev
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.FIELD)
public @interface Bidirection {
	/**
	 * Name of bidirectional reference.
	 * 
	 * @return {@link String}, name of bidirectional reference
	 */
	String name();
}
