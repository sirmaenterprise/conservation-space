package com.sirma.itt.emf.solr.config;

import com.sirma.itt.emf.solr.configuration.SolrSearchConfigurations;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.regex.Pattern;

/**
 * Implementation of {@link SolrSearchConfigurations} for configuration provisioning for Solr search specific
 * properties.
 *
 * @author Mihail Radkov
 */
@Singleton
public class SolrSearchConfigurationsImpl implements SolrSearchConfigurations {

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "search.solr.term.escape.regex",
			defaultValue = "([\\^\\[\\]\\{\\}\\(\\)\\!\\~\\+\\-\\&\\|\\:])", type = Pattern.class, subSystem = "search",
			label = "Regular expression pattern used to escape Solr specific symbols in FTS search terms. Example: ([\\~])")
	private ConfigurationProperty<Pattern> termEscapeRegex;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "search.solr.term.wildcards.enable", defaultValue = "true",
			type = Boolean.class, subSystem = "search",
			label = "Enables the processing of search terms matching from exact into a contains operation via wildcards. "
					+ "This will affect all search terms except those for exact matching with double quotes "
					+ "(phrase queries or single terms with double quotes). Default value is true")
	private ConfigurationProperty<Boolean> enableTermWildcards;

	@Override
	public ConfigurationProperty<Pattern> getTermEscapeRegex() {
		return termEscapeRegex;
	}

	@Override
	public ConfigurationProperty<Boolean> enableTermWildcards() {
		return enableTermWildcards;
	}
}
