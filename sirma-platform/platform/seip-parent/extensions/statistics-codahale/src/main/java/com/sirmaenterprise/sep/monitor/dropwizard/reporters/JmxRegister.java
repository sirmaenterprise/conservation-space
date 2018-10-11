package com.sirmaenterprise.sep.monitor.dropwizard.reporters;

import java.lang.invoke.MethodHandles;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.JmxReporter;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.tasks.TransactionMode;
import com.sirmaenterprise.sep.monitor.dropwizard.CodahaleMetrics;

/**
 * Register class for JMX console to expose statistics on application start
 *
 * @author BBonev
 */
@ApplicationScoped
public class JmxRegister {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private JmxReporter reporter;

	/** Defines if the JMX reporting should be enabled. <b> Default value: false</b> */
	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "metrics.jmx.enabled", type = Boolean.class, defaultValue = "false", sensitive = true, system = true, label = "Defines if the JMX reporting should be enabled")
	private ConfigurationProperty<Boolean> enabled;

	/**
	 * Register the JMX beans at server startup
	 */
	@Startup(async = true, transactionMode = TransactionMode.NOT_SUPPORTED)
	public void register() {
		startReporter(enabled);
		enabled.addConfigurationChangeListener(this::startReporter);
	}

	private void startReporter(ConfigurationProperty<Boolean> isEnabled) {
		unregister();
		if (!isEnabled.get().booleanValue()) {
			return;
		}
		reporter = JmxReporter.forRegistry(CodahaleMetrics.getMetrics()).build();
		reporter.start();
		LOGGER.info("Activated JMX metrics reporter!");
	}

	/**
	 * Unregister at undeploy or application stop.
	 */
	@PreDestroy
	public void unregister() {
		if (reporter != null) {
			reporter.close();
			LOGGER.info("Deactivated JMX metrics reporter!");
		}
	}

}
