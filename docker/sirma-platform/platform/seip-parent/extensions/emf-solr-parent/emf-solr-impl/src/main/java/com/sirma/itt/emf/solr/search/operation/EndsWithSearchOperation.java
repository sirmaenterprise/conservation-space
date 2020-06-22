package com.sirma.itt.emf.solr.search.operation;

import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Builds a Solr query for a ends with statement from a provided {@link Rule}.
 * 
 * @author Hristo Lungov
 */
@Extension(target = SearchOperation.SOLR_SEARCH_OPERATION, order = 90)
public class EndsWithSearchOperation extends AbstractSolrSearchOperation {

	private static final String ENDS_WITH_OPERATION = "ends_with";
	private static final String ENDS_WITH_QUERY_FORMAT = "*%s";

	@Override
	public boolean isApplicable(Rule rule) {
		return ENDS_WITH_OPERATION.equalsIgnoreCase(rule.getOperation());
	}

	@Override
	public void buildOperation(StringBuilder builder, Rule rule) {
		appendQuery(builder, rule, ENDS_WITH_QUERY_FORMAT);
	}

}
