package com.sirmaenterprise.sep.bpm.camunda.configuration;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.camunda.bpm.engine.ProcessEngine;

import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;

/**
 * The service is responsible to chain invoke all {@link CamundaConfigurationEnricher} plugins to enrich the default
 * Camunda {@link ProcessEngine} configuration
 *
 * @author bbanchev
 */
@Singleton
public class CamundaConfigurationEnricherService implements CamundaConfigurationEnricher {

	@Inject
	@ExtensionPoint(value = CamundaConfigurationEnricher.TARGET)
	private Plugins<CamundaConfigurationEnricher> configurationEnrichers;

	/**
	 * Invokes the actual enriching using all loaded {@link CamundaConfigurationEnricher}. <strong> Previous default
	 * values might be overridden</strong>
	 *
	 * @param configuration
	 *            the configuration to enrich
	 */
	@Override
	public void enrich(SepProcessEngineConfigurator configuration) {
		configurationEnrichers.forEach(enricher -> enricher.enrich(configuration));
	}

}
