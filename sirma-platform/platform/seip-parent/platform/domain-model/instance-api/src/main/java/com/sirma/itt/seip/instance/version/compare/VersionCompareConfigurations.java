package com.sirma.itt.seip.instance.version.compare;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import com.sirma.itt.seip.configuration.ConfigurationProperty;

/**
 * Provides the way of retrieving configurations related to version content compare.
 *
 * @author A. Kunchev
 */
public interface VersionCompareConfigurations {

	/**
	 * Gets the base URL for accessing the external service that will be used to compare versions contents.
	 *
	 * @return link to the external service that provides features used in version content compare
	 */
	ConfigurationProperty<URI> getServiceBaseUrl();

	/**
	 * Retrieves the expiration time configuration for the result file after successful versions compare. This
	 * configuration is used to schedule timed task, which will delete the result file after versions compare. The
	 * default value is 24. The configurations is meant to be used along with {@link TimeUnit#HOURS}.
	 *
	 * @return integer value representing the hours after which the result file from versions compare should be deleted
	 */
	int getExpirationTime();

}
