package com.sirma.itt.emf.audit.configuration;

import java.net.URI;

import javax.sql.DataSource;

import org.apache.solr.client.solrj.SolrClient;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.db.DatabaseSettings;

/**
 * Provides access to audit database configurations. *
 * <p>
 * Note: The implementation is not advised to store the created data source instances.
 *
 * @author BBonev
 */
public interface AuditConfiguration extends DatabaseSettings {

	/**
	 * Gets the solr client that can access audit solr core.
	 *
	 * @return the solr client
	 */
	ConfigurationProperty<SolrClient> getSolrClient();

	/**
	 * Gets the solr client that can access audit solr core for recent activities.
	 *
	 * @return the solr client
	 */
	ConfigurationProperty<SolrClient> getRecentActivitiesSolrClient();

	/**
	 * Gets the solr address.
	 *
	 * @return the solr address
	 */
	ConfigurationProperty<String> getSolrAddress();

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
	 * Gets the database address configuration property.
	 *
	 * @return the database address configuration
	 */
	String getDatabaseAddressConfigurationName();

	/**
	 * Gets the current tenant database URI.
	 *
	 * @return the database URI
	 */
	ConfigurationProperty<URI> getDatabaseUri();

	/**
	 * Gets the solr admin address. This is the basic solr admin services.
	 *
	 * @return the solr admin address
	 */
	ConfigurationProperty<String> getSolrAdminAddress();

	/**
	 * Gets the solr external admin address. This is application packed as external webapp controlling solr.
	 *
	 * @return the solr external admin address
	 */
	ConfigurationProperty<String> getSolrExternalAdminAddress();

	/**
	 * Gets the solr host configuration name.
	 *
	 * @return the solr host configuration name
	 */
	String getSolrHostConfigurationName();

	/**
	 * Gets the solr port configuration name.
	 *
	 * @return the solr port configuration name
	 */
	String getSolrPortConfigurationName();

	/**
	 * Gets the solr protocol configuration name.
	 *
	 * @return the solr protocol configuration name
	 */
	String getSolrProtocolConfigurationName();

	/**
	 * Gets the solr timeout configuration name.
	 *
	 * @return the solr timeout configuration name
	 */
	String getSolrTimeoutConfigurationName();

	/**
	 * Gets the solr core configuration name.
	 *
	 * @return the solr core configuration name
	 */
	String getSolrCoreConfigurationName();

	/**
	 * Gets the recent activities solr core configuration name.
	 *
	 * @return the recent activities solr core configuration name
	 */
	String getRecentActivitiesSolrCoreConfigurationName();

	/**
	 * Gets the recent activities label prefix.
	 *
	 * @return the recent activities label prefix
	 */
	ConfigurationProperty<String> getRecentActivitiesLabelPrefix();

}
