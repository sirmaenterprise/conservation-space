package com.sirma.itt.seip.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.interceptor.InterceptorBinding;

/**
 * Marks a method or type for security intercepting.<br>
 * The annotated type or method will be executed in the context of the admin user.
 *
 * @author smustafov
 */
@Inherited
@Documented
@InterceptorBinding
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface RunAsAdmin {
	// no need
}
