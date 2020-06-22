package com.sirma.itt.seip.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method or type for security intercepting.
 * <p>
 * This annotation should be used only for void methods. Any non void method annotated with {@link RunAsAllTenantAdmins}
 * will return <code>null</code>.
 * <p>
 * If tenant module is present this annotation should trigger invocation of the annotated method ones for each
 * registered active tenant. This could be changed with enabling the property {@link #includeInactive()} Setting it to
 * <code>true</code> will result invoking the annotated method for all possible tenants.<br>
 * Effectively the annotated method will be invoked in security context authenticated as the tenant administrator of
 * each active/inactive tenant so the method will be called multiple times.<br>
 * <b>NOTE:</b> This annotation should be used with care when placed on a bean or bean method that is stateful!
 * <p>
 * If tenant module is not present this annotation should behave as {@link RunAsSystem}.
 *
 * @author BBonev
 */
@Inherited
@Documented
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface RunAsAllTenantAdmins {
	/**
	 * Specifies if the per tenant executions should be done in parallel. Default behavior is in parallel.
	 * 
	 * @return if different tenants should be run in parallel
	 */
	boolean parallel() default true;

	/**
	 * Include inactive tenants when invoking the annotated method. Default value is <code>false</code>.
	 * <p>
	 * This should be activated with care because if for some reason there is missing resource (database) for inactive
	 * tenant this may lead to errors.
	 * 
	 * @return if inactive tenants should be called as well
	 */
	boolean includeInactive() default false;
}
