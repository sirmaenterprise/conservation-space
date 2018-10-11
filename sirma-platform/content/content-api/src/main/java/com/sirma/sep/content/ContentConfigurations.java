package com.sirma.sep.content;

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

	/**
	 * Gets the configuration for maximum concurrent file upload requests.
	 *
	 * @return the max file upload concurrent requests
	 */
	ConfigurationProperty<Integer> getMaxConcurrentUploads();

	/**
	 * Gets the configuration for the number of files that are allowed to be uploaded at once.
	 * Property used in the UI. Determines the limit of displayed files for upload at once.
	 *
	 * @return the limit
	 */
	ConfigurationProperty<Integer> getMaxFilesToUploadAtOnce();

	/**
	 * Gets the configuration for the share code secret key.
	 * 
	 * @return the share code secret key configuration
	 */
	ConfigurationProperty<String> getShareCodeSecretKey();
}
