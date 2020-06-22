package com.sirma.itt.emf.semantic.search.operation;

import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Builds a SPARQL query for a lesser than statement from a provided {@link Rule}.
 *
 * @author svetlozar.iliev
 */
@Extension(target = SearchOperation.SPARQL_SEARCH_OPERATION, order = 220)
public class LessThanSearchOperation implements SearchOperation {

	private static final String IS_LESSER_OPERATION = "less_than";

	@Override
	public boolean isApplicable(Rule rule) {
		return IS_LESSER_OPERATION.equalsIgnoreCase(rule.getOperation())
				&& rule.getValues().size() == 1;
	}

	@Override
	public void buildOperation(StringBuilder builder, Rule rule) {
		SemanticSearchOperationUtils.appendNumericStatement(builder, rule, ArithmeticOperators.LESS_THAN);
	}
}
