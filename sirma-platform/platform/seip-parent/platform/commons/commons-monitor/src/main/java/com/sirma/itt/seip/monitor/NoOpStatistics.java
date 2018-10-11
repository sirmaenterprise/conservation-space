package com.sirma.itt.seip.monitor;

import java.util.concurrent.Callable;

import com.sirma.itt.seip.annotation.NoOperation;
import com.sirma.itt.seip.time.TimeTracker;

/**
 * Empty statistics object. This is default implementation. If the statistics module is not present.
 *
 * @author BBonev
 */
@NoOperation
@StatisticsImplementation
public class NoOpStatistics implements Statistics {

	private static final StatCounter STAT_COUNTER = new NoOpStatCounter();

	@Override
	public boolean areStatisticsEnabled() {
		return false;
	}

	@Override
	public void updateMeter(Class<?> caller, String functionName) {
		// nothing to do
	}

	@Override
	public TimeTracker createTimeStatistics(Class<?> caller, String functionName) {
		return new TimeTracker();
	}

	@Override
	public void registerHealthCheck(String systemName, Callable<Boolean> checker) {
		// nothing to do
	}

	@Override
	public void logTrend(Class<?> caller, String functionName, Object value) {
		// nothing to do
	}

	@Override
	public StatCounter getCounter(Class<?> caller, String functionName) {
		return STAT_COUNTER;
	}
}
