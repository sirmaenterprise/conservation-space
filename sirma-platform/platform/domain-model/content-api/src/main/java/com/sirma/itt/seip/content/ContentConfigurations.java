package com.sirma.itt.seip.content;

import com.sirma.itt.seip.configuration.ConfigurationProperty;

/**
 * Configurations related to content functionality
 *
 * @author BBonev
 */
public interface ContentConfigurations {

	/**
	 * Gets the configuration for maximum size for file upload.
	 *
	 * @return the max file size
	 */
	ConfigurationProperty<Long> getMaxFileSize();
}
