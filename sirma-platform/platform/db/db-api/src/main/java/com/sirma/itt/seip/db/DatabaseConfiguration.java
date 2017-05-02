/**
 *
 */
package com.sirma.itt.seip.db;

import java.net.URI;

import javax.sql.DataSource;

import com.sirma.itt.seip.configuration.ConfigurationProperty;

/**
 * Provides access to database specific configurations.
 * <p>
 * Note: The implementation is not advised to store the created data source instances.
 *
 * @author BBonev
 */
public interface DatabaseConfiguration {

	/**
	 * Gets the core data source jndi.
	 *
	 * @return the core data source jndi
	 */
	String getCoreDataSourceJndi();

	/**
	 * Gets the core data source.
	 *
	 * @return the core data source
	 */
	DataSource getCoreDataSource();

	/**
	 * Gets the data source jndi.
	 *
	 * @return the data source jndi
	 */
	String getDataSourceJndi();

	/**
	 * Gets a data source instance that points to {@link DataSource} with JNDI name {@link #getDataSourceJndi()}.
	 *
	 * @return the data source
	 */
	DataSource getDataSource();

	/**
	 * Gets the database name configuration property
	 *
	 * @return the database name configuration
	 */
	String getDatabaseNameConfiguration();

	/**
	 * Gets the database address configuration property
	 *
	 * @return the database address configuration
	 */
	String getDatabaseAddressConfiguration();

	/**
	 * Gets the database port configuration.
	 *
	 * @return the database port configuration
	 */
	String getDatabasePortConfiguration();

	/**
	 * Gets the database dialect configuration.
	 *
	 * @return the database dialect configuration
	 */
	String getDatabaseDialectConfiguration();

	/**
	 * Gets the current tenant database URI.
	 *
	 * @return the database URI
	 */
	ConfigurationProperty<URI> getDatabaseUri();

}
