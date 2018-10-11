package com.sirma.itt.emf.solr.configuration;

import com.sirma.itt.seip.configuration.ConfigurationProperty;

import java.util.regex.Pattern;

/**
 * Describes configuration properties used for performing searches in Solr.
 *
 * @author Mihail Radkov
 */
public interface SolrSearchConfigurations {

	/**
	 * Provides regular expression {@link Pattern} used to escape Solr specific symbols (for example in user input
	 * terms).
	 *
	 * @return {@link ConfigurationProperty} carrying compiled {@link Pattern} with characters matching group
	 */
	ConfigurationProperty<Pattern> getTermEscapeRegex();

	/**
	 * Enables the processing of search terms to include wildcards per each term. If enabled, it turns term matching
	 * from exact into a contains operation. This will affect all search terms except exact matches with double quotes
	 * (phrase queries for exact phrase match or exact term matching).
	 * <p>
	 * For example:
	 * <li>if the search term is <i>bar</i>, it will apply wildcards and would find <i>toolbar</i> and <i>bartender</i>
	 * too.</li>
	 * <li>if the search term is <i>"bar"</i>, it would not apply wildcards and will find only <i>bar</i></li>
	 *
	 * @return {@link ConfigurationProperty} carrying true if enabled or false otherwise
	 */
	ConfigurationProperty<Boolean> enableTermWildcards();
}
