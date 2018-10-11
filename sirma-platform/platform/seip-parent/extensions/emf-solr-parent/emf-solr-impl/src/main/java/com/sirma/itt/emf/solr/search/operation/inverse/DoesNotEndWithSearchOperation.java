package com.sirma.itt.emf.solr.search.operation.inverse;

import com.sirma.itt.emf.solr.search.operation.AbstractSolrSearchOperation;
import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Builds a Solr query for a does not end with statement from a provided {@link Rule}.
 * 
 * @author Hristo Lungov
 */
@Extension(target = SearchOperation.SOLR_SEARCH_OPERATION, order = 100)
public class DoesNotEndWithSearchOperation extends AbstractSolrSearchOperation {

	private static final String DOES_NOT_END_WITH_OPERATION = "does_not_end_with";
	private static final String DOES_NOT_END_WITH_QUERY_FORMAT = "*%s";

	@Override
	public boolean isApplicable(Rule rule) {
		return DOES_NOT_END_WITH_OPERATION.equalsIgnoreCase(rule.getOperation());
	}

	@Override
	public void buildOperation(StringBuilder builder, Rule rule) {
		appendNegatedQuery(builder, rule, DOES_NOT_END_WITH_QUERY_FORMAT);
	}
}
