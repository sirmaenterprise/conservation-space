package com.sirma.itt.seip.configuration;

import java.io.Serializable;
import java.net.URI;
import java.time.ZoneId;

/**
 * Expose the default system configuration that are globally valid.
 *
 * @author bbanchev
 */
public interface SystemConfiguration extends Serializable {

	/**
	 * Gets the tenant specific system language.
	 *
	 * @return the system language
	 */
	default String getSystemLanguage() {
		return getSystemLanguageConfiguration().get();
	}

	/**
	 * Configuration for the system's language. The configuration is tenant specific.
	 *
	 * @return the system language
	 */
	ConfigurationProperty<String> getSystemLanguageConfiguration();

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

	/**
	 * Gets the public REST access url with emf. <br>
	 * {@code <protocol>://<host>:<port>/emf/api}
	 *
	 * @return access url for remote rest services
	 */
	ConfigurationProperty<URI> getRESTRemoteAccessUrl();

	/**
	 * Gets the timezone ID.
	 *
	 * @return the timezone ID
	 */
	ConfigurationProperty<ZoneId> getTimeZoneID();

	/**
	 * Gets the help support email address.
	 *
	 * @return email address
	 */
	ConfigurationProperty<String> getHelpSuportEmail();

	/**
	 * Gets the default server protocol.
	 *
	 * @return The configured server protocol. Default http.
	 */
	ConfigurationProperty<String> getDefaultProtocol();

	/**
	 * Gets the default server address or hostname.
	 *
	 * @return The configured server address or null if not provided.
	 */
	ConfigurationProperty<String> getDefaultHost();

	/**
	 * Gets the default server port.
	 *
	 * @return The configured server port. Default 8080.
	 */
	ConfigurationProperty<Integer> getDefaultPort();

	/**
	 * Gets the configured context path.
	 *
	 * @return The configured context path. Default '/emf'.
	 */
	ConfigurationProperty<String> getDefaultContextPath();
}
