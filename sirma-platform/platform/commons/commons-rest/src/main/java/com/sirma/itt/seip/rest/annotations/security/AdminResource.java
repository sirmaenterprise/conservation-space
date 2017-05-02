package com.sirma.itt.seip.rest.annotations.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.ws.rs.NameBinding;

/**
 * Marks REST endpoint to be accessible only by admin user. Such endpoints require authenticated user with administrator
 * permissions. Can be used on methods and classes.
 *
 * @author smustafov
 */
@NameBinding
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface AdminResource {
	// marker only
}
