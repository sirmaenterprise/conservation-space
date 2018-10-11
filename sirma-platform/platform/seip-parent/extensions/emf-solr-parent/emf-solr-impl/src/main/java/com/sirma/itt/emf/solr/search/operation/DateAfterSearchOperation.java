package com.sirma.itt.emf.solr.search.operation;

import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Builds a Solr query for a date after statement from a provided {@link Rule}.
 * 
 * @author Hristo Lungov
 */
@Extension(target = SearchOperation.SOLR_SEARCH_OPERATION, order = 120)
public class DateAfterSearchOperation extends AbstractSolrSearchOperation {

	private static final String DATE_AFTER_OPERATION = "after";
	private static final String DATE_AFTER_QUERY_FORMAT = "[%s TO *]";

	@Override
	public boolean isApplicable(Rule rule) {
		return DATE_AFTER_OPERATION.equalsIgnoreCase(rule.getOperation());
	}

	@Override
	public void buildOperation(StringBuilder builder, Rule rule) {
		appendQuery(builder, rule, DATE_AFTER_QUERY_FORMAT);
	}

}
