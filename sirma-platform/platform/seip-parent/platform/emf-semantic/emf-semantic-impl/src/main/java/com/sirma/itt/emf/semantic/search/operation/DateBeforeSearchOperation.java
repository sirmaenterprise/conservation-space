package com.sirma.itt.emf.semantic.search.operation;

import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Builds a SPARQL query for a date before statement from a provided {@link Rule}.
 * 
 * @author Mihail Radkov
 */
@Extension(target = SearchOperation.SPARQL_SEARCH_OPERATION, order = 110)
public class DateBeforeSearchOperation implements SearchOperation {

	private static final String DATE_BEFORE_OPERATION = "before";

	@Override
	public boolean isApplicable(Rule rule) {
		return DATE_BEFORE_OPERATION.equalsIgnoreCase(rule.getOperation());
	}

	@Override
	public void buildOperation(StringBuilder builder, Rule rule) {
		SemanticSearchOperationUtils.appendSingleDateStatement(builder, rule, true);
	}

}
