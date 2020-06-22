package com.sirma.itt.emf.semantic.search.operation.query;

import com.sirma.itt.emf.semantic.search.operation.SemanticSearchOperationUtils;
import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Builds a SPARQL query for a set to embedded query with statement from a provided {@link Rule}.
 *
 * @author nvelkov
 */
@Extension(target = SearchOperation.SPARQL_SEARCH_OPERATION, order = 210)
public class NotSetToQuerySearchOperation extends QuerySearchOperation {

	private static final String NOT_SET_TO_QUERY_OPERATION = "not_set_to_query";

	@Override
	public boolean isApplicable(Rule rule) {
		return NOT_SET_TO_QUERY_OPERATION.equalsIgnoreCase(rule.getOperation()) && super.isApplicable(rule);
	}

	@Override
	protected void appendQuery(StringBuilder builder, Rule rule, String instanceVariableName) {
		SemanticSearchOperationUtils.appendNegateTriple(builder, rule, instanceVariableName,
				() -> appendEmbeddedQuery(builder, rule, instanceVariableName));
	}
}
