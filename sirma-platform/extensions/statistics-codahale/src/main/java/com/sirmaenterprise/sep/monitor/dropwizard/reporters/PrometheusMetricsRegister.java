package com.sirmaenterprise.sep.monitor.dropwizard.reporters;

import java.lang.invoke.MethodHandles;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirmaenterprise.sep.monitor.dropwizard.CodahaleMetrics;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;

/**
 * Register of Dropwizard Metrics reporter to Prometheus store. There is a configuration that controls if the reporter
 * is active or not.
 *
 * @author BBonev
 */
@ApplicationScoped
public class PrometheusMetricsRegister {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final DropwizardExports DROPWIZARD_EXPORTS = new DropwizardExports(CodahaleMetrics.getMetrics());

	@ConfigurationPropertyDefinition(type = Boolean.class, defaultValue = "false", system = true, subSystem = "metrics", label = "Defines if the Promethus reporter is active or not")
	private static final String ENABLE_PROMETHUS_REPORTER = "metrics.prometheus.enabled";

	@Inject
	@Configuration(ENABLE_PROMETHUS_REPORTER)
	private ConfigurationProperty<Boolean> dropWizardToPrometheusExporter;

	@Startup
	void initialize() {
		// when disabled we will unregister it
		dropWizardToPrometheusExporter.addConfigurationChangeListener(newValue -> {
			// depending on the new value will register or unregister the reporter
			if (Boolean.TRUE.equals(newValue.get())) {
				CollectorRegistry.defaultRegistry.register(DROPWIZARD_EXPORTS);
				LOGGER.info("Enabled Prometheus metrics reporter");
			} else {
				CollectorRegistry.defaultRegistry.unregister(DROPWIZARD_EXPORTS);
				LOGGER.info("Disabled Prometheus metrics reporter");
			}
		});
		// this will trigger initial registration if enabled
		Boolean isEnabled = dropWizardToPrometheusExporter.get();
		if (Boolean.TRUE.equals(isEnabled)) {
			CollectorRegistry.defaultRegistry.register(DROPWIZARD_EXPORTS);
		}
	}
}
