package com.sirma.itt.seip.configuration;

import java.io.Serializable;
import java.net.URI;

/**
 * Expose the default system configuration that are globally valid.
 *
 * @author bbanchev
 */
public interface SystemConfiguration extends Serializable {
	/**
	 * "If application is in development mode or not"
	 */
	String APPLICATION_MODE_DEVELOPMENT = "application.mode.development";

	/**
	 * Gets the system language.
	 *
	 * @return the system language
	 */
	String getSystemLanguage();

	/**
	 * Gets the public system access url. This is a link to the application that is accessible by users. It may be a
	 * proxy address.
	 *
	 * @return the system access url
	 */
	ConfigurationProperty<URI> getSystemAccessUrl();

	/**
	 * Gets the public REST access url. It may be a proxy address. The address is sub address from the
	 * {@link #getSystemAccessUrl()}
	 *
	 * @return the REST access url
	 */
	ConfigurationProperty<URI> getRESTAccessUrl();

	/**
	 * Gets the application mode.
	 *
	 * @return the application mode
	 */
	ConfigurationProperty<Boolean> getApplicationMode();

	/**
	 * Gets the ui2 url.
	 *
	 * @return the ui2 url
	 */
	ConfigurationProperty<String> getUi2Url();

	/**
	 * Gets the application name.
	 *
	 * @return the application name
	 */
	ConfigurationProperty<String> getApplicationName();

}
