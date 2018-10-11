package com.sirma.itt.seip.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method to be called after new tenant has been added or activated in the system.
 * <p>
 * This should be used to initialize components that have some specific logic that need to be executed for each tenant.
 * This is intended for use in combination of {@link RunAsAllTenantAdmins} when something must called and for new
 * tenants during live application without the need to be restarted.
 * <p>
 * If tenant module is not present then this annotation does not affect anything.
 * <p>
 * The annotated method will only be called if new tenant is added while application is running
 *
 * @author BBonev
 */
@Inherited
@Documented
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface OnTenantAdd {

	/**
	 * Defines execution order of the components. Components with lower order will be executed before the ones with
	 * higher order. Components with the same order will be executed at random order. The default value is zero, which
	 * means components will be executed after the ones with negative order and before the ones with positive order.
	 *
	 * @return order of the component
	 */
	double order() default 0;

}
