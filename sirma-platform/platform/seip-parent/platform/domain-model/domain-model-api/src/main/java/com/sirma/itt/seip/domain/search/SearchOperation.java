package com.sirma.itt.seip.domain.search;

import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * Represents a command that will be executed upon a {@link Rule} to build a string query statement.
 *
 * The provided {@link Rule}'s values list must not be null or empty.
 *
 * @author Mihail Radkov
 */
public interface SearchOperation extends Plugin {

	String SPARQL_SEARCH_OPERATION = "SparqlSearchOperation";
	String SOLR_SEARCH_OPERATION = "SolrSearchOperation";

	/**
	 * Tells if the provided rule is supported by the search operation and if a string query could be built from it.
	 *
	 * @param rule
	 *            - the provided rule
	 * @return true if it's supported of false otherwise
	 */
	boolean isApplicable(Rule rule);

	/**
	 * Builds a query from the provided search rule and appends it into the string builder. Providing a non empty
	 * {@link java.util.List} of values in the {@link Rule} is mandatory for a query to be built.
	 *
	 * @param builder
	 *            - the query builder where query will be appended
	 * @param rule
	 *            - the provided search rule with value &amp; operation
	 */
	void buildOperation(StringBuilder builder, Rule rule);

}
