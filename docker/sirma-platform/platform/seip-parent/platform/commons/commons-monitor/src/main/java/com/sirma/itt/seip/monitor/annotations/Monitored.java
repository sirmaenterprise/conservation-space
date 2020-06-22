package com.sirma.itt.seip.monitor.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

/**
 * Marks a method as "monitored". The annotation defines metrics to be
 * registered for the method.
 *
 * @author yasko
 */
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Monitored {

	/**
	 * Metrics to be registered.
	 *
	 * @return metric definitions to be registered.
	 */
	@Nonbinding
	MetricDefinition[] value() default {};
}
