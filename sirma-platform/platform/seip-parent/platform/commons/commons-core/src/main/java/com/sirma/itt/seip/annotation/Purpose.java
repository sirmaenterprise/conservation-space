package com.sirma.itt.seip.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Defines specific purpose to something.
 *
 * @author A. Kunchev
 */
@Qualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ METHOD, TYPE, FIELD, PARAMETER })
public @interface Purpose {

	/**
	 * Defines the purpose.
	 *
	 * @return specified purpose
	 */
	String value();
}