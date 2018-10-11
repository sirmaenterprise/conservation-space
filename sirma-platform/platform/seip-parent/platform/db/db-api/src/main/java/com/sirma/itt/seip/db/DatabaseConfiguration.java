/**
 *
 */
package com.sirma.itt.seip.db;

import javax.sql.DataSource;

/**
 * Provides access to database specific configurations.
 * <p>
 * Note: The implementation is not advised to store the created data source instances.
 *
 * @author BBonev
 */
public interface DatabaseConfiguration extends DatabaseSettings {

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
	 * Gets the database address configuration property
	 *
	 * @return the database address configuration
	 */
	String getDatabaseAddressConfigurationName();

}