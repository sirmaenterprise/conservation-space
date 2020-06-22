package com.sirma.itt.emf.semantic.search.operation;

import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Builds a SPARQL query for a collection statement from a provided {@link Rule}.
 *
 * @author Mihail Radkov
 */
@Extension(target = SearchOperation.SPARQL_SEARCH_OPERATION, order = 50)
public class InSearchOperation implements SearchOperation {

	private static final String IN_OPERATION = "in";

	@Override
	public boolean isApplicable(Rule rule) {
		return IN_OPERATION.equalsIgnoreCase(rule.getOperation()) && !"instanceId".equals(rule.getField());
	}

	@Override
	public void buildOperation(StringBuilder builder, Rule rule) {
		SemanticSearchOperationUtils.appendStringTripleAndFilter(builder, rule, true, true);
	}
}
