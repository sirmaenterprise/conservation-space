package com.sirma.itt.emf.solr.search.operation;

import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Builds a Solr query for a date before statement from a provided {@link Rule}.
 * 
 * @author Hristo Lungov
 */
@Extension(target = SearchOperation.SOLR_SEARCH_OPERATION, order = 110)
public class DateBeforeSearchOperation extends AbstractSolrSearchOperation {

	private static final String DATE_BEFORE_OPERATION = "before";
	private static final String DATE_BEFORE_QUERY_FORMAT = "[* TO %s]";

	@Override
	public boolean isApplicable(Rule rule) {
		return DATE_BEFORE_OPERATION.equalsIgnoreCase(rule.getOperation());
	}

	@Override
	public void buildOperation(StringBuilder builder, Rule rule) {
		appendQuery(builder, rule, DATE_BEFORE_QUERY_FORMAT);
	}

}
