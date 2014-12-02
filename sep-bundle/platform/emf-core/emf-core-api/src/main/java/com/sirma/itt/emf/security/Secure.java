package com.sirma.itt.emf.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

/**
 * Marks a method or type for security intercepting.<br>
 * Effectively enables a method to be able to access the underlying DM system
 * using the access rights of the currently logged in user.
 *
 * @author BBonev
 */
@InterceptorBinding
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Secure {

	/**
	 * Override the current user credentials and execute the method as admin in DMS.
	 * 
	 * @deprecated Do not use this option for now use {@link #runAsSystem()} instead
	 */
	@Deprecated
	@Nonbinding
	boolean runAsAdmin() default false;

	/**
	 * Override the current user credentials and execute the method as admin in
	 * DMS and System user for real authentication. For use in asynchronous
	 * calls.
	 */
	@Nonbinding
	boolean runAsSystem() default false;
}
