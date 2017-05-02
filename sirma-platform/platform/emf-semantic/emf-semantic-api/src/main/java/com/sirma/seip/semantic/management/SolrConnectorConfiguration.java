package com.sirma.seip.semantic.management;

/**
 * Configuration for Solr connector creation or recreation in semantic database if supported.
 *
 * @author BBonev
 */
public class SolrConnectorConfiguration {

	private RepositoryInfo repositoryInfo;
	private String connectorName;
	private String solrConnector;
	private String initialSolrImport;

	/**
	 * Instantiates a new solr core configuration.
	 */
	public SolrConnectorConfiguration() {
		// default constructor
	}

	/**
	 * Instantiates a new solr core configuration.
	 *
	 * @param repositoryInfo
	 *            the repository info
	 * @param connectorName
	 *            the connector name
	 * @param solrConnector
	 *            the solr connector
	 * @param initialSolrImport
	 *            the initial solr import
	 */
	public SolrConnectorConfiguration(RepositoryInfo repositoryInfo, String connectorName, String solrConnector,
			String initialSolrImport) {
		this.repositoryInfo = repositoryInfo;
		this.connectorName = connectorName;
		this.solrConnector = solrConnector;
		this.initialSolrImport = initialSolrImport;
	}

	/**
	 * Gets the repository info.
	 *
	 * @return the repository info
	 */
	public RepositoryInfo getRepositoryInfo() {
		return repositoryInfo;
	}

	/**
	 * Sets the repository info.
	 *
	 * @param repositoryInfo
	 *            the new repository info
	 */
	public void setRepositoryInfo(RepositoryInfo repositoryInfo) {
		this.repositoryInfo = repositoryInfo;
	}

	/**
	 * Gets the solr connector.
	 *
	 * @return the solr connector
	 */
	public String getSolrConnector() {
		return solrConnector;
	}

	/**
	 * Sets the solr connector.
	 *
	 * @param solrConnector
	 *            the new solr connector
	 */
	public void setSolrConnector(String solrConnector) {
		this.solrConnector = solrConnector;
	}

	/**
	 * Gets the initial solr import.
	 *
	 * @return the initial solr import
	 */
	public String getInitialSolrImport() {
		return initialSolrImport;
	}

	/**
	 * Sets the initial solr import.
	 *
	 * @param initialSolrImport
	 *            the new initial solr import
	 */
	public void setInitialSolrImport(String initialSolrImport) {
		this.initialSolrImport = initialSolrImport;
	}

	/**
	 * Gets the connector name.
	 *
	 * @return the connector name
	 */
	public String getConnectorName() {
		return connectorName;
	}

	/**
	 * Sets the connector name.
	 *
	 * @param connectorName
	 *            the new connector name
	 */
	public void setConnectorName(String connectorName) {
		this.connectorName = connectorName;
	}

}
