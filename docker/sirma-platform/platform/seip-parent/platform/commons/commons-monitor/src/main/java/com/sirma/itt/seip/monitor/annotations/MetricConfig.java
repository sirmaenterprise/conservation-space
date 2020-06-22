package com.sirma.itt.seip.monitor.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a configuration key/value pair to set on a metric.
 *
 * @author yasko
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
public @interface MetricConfig {

	/**
	 * Metric configuration key.
	 *
	 * @return the key of the configuration.
	 */
	String key();

	/**
	 * Metric configuration value.
	 *
	 * @return the value of the configuration.
	 */
	String value();
}
