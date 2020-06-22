package com.sirma.itt.emf.solr.search.operation;

import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Builds a Solr query for a collection statement from a provided {@link Rule}.
 *
 * @author Hristo Lungov
 */
@Extension(target = SearchOperation.SOLR_SEARCH_OPERATION, order = 50)
public class InSearchOperation extends AbstractSolrSearchOperation {

	private static final String IN_OPERATION = "in";
	private static final String IN_QUERY_FORMAT = "%s";

	@Override
	public boolean isApplicable(Rule rule) {
		return IN_OPERATION.equalsIgnoreCase(rule.getOperation()) && !"instanceId".equals(rule.getField());
	}

	@Override
	public void buildOperation(StringBuilder builder, Rule rule) {
		appendQuery(builder, rule, IN_QUERY_FORMAT);
	}
}
