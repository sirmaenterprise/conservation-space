package com.sirma.itt.emf.solr.configuration;

import org.apache.solr.client.solrj.SolrClient;

/**
 * Provides access to Solr related configuration object.
 *
 * @author BBonev
 */
public interface SolrConfiguration {

	/**
	 * Gets the Solr protocol configuration property.
	 *
	 * @return the Solr protocol configuration
	 */
	String getSolrProtocolConfiguration();

	/**
	 * Gets the Solr host configuration property.
	 *
	 * @return the Solr host configuration
	 */
	String getSolrHostConfiguration();

	/**
	 * Gets the Solr port configuration property.
	 *
	 * @return the Solr port configuration
	 */
	String getSolrPortConfiguration();

	/**
	 * Gets the Solr core configuration property.
	 *
	 * @return the Solr core configuration
	 */
	String getSolrCoreConfiguration();

	/**
	 * Gets the name of the main data solr core.
	 *
	 * @return the main solr core
	 */
	String getMainSolrCore();

	/**
	 * Gets {@link SolrClient} to the solr server.
	 *
	 * @return the solr server
	 */
	SolrClient getSolrServer();

	/**
	 * Gets the {@link SolrClient} to to solr master server.
	 *
	 * @return the solr admin server
	 */
	SolrClient getSolrMaster();

	/**
	 * Gets the Solr server address as full url as 'http://localhost:8983/solr'
	 * 
	 * @return Solr server URL
	 */
	String getSolrAddress();

}