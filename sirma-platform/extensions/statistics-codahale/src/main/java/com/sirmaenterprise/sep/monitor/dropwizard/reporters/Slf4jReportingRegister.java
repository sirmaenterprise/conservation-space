package com.sirmaenterprise.sep.monitor.dropwizard.reporters;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.Slf4jReporter.LoggingLevel;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationGroupDefinition;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.GroupConverterContext;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirmaenterprise.sep.monitor.dropwizard.CodahaleMetrics;

/**
 * Slf4j logger reporter for the statistics.
 *
 * @author BBonev
 */
@ApplicationScoped
public class Slf4jReportingRegister {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/** Defines if the logging should be enabled. <b> Default value: false</b> */
	@ConfigurationPropertyDefinition(type = Boolean.class, defaultValue = "false", sensitive = true, system = true, label = "Defines if the logging should be enabled. <b> Default value: false</b>")
	private static final String LOGGING_ENABLED = "metrics.logging.enabled";
	/** The logger to use. <b>Default value: metrics</b> */
	@ConfigurationPropertyDefinition(defaultValue = "metrics", sensitive = true, system = true, label = "The logger to use. <b>Default value: metrics</b>")
	private static final String LOGGING_LOGGER = "metrics.logging.logger";
	/** The logging report timeout in milliseconds. <b>Default value: 5000</b> */
	@ConfigurationPropertyDefinition(type = Long.class, defaultValue = "5000", sensitive = true, system = true, label = "The logging report timeout in milliseconds. <b>Default value: 5000</b>")
	private static final String LOGGING_REPORT_TIMEOUT = "metrics.logging.timeout";
	/** The logging level to use. <b>Default value: DEBUG</b> */
	@ConfigurationPropertyDefinition(defaultValue = "DEBUG", sensitive = true, system = true, label = "The logging level to use. <b>Default value: DEBUG")
	private static final String LOGGING_LEVEL = "metrics.logging.level";

	@ConfigurationGroupDefinition(type = Slf4jReporter.class, system = true, properties = { LOGGING_ENABLED,
			LOGGING_LOGGER, LOGGING_REPORT_TIMEOUT, LOGGING_LEVEL })
	private static final String LOGGING_INSTANCE = "metrics.logging.instance";

	@Inject
	@Configuration(LOGGING_INSTANCE)
	private ConfigurationProperty<Slf4jReporter> reporter;

	/**
	 * Register slf4j reporter
	 */
	@Startup(async = true)
	public void register() {
		reporter.get();
		reporter.addValueDestroyListener(Slf4jReportingRegister::stopReporter);
		// force property building on change
		reporter.addConfigurationChangeListener(c -> c.get());
	}

	@ConfigurationConverter(LOGGING_INSTANCE)
	static Slf4jReporter buildReporterInstance(GroupConverterContext context) {
		Boolean enabled = context.get(LOGGING_ENABLED);
		if (!enabled.booleanValue()) {
			return null;
		}
		String loggerName = context.get(LOGGING_LOGGER);
		String loggerLevel = context.get(LOGGING_LEVEL);
		Long logTimeout = context.get(LOGGING_REPORT_TIMEOUT);
		Slf4jReporter reporter = Slf4jReporter
				.forRegistry(CodahaleMetrics.getMetrics())
					.withLoggingLevel(LoggingLevel.valueOf(loggerLevel.toUpperCase()))
					.outputTo(LoggerFactory.getLogger(loggerName))
					.convertDurationsTo(TimeUnit.MILLISECONDS)
					.convertRatesTo(TimeUnit.SECONDS)
					.build();
		reporter.start(logTimeout.longValue(), TimeUnit.MILLISECONDS);
		LOGGER.info("Started SLF4J metrics reporter");
		return reporter;
	}

	private static void stopReporter(Slf4jReporter reporter) {
		if (reporter != null) {
			LOGGER.info("Stopped the SLF4J metrics reporter");
			reporter.stop();
		}
	}

	/**
	 * Unregister reporter
	 */
	@PreDestroy
	public void unregister() {
		stopReporter(reporter.get());
	}

}
