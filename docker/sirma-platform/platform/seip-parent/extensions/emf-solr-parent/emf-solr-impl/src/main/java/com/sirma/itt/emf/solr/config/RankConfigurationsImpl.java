package com.sirma.itt.emf.solr.config;

import com.sirma.itt.emf.solr.configuration.RankConfigurations;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Implementation of {@link RankConfigurations} for provisioning of Solr ranking configuration properties.
 *
 * @author Mihail Radkov
 */
@Singleton
public class RankConfigurationsImpl implements RankConfigurations {

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "search.solr.ranking.q",
			defaultValue = "({!join from=id to=hasAttachment score=max}{!edismax v=$uq qf=altTitle^7} OR "
					+ "{!join from=id to=isAttachedTo score=max}{!edismax v=$uq qf=altTitle^7} OR "
					+ "{!join from=id to=createdBy score=max}{!edismax v=$uq qf=altTitle^7} OR "
					+ "{!join from=id to=assignee score=max}{!edismax v=$uq qf=altTitle^8} OR {!edismax v=$uq})",
			subSystem = "search",
			label = "Query template allowing to specify document joins to search and rank by object properties (relations)."
					+ " Example configuration to enable specific object property (assignee) along with the data properties: "
					+ "({!join from=id to=assignee score=max}{!edismax v=$uq qf=altTitle^8} OR {!edismax v=$uq})"
					+ " This supports variable expansion and in this case the uq is another query parameter carrying the search terms.")
	private ConfigurationProperty<String> queryTemplate;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "search.solr.ranking.qf",
			defaultValue = "altTitle^50 title^10 identifier^10 description^8 fileName^8 content^5  ocrContent^5", subSystem = "search",
			label = "Solr query fields upon which rank is calculated. Separated with white space, accepts multipliers. "
					+ "Those multipliers are used to increase the score per field if there is a matched term. "
					+ "Example: altTitle^50 title^10 identifier^10 description^8 fileName^8 content^5")
	private ConfigurationProperty<String> queryFields;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "search.solr.ranking.pf",
			defaultValue = "altTitle~3 title~3 identifier~2 description~5 fileName~5 content~10 ocrContent~10", subSystem = "search",
			label = "Solr phrase fields used for calculating rank based on the distance of terms per field. "
					+ "Separated with white space. Each field accepts a phrase slop (term distance) which affects the score. "
					+ "The smaller the slop, the less score is calculated if the term distance is above it. "
					+ "Example: altTitle~3 title~3 identifier~2 description~5 fileName~5 content~10")
	private ConfigurationProperty<String> phraseFields;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "search.solr.ranking.tie", defaultValue = "0.1", subSystem = "search",
			label = "Solr tie breaker used for turning the disjunction into a summary of ranks if a search term is matched "
					+ "in multiple fields. Possible float value is between 0 and 1. Example: 0.1")
	private ConfigurationProperty<String> tieBreaker;

	@Override
	public ConfigurationProperty<String> getQueryTemplate() {
		return queryTemplate;
	}

	@Override
	public ConfigurationProperty<String> getQueryFields() {
		return queryFields;
	}

	@Override
	public ConfigurationProperty<String> getPhraseFields() {
		return phraseFields;
	}

	@Override
	public ConfigurationProperty<String> getTieBreaker() {
		return tieBreaker;
	}

}
