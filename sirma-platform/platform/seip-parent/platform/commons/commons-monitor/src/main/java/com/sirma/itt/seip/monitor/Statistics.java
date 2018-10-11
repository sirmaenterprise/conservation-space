package com.sirma.itt.seip.monitor;

import java.util.concurrent.Callable;

import com.sirma.itt.seip.time.TimeTracker;

/**
 * Application statistics and metrics. Provides methods to update the metrics via various method. All methods require a
 * class and a name for the metrics. Both are used to identify the metric. If the class is not passed then the metric
 * will be identified only by string name. And if the name is not present a default metric will be created using the
 * called method name.
 *
 * @author BBonev
 */
public interface Statistics {

	/**
	 * Statistics instance that does not track any statistics
	 */
	Statistics NO_OP = new NoOpStatistics();

	/**
	 * Checks if the statistics are enabled or not. If not any call to all other methods will do nothing and no
	 * exception will be thrown.
	 *
	 * @return <code>true</code> if statistics are enabled.
	 */
	boolean areStatisticsEnabled();

	/**
	 * Updates the metric as called. This could be used register a some operation rate like http request rate.
	 *
	 * @param caller
	 *            caller class or <code>null</code>
	 * @param functionName
	 *            metric name
	 */
	void updateMeter(Class<?> caller, String functionName);

	/**
	 * Register a tracker object for collecting time intervals. The method will return non <code>null</code> object no
	 * matter if the statistics are enabled or not. When statistics are disabled the logged times will not be tracked.
	 *
	 * @param caller
	 *            caller class or <code>null</code>
	 * @param functionName
	 *            metric name
	 * @return {@link TimeTracker} object that is associated with the metrics context only if the statistics are
	 *         enabled. If not then the returned object could be used as normal to track time to the logger and the
	 *         statistics will not be collected.
	 */
	TimeTracker createTimeStatistics(Class<?> caller, String functionName);

	/**
	 * Register health check callback for the given sub system name. The passed callable should return <code>true</code>
	 * if the sub system check is ok and false if not
	 *
	 * @param systemName
	 *            system name to identify the health check for
	 * @param checker
	 *            to be executed when the status should be checked.
	 */
	void registerHealthCheck(String systemName, Callable<Boolean> checker);

	/**
	 * The metric could log value trends to display as histograms. The most common cases the value should be long or
	 * int.
	 *
	 * @param caller
	 *            caller class or <code>null</code>
	 * @param functionName
	 *            metric name
	 * @param value
	 *            value to add to the trend
	 */
	void logTrend(Class<?> caller, String functionName, Object value);

	/**
	 * Gets the counter.
	 *
	 * @param caller
	 *            the caller
	 * @param functionName
	 *            the function name
	 * @return the counter
	 */
	StatCounter getCounter(Class<?> caller, String functionName);
}
