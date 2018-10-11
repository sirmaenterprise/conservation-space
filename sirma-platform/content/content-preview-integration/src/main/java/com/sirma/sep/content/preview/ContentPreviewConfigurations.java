package com.sirma.sep.content.preview;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Configuration properties for the content preview integration module.
 *
 * @author Mihail Radkov
 */
@Singleton
public class ContentPreviewConfigurations {

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "content.preview.remote.enabled", subSystem = "content",
			defaultValue = "false", type = Boolean.class,
			label = "Feature toggle for the remote content preview service integration, true for enabled and false for"
					+ " disabled. By default it is false")
	private ConfigurationProperty<Boolean> integrationEnabled;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "content.preview.remote.address", system = true, subSystem = "content",
			defaultValue = "localhost:8300",
			label = "Address to the remote content preview service. Expected format is <url or ip>:<port>. Default "
					+ "value is localhost:8300")
	private ConfigurationProperty<String> previewServiceAddress;

	public ConfigurationProperty<Boolean> isIntegrationEnabled() {
		return integrationEnabled;
	}

	public ConfigurationProperty<String> getPreviewServiceAddress() {
		return previewServiceAddress;
	}

}
