package com.sirma.itt.emf.solr.configuration;

import com.sirma.itt.seip.configuration.ConfigurationProperty;

/**
 * Provider of configuration properties that describe the ranking behaviour when searching by free text terms.
 *
 * @author Mihail Radkov
 */
public interface RankConfigurations {

	/**
	 * Configuration for Solr {@link org.apache.solr.common.params.CommonParams#Q} query parameter. It allows specifying
	 * Solr join queries to enable ranking on relation instance properties along with data properties.
	 * <p>
	 * Example: <code>({!join ...}{!edismax ...} OR {!edismax ...})</code>
	 * <p>
	 * Solr supports variable expansion so any variable (e.g. $qf) specified in the template will be expanded if it is
	 * provided as a query parameter.
	 *
	 * @return the Solr query template
	 */
	ConfigurationProperty<String> getQueryTemplate();

	/**
	 * Configuration for specifying the query fields upon which the ranking is performed. Those fields could carry
	 * a strength multiplier. Example: <code>title^2</code>
	 *
	 * @return the query fields configuration
	 */
	ConfigurationProperty<String> getQueryFields();

	/**
	 * Configuration property specifying the phrase fields for calculating the relevancy based on term distance in
	 * those fields. Example: <code>title~5</code>
	 *
	 * @return the phrase fields configuration
	 */
	ConfigurationProperty<String> getPhraseFields();

	/**
	 * Breaker configuration for turning maximum disjunction into a summary of ranks if given term is matched in
	 * multiple fields for single instance. Example: <code>0.1</code>
	 *
	 * @return the tie breaker configuration
	 */
	ConfigurationProperty<String> getTieBreaker();

}
