package com.sirma.itt.emf.semantic.search.operation.inverse;

import com.sirma.itt.emf.semantic.search.operation.SemanticSearchOperationUtils;
import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Builds a SPARQL query for a does not end with statement from a provided {@link Rule}.
 * 
 * @author Mihail Radkov
 */
@Extension(target = SearchOperation.SPARQL_SEARCH_OPERATION, order = 100)
public class DoesNotEndWithSearchOperation implements SearchOperation {

	private static final String DOES_NOT_END_WITH_OPERATION = "does_not_end_with";

	@Override
	public boolean isApplicable(Rule rule) {
		return DOES_NOT_END_WITH_OPERATION.equalsIgnoreCase(rule.getOperation());
	}

	@Override
	public void buildOperation(StringBuilder builder, Rule rule) {
		SemanticSearchOperationUtils.appendStringTripleAndInversedFilter(builder, rule, false, true);
	}
}
