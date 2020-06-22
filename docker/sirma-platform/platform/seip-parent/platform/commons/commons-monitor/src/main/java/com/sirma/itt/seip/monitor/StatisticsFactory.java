package com.sirma.itt.seip.monitor;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.monitor.annotations.StatisticsImplementation;

/**
 * Produces and caches the a statistics implementation.
 *
 * @author BBonev
 * @author yasko
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

	@Inject
	@StatisticsImplementation
	private Instance<Statistics> implementations;

	private Statistics cached;

	/**
	 * Produce.
	 *
	 * @return the statistics
	 */
	@Produces
	public Statistics produce() {
		if (cached != null) {
			return cached;
		}

		if (!statisticsEnabledExternal.get().booleanValue()) {
			cached = NoOpStatistics.INSTANCE;
			return cached;
		}

		if (implementations.isAmbiguous() || implementations.isUnsatisfied()) {
			LOGGER.error("statistics are enabled, but no imementation could be found - using noop implemetation");
			cached = NoOpStatistics.INSTANCE;
			return cached;
		}

		cached = implementations.get();
		LOGGER.debug("using statistics implementation {}", cached.getClass().getName());
		return cached;
	}
}
