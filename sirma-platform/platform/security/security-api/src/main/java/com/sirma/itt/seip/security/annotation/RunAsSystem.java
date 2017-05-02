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
 * The annotated type or method will be executed in the context of the system user outside tenant scope if tenants
 * module is present.
 * <p>
 *
 * @author BBonev
 */
@Inherited
@Documented
@InterceptorBinding
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface RunAsSystem {

	/**
	 * Protect current tenant. If <code>true</code> <i>(default value)</i> and there is active tenant context it will be
	 * protected and the annotated method will be invoked in the same tenant context but with system privileges.<br>
	 * If <code>false</code> the annotated method will be executed in no tenant (system) context. This means that the
	 * code cannot access particular tenant.
	 *
	 * @return if the operation should be run in the current tenant
	 */
	boolean protectCurrentTenant() default true;
}
