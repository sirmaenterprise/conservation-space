package com.sirma.itt.emf.semantic.search.operation.inverse;

import com.sirma.itt.emf.semantic.search.operation.SemanticSearchOperationUtils;
import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Builds a SPARQL query for a not in statement from a provided {@link Rule}.
 * 
 * @author Mihail Radkov
 */
@Extension(target = SearchOperation.SPARQL_SEARCH_OPERATION, order = 60)
public class NotInSearchOperation implements SearchOperation {

	private static final String NOT_IN_OPERATION = "not_in";

	@Override
	public boolean isApplicable(Rule rule) {
		return NOT_IN_OPERATION.equalsIgnoreCase(rule.getOperation());
	}

	@Override
	public void buildOperation(StringBuilder builder, Rule rule) {
		SemanticSearchOperationUtils.appendStringTripleAndInversedFilter(builder, rule, true, true);
	}

}
