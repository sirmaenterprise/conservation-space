/**
 *
 */
package com.sirma.itt.semantic.configuration;

import org.eclipse.rdf4j.repository.Repository;

import com.sirma.itt.seip.configuration.ConfigurationProperty;

/**
 * Defines configurations for semantic module.
 *
 * @author BBonev
 */
public interface SemanticConfiguration {

	/**
	 * Gets the repository url configuration.
	 *
	 * @return the repository url configuration
	 */
	String getServerURLConfiguration();

	/**
	 * Gets the repository name configuration.
	 *
	 * @return the repository name configuration
	 */
	String getRepositoryNameConfiguration();

	/**
	 * Gets the repository access user name configuration.
	 *
	 * @return the repository access user name configuration
	 */
	String getRepositoryAccessUserNameConfiguration();

	/**
	 * Gets the repository access user password configuration.
	 *
	 * @return the repository access user password configuration
	 */
	String getRepositoryAccessUserPasswordConfiguration();

	/**
	 * Gets the repository.
	 *
	 * @return the repository
	 */
	ConfigurationProperty<Repository> getRepository();

	/**
	 * Gets the full text search index name.
	 *
	 * @return the ftsIndexName property
	 */
	ConfigurationProperty<String> getFtsIndexName();

	/**
	 * Gets the name of the root class in the system.
	 * 
	 * @return the property for root class name
	 */
	ConfigurationProperty<String> getRootClassName();

	/**
	 * Gets the server url configuration property.
	 * 
	 * @return the server url
	 */
	ConfigurationProperty<String> getServerURL();

	/**
	 * Gets the repository name configuration property.
	 * 
	 * @return the repository name
	 */
	ConfigurationProperty<String> getRepositoryName();
}