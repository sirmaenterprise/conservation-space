package com.sirmaenterprise.sep.monitor.dropwizard;

import java.util.concurrent.Callable;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.monitor.StatCounter;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.monitor.StatisticsImplementation;
import com.sirma.itt.seip.time.TimeTracker;

/**
 * Statistics implementation for Codahale Dropwizard Metrics library
 *
 * @author BBonev
 */
@StatisticsImplementation
public class CodahaleMetrics implements Statistics {
	private static final MetricRegistry METRICS = new MetricRegistry();
	private static final HealthCheckRegistry HEALTH_CHECK_REGISTRY = new HealthCheckRegistry();

	/**
	 * Getter method for metrics registry
	 *
	 * @return the metrics
	 */
	public static MetricRegistry getMetrics() {
		return METRICS;
	}

	/**
	 * Gets the health check registry
	 *
	 * @return the health check
	 */
	public static HealthCheckRegistry getHealthCheck() {
		return HEALTH_CHECK_REGISTRY;
	}

	@Override
	public boolean areStatisticsEnabled() {
		return true;
	}

	@Override
	public void updateMeter(Class<?> caller, String functionName) {
		METRICS.meter(getName(caller, functionName)).mark();
	}

	/**
	 * Gets the name.
	 *
	 * @param caller
	 *            the caller
	 * @param functionName
	 *            the function name
	 * @return the name
	 */
	private static String getName(Class<?> caller, String functionName) {
		if (caller == null) {
			return functionName;
		}
		return MetricRegistry.name(caller.getSimpleName(), functionName);
	}

	@Override
	public TimeTracker createTimeStatistics(Class<?> caller, String functionName) {
		if (areStatisticsEnabled()) {
			return new MetricTimer(METRICS.timer(getName(caller, functionName)));
		}
		return new TimeTracker();
	}

	@Override
	public void registerHealthCheck(final String systemName, final Callable<Boolean> checker) {
		if (checker == null) {
			throw new EmfRuntimeException("Cannot register null health check callable!");
		}
		HealthCheck healthCheck = new HealthCheckExtension(systemName, checker);
		HEALTH_CHECK_REGISTRY.register(getName(null, systemName), healthCheck);
	}

	@Override
	public void logTrend(Class<?> caller, String functionName, Object value) {
		if (value instanceof Long) {
			METRICS.histogram(getName(caller, functionName)).update(((Long) value).longValue());
		} else if (value instanceof Integer) {
			METRICS.histogram(getName(caller, functionName)).update(((Integer) value).intValue());
		}
	}

	/**
	 * Getter method for healthChecks.
	 *
	 * @return the healthChecks
	 */
	public HealthCheckRegistry getHealthChecks() {
		return HEALTH_CHECK_REGISTRY;
	}

	@Override
	public StatCounter getCounter(Class<?> caller, String functionName) {
		final Counter counter = METRICS.counter(getName(caller, functionName));
		return new StatCounterImplementation(counter);
	}

	/**
	 * Proxy class for the {@link HealthCheck} instance to wrap a {@link Callable} object as {@link HealthCheck}.
	 *
	 * @author BBonev
	 */
	private static final class HealthCheckExtension extends HealthCheck {
		/**
		 * Comment for systemName.
		 */
		private final String systemName;
		/**
		 * Comment for checker.
		 */
		private final Callable<Boolean> checker;

		/**
		 * Instantiates a new health check extension.
		 *
		 * @param systemName
		 *            the system name
		 * @param checker
		 *            the checker
		 */
		private HealthCheckExtension(String systemName, Callable<Boolean> checker) {
			this.systemName = systemName;
			this.checker = checker;
		}

		@Override
		protected Result check() throws Exception {
			try {
				if (checker.call()) {
					return HealthCheck.Result.healthy();
				}
			} catch (Exception e) {
				return HealthCheck.Result.unhealthy(e);
			}
			return HealthCheck.Result.unhealthy(systemName + " is failing");
		}
	}

	/**
	 * {@link StatCounter} proxy for the actual {@link Counter} object.
	 *
	 * @author BBonev
	 */
	private static final class StatCounterImplementation implements StatCounter {
		/**
		 * Comment for counter.
		 */
		private final Counter counter;

		/**
		 * @param counter
		 */
		private StatCounterImplementation(Counter counter) {
			this.counter = counter;
		}

		@Override
		public void increment(int amount) {
			counter.inc(amount);
		}

		@Override
		public void increment() {
			counter.inc();
		}

		@Override
		public void decrement(int amount) {
			counter.dec(amount);
		}

		@Override
		public void decrement() {
			counter.dec();
		}
	}

}
