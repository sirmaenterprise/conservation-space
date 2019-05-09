package com.sirma.itt.seip.monitor;

import com.sirma.itt.seip.monitor.annotations.StatisticsImplementation;

/**
 * Provides the contract for registering and updating application metrics.
 * Implementations must be annotated with {@link StatisticsImplementation}.
 *
 * @author yasko
 */
public interface Statistics {

	/**
	 * Begin tracking of a metric definition e.g. for timers here we start the
	 * timer. If the metric is not already created - it should create it.
	 *
	 * @param def
	 *            {@link Metric} definition to track.
	 */
	void track(Metric def);

	/**
	 * Retrieves a metric value from the current thread context.
	 *
	 * @param metric
	 *            Metric name.
	 * @return Metric value.
	 */
	Number value(String metric);

	/**
	 * Sets metric value. The value must be kept in a thread local context.
	 *
	 * @param metric
	 *            Metric name.
	 * @param value
	 *            Metric value.
	 */
	void value(String metric, Number value);

	/**
	 * End tracking of a metric and register it's value e.g. for timers here we
	 * stop it and register the time, for counter/gauges we could inc/dec. If
	 * the metric is not already created - it should create it.
	 *
	 * @param def
	 *            {@link Metric} definition to register a value for.
	 */
	void end(Metric def);

}
