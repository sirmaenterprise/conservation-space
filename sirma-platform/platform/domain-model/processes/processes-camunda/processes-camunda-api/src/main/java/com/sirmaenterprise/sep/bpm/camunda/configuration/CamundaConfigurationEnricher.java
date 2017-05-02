package com.sirmaenterprise.sep.bpm.camunda.configuration;

import org.camunda.bpm.engine.ProcessEngine;

import com.sirma.itt.seip.plugin.Plugin;

/**
 * Plugin to enrich single {@link ProcessEngine} configuration
 * 
 * @author bbanchev
 */
public interface CamundaConfigurationEnricher extends Plugin {
	/** The plugin target point. */
	String TARGET = "CamundaConfigurationEnricher";

	/**
	 * Invokes the actual enriching. <strong> Previous default values might be overridden</strong>
	 *
	 * @param configuration
	 *            the configuration to enrich
	 */
	public void enrich(SepProcessEngineConfigurator configuration);
}
