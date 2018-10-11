package com.sirma.itt.emf.solr.configuration;

import com.sirma.itt.seip.configuration.ConfigurationProperty;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Define methods for accessing search configurations.
 *
 * @author BBonev
 * @see SolrSearchConfigurations
 *
 * @deprecated (Deprecated use SolrSearchConfigurations instead.)
 */
@Deprecated
public interface SolrSearchConfiguration {
	/**
	 * Solr FL query param for dashlets. Split fields by ','
	 *
	 * @return the dashlets request fields
	 */
	ConfigurationProperty<String> getDashletsRequestFields();

	/**
	 * Solr FL query param for links. Split fields by ','
	 *
	 * @return the relations request fields
	 */
	ConfigurationProperty<String> getRelationsRequestFields();

	/**
	 * Gets the status filter query.
	 *
	 * @return the status filter query
	 */
	ConfigurationProperty<String> getStatusFilterQuery();

	/**
	 * Gets the full text search template.
	 *
	 * @return the full text search template
	 */
	ConfigurationProperty<String> getFullTextSearchTemplate();

	/**
	 * Gets the full text search escape regex.
	 *
	 * @return the full text search escape regex
	 */
	ConfigurationProperty<Pattern> getFullTextSearchEscapePattern();

	/**
	 * Gets the full text search filter query.
	 *
	 * @return the full text search filter query
	 */
	ConfigurationProperty<String> getFullTextSearchFilterQuery();

	/**
	 * Returns an array of pattern to match + for each match a replacement value wrapped in
	 * {@link TokenProcessorConfiguration}
	 * 
	 * @return the a list of {@link TokenProcessorConfiguration} of pattern to match + match replacement
	 */
	ConfigurationProperty<List<TokenProcessorConfiguration>> getFullTextTokenPreprocessModel();

}
