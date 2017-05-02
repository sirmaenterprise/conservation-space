package com.sirma.itt.seip.eai.configuration;

import com.sirma.itt.seip.Named;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.eai.service.communication.CommunicationConfiguration;
import com.sirma.itt.seip.eai.service.model.ModelConfiguration;
import com.sirma.itt.seip.eai.service.search.SearchModelConfiguration;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * The {@link EAIConfigurationProvider} is extension to provide a configuration for single specific subsystem and for
 * any tenant.
 *
 * @author bbanchev
 */
public interface EAIConfigurationProvider extends Plugin, Named {
	/** Plugin name. */
	String NAME = "EAIConfigurationProvider";

	/**
	 * Gets the client id - system+tenant as Uri object.
	 *
	 * @return the client id
	 */
	ConfigurationProperty<String> getSystemClientId();

	/**
	 * Gets the communication configuration.
	 *
	 * @return the communication configuration
	 */
	ConfigurationProperty<CommunicationConfiguration> getCommunicationConfiguration();

	/**
	 * Gets the model configuration.
	 *
	 * @return the model configuration
	 */
	ConfigurationProperty<ModelConfiguration> getModelConfiguration();

	/**
	 * Gets the search model configuration.
	 *
	 * @return the search model configuration
	 */
	ConfigurationProperty<SearchModelConfiguration> getSearchConfiguration();

	/**
	 * Checks if subsystem is enabled
	 *
	 * @return true if it is, false otherwise
	 */
	ConfigurationProperty<Boolean> isEnabled();

	/**
	 * Checks if subsystem is service that is visible in the UI
	 *
	 * @return true if it is, false otherwise
	 */
	default Boolean isUserService() {
		return Boolean.TRUE;
	}
}
