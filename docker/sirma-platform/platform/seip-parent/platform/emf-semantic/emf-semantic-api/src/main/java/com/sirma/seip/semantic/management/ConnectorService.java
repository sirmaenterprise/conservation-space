package com.sirma.seip.semantic.management;

import static com.sirma.itt.emf.semantic.repository.creator.RepositoryUtils.escapeRepositoryName;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Connector service responsible for managing connectors in GraphDB
 * 
 * @author kirq4e
 */
public interface ConnectorService {

	/**
	 * Retrieves all connectors from the repository
	 * 
	 * @return List of all available connectors in the repository
	 */
	List<ConnectorConfiguration> listConnectors();

	/**
	 * Checks is the connector present.
	 *
	 * @param info
	 *            the info
	 * @param connectorName
	 *            the connector name
	 * @return true, if the connector is present
	 */
	boolean isConnectorPresent(String connectorName);

	/**
	 * Creates the connector using the specified configuration. The configuration is used to generate SPARQL query for
	 * creation of the connector and it is stored in the semantic repository. The configuration must provide Solr Server
	 * address. If the connector name is not set then it is constructed from the tenant name
	 * <p>
	 * For generating the default connector configuration use {@link #createDefaultConnectorConfiguration(String)}
	 *
	 * @param configuration
	 *            the configuration
	 * @return Created configuration
	 */
	ConnectorConfiguration createConnector(ConnectorConfiguration configuration);

	/**
	 * Reset connector. If connector exists it will be deleted and initialized again.
	 *
	 * @param connectorName
	 *            the connector name
	 * @see #createConnector(ConnectorConfiguration)
	 */
	void resetConnector(String connectorName);

	/**
	 * Delete connector if exists
	 *
	 * @param connectorName
	 *            the name of the connector
	 */
	boolean deleteConnector(String connectorName);

	/**
	 * Creates default connector configuration with the given connector name. It contains default configuration of a
	 * connector and simple fields
	 * 
	 * @param connectorName
	 *            The name of the connector
	 * @return Configuration for creating the default connector
	 */
	ConnectorConfiguration createDefaultConnectorConfiguration(String connectorName);

	/**
	 * Saves the connector configuration without recreating the connector
	 * 
	 * @param configuration
	 *            Connector configuration
	 */
	void saveConnectorConfiguration(ConnectorConfiguration configuration);

	/**
	 * Loads the configuration from the repository
	 * 
	 * @param connectorName
	 *            the connector name
	 * @return Connector configuration
	 */
	ConnectorConfiguration loadConnectorConfiguration(String connectorName);

	/**
	 * Generate proper connector name by the template "fts_suffix", where in the most cases suffix will be the name of
	 * the tenant
	 * 
	 * @param suffix
	 *            Suffix to append in the connector name. In the most cases this will be the name of the tenant
	 * @return Proper connector name
	 */
	default String createConnectorName(String suffix) {
		if (StringUtils.isNotEmpty(suffix) && !suffix.startsWith("fts_")) {
			return "fts_" + escapeRepositoryName(suffix);
		} else {
			return suffix;
		}
	}

}