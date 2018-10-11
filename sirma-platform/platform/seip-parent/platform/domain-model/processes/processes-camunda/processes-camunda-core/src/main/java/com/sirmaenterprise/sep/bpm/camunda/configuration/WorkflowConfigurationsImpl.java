/**
 *
 */
package com.sirmaenterprise.sep.bpm.camunda.configuration;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;

/**
 * Provides concrete implementation of {@link WorkflowConfigurations}. It contains the definitions of the related
 * configurations.
 *
 * @author BBonev
 */
@Singleton
public class WorkflowConfigurationsImpl implements WorkflowConfigurations {

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "workflow.priority.normal", label = "Codelist value for workflow service priority with level: normal")
	private ConfigurationProperty<String> priorityNormal;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "workflow.priority.low", label = "Codelist value for workflow service priority with level: low")
	private ConfigurationProperty<String> priorityLow;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "workflow.priority.high", label = "Codelist value for workflow service priority with level: high")
	private ConfigurationProperty<String> priorityHigh;

	@Override
	public String getWorkflowPriorityLow() {
		return priorityLow.get();
	}

	@Override
	public String getWorkflowPriorityNormal() {
		return priorityNormal.get();
	}

	@Override
	public String getWorkflowPriorityHigh() {
		return priorityHigh.get();
	}
}