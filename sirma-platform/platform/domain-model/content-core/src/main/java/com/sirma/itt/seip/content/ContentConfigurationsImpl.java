package com.sirma.itt.seip.content;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;

/**
 * Default content configurations definitions
 * 
 * @author BBonev
 */
@Singleton
public class ContentConfigurationsImpl implements ContentConfigurations {
	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "file.upload.maxsize", type = Long.class, defaultValue = "10485760", label = "Max file size allowed for upload in bytes.")
	private ConfigurationProperty<Long> maxUploadSize;

	@Override
	public ConfigurationProperty<Long> getMaxFileSize() {
		return maxUploadSize;
	}
}
