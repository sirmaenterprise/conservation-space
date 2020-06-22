/**
 *
 */
package com.sirma.itt.seip.adapters;

import java.net.URI;

import com.sirma.itt.seip.configuration.ConfigurationProperty;

/**
 * DMS specific configurations
 *
 * @author BBonev
 */
public interface AdaptersConfiguration {

	/**
	 * Gets the id of the container in DMS system. This should uniquely identify the store in DMS. This configuration is
	 * per tenant.
	 *
	 * @return the dms container id
	 */
	ConfigurationProperty<String> getDmsContainerId();

	/**
	 * Gets the DMS address as {@link URI}
	 *
	 * @return the dms address
	 */
	ConfigurationProperty<URI> getDmsAddress();

	/**
	 * Gets the dms protocol configuration id
	 *
	 * @return the dms protocol configuration
	 */
	String getDmsProtocolConfiguration();

	/**
	 * Gets the dms host configuration id
	 *
	 * @return the dms host configuration
	 */
	String getDmsHostConfiguration();

	/**
	 * Gets the dms port configuration id
	 *
	 * @return the dms port configuration
	 */
	String getDmsPortConfiguration();

	/**
	 * Returns {@link ConfigurationProperty} for configuring alfresco if its enabled as content store.
	 *
	 * @return {@link ConfigurationProperty} which tells if alfresco is enabled as content store
	 */
	ConfigurationProperty<Boolean> getAlfrescoStoreEnabled();

	/**
	 * Returns {@link ConfigurationProperty} for configuring alfresco if its enabled as view store.
	 *
	 * @return {@link ConfigurationProperty} which tells if alfresco is enabled as view store
	 */
	ConfigurationProperty<Boolean> getAlfrescoViewStoreEnabled();
}
