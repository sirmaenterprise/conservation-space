package com.sirma.itt.emf.semantic.search.operation;

import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Builds a SPARQL query statement for relations that are not set from a provided {@link Rule}.
 *
 * @author nvelkov
 */
@Extension(target = SearchOperation.SPARQL_SEARCH_OPERATION, order = 170)
public class NotSetToSearchOperation implements SearchOperation {

	private static final String NOT_SET_TO_OPERATION = "not_set_to";

	@Override
	public boolean isApplicable(Rule rule) {
		return NOT_SET_TO_OPERATION.equalsIgnoreCase(rule.getOperation());
	}

	@Override
	public void buildOperation(StringBuilder builder, Rule rule) {
		for (String value : rule.getValues()) {
			SemanticSearchOperationUtils.appendNegateTriple(builder, rule, value);
		}
	}

}
