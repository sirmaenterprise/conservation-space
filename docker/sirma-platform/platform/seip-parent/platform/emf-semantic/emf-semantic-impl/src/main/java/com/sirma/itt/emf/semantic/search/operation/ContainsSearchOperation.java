package com.sirma.itt.emf.semantic.search.operation;

import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Builds a SPARQL query for a contains statement from a provided {@link Rule}.
 * 
 * @author Mihail Radkov
 */
@Extension(target = SearchOperation.SPARQL_SEARCH_OPERATION, order = 30)
public class ContainsSearchOperation implements SearchOperation {

	private static final String CONTAINS_OPERATION = "contains";

	@Override
	public boolean isApplicable(Rule rule) {
		return CONTAINS_OPERATION.equalsIgnoreCase(rule.getOperation()) && !"fts".equals(rule.getType());
	}

	@Override
	public void buildOperation(StringBuilder builder, Rule rule) {
		SemanticSearchOperationUtils.appendStringTripleAndFilter(builder, rule, false, false);
	}

}
