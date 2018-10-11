package com.sirma.itt.seip.monitor;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.CachingSupplier;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.time.TimeTracker;

/**
 * Produces and caches the statistics objects.
 *
 * @author BBonev
 */
@ApplicationScoped
public class StatisticsFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsFactory.class);

	/**
	 * Defines if the external statistics are active or not. If any external implementation is found it will be
	 * activated by default. <b>Default value: true</b>
	 */
	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "statistics.enabled", system = true, type = Boolean.class, defaultValue = "true", sensitive = true, label = "Defines if the external statistics are active or not. If any external implementation is found it will be activated by default.")
	private ConfigurationProperty<Boolean> statisticsEnabledExternal;

	/** The statistics. */
	private StatisticsProxy statistics;

	/**
	 * Produce.
	 *
	 * @return the statistics
	 */
	@Produces
	public Statistics produce() {
		return getCachedInstance();
	}

	/**
	 * Creates a new Statistics object.
	 *
	 * @return the statistics
	 */
	protected Statistics createInstance() {
		if (!statisticsEnabledExternal.get().booleanValue()) {
			LOGGER.info("External statistics collection has been disabled. Using implementation: {}"
					+ NoOpStatistics.class.getName());
			return new NoOpStatistics();
		}
		ServiceLoader<StatisticsProvider> serviceLoader = ServiceLoader.load(StatisticsProvider.class);
		Iterator<StatisticsProvider> it = serviceLoader.iterator();
		Statistics actualInstance;
		if (it.hasNext()) {
			StatisticsProvider statisticsProvider = it.next();
			LOGGER.info("Using provider implementation: " + statisticsProvider.getClass().getName());
			actualInstance = statisticsProvider.provide();
			LOGGER.info("Detected registered statistics implementation: " + actualInstance.getClass().getName());
		} else {
			actualInstance = new NoOpStatistics();
			LOGGER.info("No statistics implementation found, using: " + NoOpStatistics.class.getName());
		}
		return actualInstance;
	}

	/**
	 * Gets the cached instance.
	 *
	 * @return the cached instance
	 */
	protected Statistics getCachedInstance() {
		if (statistics == null) {
			statistics = new StatisticsProxy(this::createInstance);
			statisticsEnabledExternal.addConfigurationChangeListener(c -> statistics.reset());
		}
		return statistics;
	}

	/**
	 * Proxy object to wrap the actual statistics object
	 *
	 * @author BBonev
	 */
	private static class StatisticsProxy extends CachingSupplier<Statistics>implements Statistics {

		/**
		 * Instantiates a new statistics proxy.
		 *
		 * @param proxyTarget
		 *            the proxy target
		 */
		public StatisticsProxy(Supplier<Statistics> proxyTarget) {
			super(proxyTarget);
		}

		@Override
		public boolean areStatisticsEnabled() {
			return get().areStatisticsEnabled();
		}

		@Override
		public void updateMeter(Class<?> caller, String functionName) {
			get().updateMeter(caller, functionName);
		}

		@Override
		public TimeTracker createTimeStatistics(Class<?> caller, String functionName) {
			return get().createTimeStatistics(caller, functionName);
		}

		@Override
		public void registerHealthCheck(String systemName, Callable<Boolean> checker) {
			get().registerHealthCheck(systemName, checker);
		}

		@Override
		public void logTrend(Class<?> caller, String functionName, Object value) {
			get().logTrend(caller, functionName, value);
		}

		@Override
		public StatCounter getCounter(Class<?> caller, String functionName) {
			return get().getCounter(caller, functionName);
		}
	}

}
