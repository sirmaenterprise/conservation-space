/**
 *
 */
package com.sirma.seip.semantic.management;

/**
 * Provides means of managing semantic repositories and full text search (Solr) connectors if supported
 *
 * @author BBonev
 */
public interface RepositoryManagement {

	/**
	 * Creates the repository.
	 *
	 * @param configuration
	 *            the configuration
	 */
	void createRepository(RepositoryConfiguration configuration);

	/**
	 * Delete repository.
	 *
	 * @param configuration
	 *            the configuration
	 */
	void deleteRepository(RepositoryInfo configuration);

	/**
	 * Creates the access user for repo.
	 *
	 * @param configuration
	 *            the configuration
	 * @param userName
	 *            the user name
	 * @param password
	 *            the password
	 */
	void createAccessUserForRepo(RepositoryInfo configuration, String userName, String password);

	/**
	 * Checks if is repository exists.
	 *
	 * @param configuration
	 *            the configuration
	 * @return true, if is repository exists
	 */
	boolean isRepositoryExists(RepositoryInfo configuration);

	/**
	 * Checks if is solr connector present.
	 *
	 * @param info
	 *            the info
	 * @param connectorName
	 *            the connector name
	 * @return true, if is solr connector present
	 */
	boolean isSolrConnectorPresent(RepositoryInfo info, String connectorName);

	/**
	 * Creates the solr connector using the specified configuration. The connector is created by inserting the connector
	 * configuration provided by {@link SolrConnectorConfiguration#getSolrConnector()}. If
	 * {@link SolrConnectorConfiguration#getInitialSolrImport()} is non <code>null</code> it will be used to initialize
	 * the connector data.
	 * <p>
	 * The expected format of {@link SolrConnectorConfiguration#getSolrConnector()} is turtle.<br>
	 * The expected format of {@link SolrConnectorConfiguration#getInitialSolrImport()} is trig.
	 *
	 * @param configuration
	 *            the configuration
	 */
	void createSolrConnector(SolrConnectorConfiguration configuration);

	/**
	 * Reset solr connector. If connector exists it will be deleted and initialized again.
	 *
	 * @param configuration
	 *            the configuration
	 * @see #createSolrConnector(SolrConnectorConfiguration)
	 */
	void resetSolrConnector(SolrConnectorConfiguration configuration);

	/**
	 * Delete solr connector if exists
	 *
	 * @param info
	 *            the info
	 * @param solrCoreName
	 *            the solr core name
	 */
	void deleteSolrConnector(RepositoryInfo info, String solrCoreName);
}
