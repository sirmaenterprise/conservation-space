package com.sirma.itt.emf.solr.search.operation;

import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Builds a Solr query for a contains statement from a provided {@link Rule}.
 *
 * @author Hristo Lungov
 */
@Extension(target = SearchOperation.SOLR_SEARCH_OPERATION, order = 30)
public class ContainsSearchOperation extends AbstractSolrSearchOperation {

	private static final String CONTAINS_OPERATION = "contains";
	private static final String CONTAINS_QUERY_FORMAT = "*%s*";

	@Override
	public boolean isApplicable(Rule rule) {
		return CONTAINS_OPERATION.equalsIgnoreCase(rule.getOperation());
	}

	@Override
	public void buildOperation(StringBuilder builder, Rule rule) {
		appendQuery(builder, rule, CONTAINS_QUERY_FORMAT);
	}

}
