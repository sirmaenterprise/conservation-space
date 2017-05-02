package com.sirma.itt.emf.solr.connector;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;

/**
 * Contains configuration properties related to solr.
 *
 * @author nvelkov
 */
@Singleton
public class SolrConfigurationProperties {

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "solr.connector.path", defaultValue = "graphdb_solr_connector.sparql", sensitive = true, label = "Path to the graphdb solr connector file")
	private ConfigurationProperty<String> solrConnectorPath;

	/**
	 * Gets the solr connector path.
	 *
	 * @return the solr connector path
	 */
	public ConfigurationProperty<String> getSolrConnectorPath() {
		return solrConnectorPath;
	}
}
