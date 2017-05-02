package com.sirma.itt.seip.rest.annotations.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.ws.rs.NameBinding;

/**
 * Marks a REST endpoint as public. Such endpoints do not require authentication. <br>
 * If {@link #tenantParameterName()} is specified then the security context will be initialized for the given tenant.
 *
 * @author yasko
 */
@NameBinding
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PublicResource {

	/**
	 * The name of the query or path parameter that specifies the desired tenant context. If empty no tenant
	 * initialization will be done.
	 * 
	 * @return parameter name that describes a tenant identifier
	 */
	String tenantParameterName() default "";
}
