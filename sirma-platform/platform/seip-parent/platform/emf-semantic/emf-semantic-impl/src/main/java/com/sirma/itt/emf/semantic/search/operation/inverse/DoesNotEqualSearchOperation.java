package com.sirma.itt.emf.semantic.search.operation.inverse;

import com.sirma.itt.emf.semantic.search.operation.ArithmeticOperators;
import com.sirma.itt.emf.semantic.search.operation.SemanticSearchOperationUtils;
import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Builds a SPARQL query for a does not equal statement from a provided {@link Rule}.
 *
 * @author Mihail Radkov
 */
@Extension(target = SearchOperation.SPARQL_SEARCH_OPERATION, order = 20)
public class DoesNotEqualSearchOperation implements SearchOperation {

	private static final String DOES_NOT_EQUAL_OPERATION = "does_not_equal";

	@Override
	public boolean isApplicable(Rule rule) {
		return DOES_NOT_EQUAL_OPERATION.equalsIgnoreCase(rule.getOperation());
	}

	@Override
	public void buildOperation(StringBuilder builder, Rule rule) {

		switch (rule.getType()) {
			case "rdfs:Literal":
			case "string":
				SemanticSearchOperationUtils.appendStringTripleAndInversedFilter(builder, rule, true, true);
				break;
			case "numeric":
				SemanticSearchOperationUtils.appendNumericStatement(builder, rule, ArithmeticOperators.DOES_NOT_EQUAL);
				break;
			default:
				break;
		}

	}

}
