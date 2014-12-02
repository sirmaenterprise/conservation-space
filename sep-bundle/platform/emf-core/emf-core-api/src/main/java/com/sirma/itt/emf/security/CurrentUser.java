package com.sirma.itt.emf.security;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Currently authenticated user.
 * 
 * @author Adrian Mitev
 */
@Qualifier
@Inherited
@Target({ METHOD, PARAMETER, FIELD })
@Retention(RUNTIME)
@Documented
public @interface CurrentUser {

}
