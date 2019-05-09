package com.sirma.itt.seip.monitor.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a metric to be registered.
 *
 * @author yasko
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.ANNOTATION_TYPE })
public @interface MetricDefinition {
	public enum Type {
		HISTOGRAM, TIMER, COUNTER, GAUGE
	}

	/**
	 * Name of the metric to be registered.
	 *
	 * @return base name of the metric.
	 */
	String name();

	/**
	 * Type of the metric e.g. histogram, counter, etc.
	 *
	 * @return type of the metric.
	 */
	Type type();

	/**
	 * Short description of the metric.
	 *
	 * @return Metric description.
	 */
	String descr() default "";

	/**
	 * Configuration for the metric.
	 *
	 * @return metric configurations.
	 */
	MetricConfig[] configs() default {};
}
