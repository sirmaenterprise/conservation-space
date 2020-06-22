package com.sirma.itt.seip.adapters.iiif;

import java.net.URI;

import com.sirma.itt.seip.adapters.remote.FTPConfiguration;
import com.sirma.itt.seip.configuration.ConfigurationProperty;

/**
 * Interface that contains the image server configuration and provides method for its access.
 *
 * @author Nikolay Ch
 */
public interface ImageServerConfigurations {

	/** The name of extensions to represent the IIIF server */
	String IIIF = "iiif";

	/**
	 * Checks if is image server enabled.
	 *
	 * @return the configuration property
	 */
	ConfigurationProperty<Boolean> isImageServerEnabled();

	/**
	 * Provides the configuration to the FTP server where images are stored.
	 *
	 * @return the configuration
	 */
	ConfigurationProperty<FTPConfiguration> getImageFTPServerConfig();

	/**
	 * Gets the image access server address. This is the base address that can be used for accessing the original image
	 * for download.
	 *
	 * @return the image access server address
	 */
	ConfigurationProperty<URI> getImageAccessServerAddress();

	/**
	 * Gets the IIIF server address. This is the base address of the IIIF server used for tale access of the images.
	 *
	 * @return the IIIF server address
	 */
	ConfigurationProperty<URI> getIiifServerAddress();

	/**
	 * Gets the IIIF context address.
	 *
	 * @return the IIIF context address
	 */
	ConfigurationProperty<URI> getIiifContextAddress();

	/**
	 * Gets the async upload threshold.
	 *
	 * @return the async upload threshold
	 */
	ConfigurationProperty<Long> getAsyncUploadThreshold();

	/**
	 * Gets the name of the default image to be displayed until the actual image is converted and available.
	 *
	 * @return the default image name
	 */
	ConfigurationProperty<String> getDefaultImageName();

	/**
	 * Gets the name of the fallback image to be displayed if the content of the currently selected object is not.
	 * available.
	 *
	 * @return the no content fallback image name
	 */
	ConfigurationProperty<String> getNoContentImageName();

	/**
	 * Gets the IIIF server address suffix to be added after the image name.
	 *
	 * @return the iiif server address suffix
	 */
	ConfigurationProperty<String> getIiifServerAddressSuffix();

	/**
	 * Gets the maximum width of image returned from the preview service for image instances.
	 *
	 * @return The width
	 */
	ConfigurationProperty<Integer> getMaximumWidthForPreview();
}
