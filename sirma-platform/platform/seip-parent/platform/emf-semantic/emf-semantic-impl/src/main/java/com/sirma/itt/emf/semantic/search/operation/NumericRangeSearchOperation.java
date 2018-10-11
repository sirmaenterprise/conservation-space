package com.sirma.itt.emf.semantic.search.operation;

import static com.sirma.itt.emf.semantic.search.operation.SemanticSearchOperationUtils.INSTANCE_VAR;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Builds a SPARQL query for a between two numeric values statement from a provided {@link Rule}.
 *
 * @author svetlozar.iliev
 */
@Extension(target = SearchOperation.SPARQL_SEARCH_OPERATION, order = 240)
public class NumericRangeSearchOperation implements SearchOperation {

	private static final String IS_BETWEEN = "between";
	private static final String NUMERIC = "numeric";

	@Override
	public boolean isApplicable(Rule rule) {
		return NUMERIC.equalsIgnoreCase(rule.getType()) && IS_BETWEEN.equalsIgnoreCase(rule.getOperation())
				&& rule.getValues().size() == 2;
	}

	@Override
	public void buildOperation(StringBuilder builder, Rule rule) {
		List<String> values = rule.getValues();
		String variable = SPARQLQueryHelper.generateVarName();

		SemanticSearchOperationUtils.appendTriple(builder, INSTANCE_VAR, rule.getField(), variable);

		String starting = values.get(0);
		if (StringUtils.isNotBlank(starting)) {
			SemanticSearchOperationUtils.appendNumericFilter(builder, starting, variable,
					ArithmeticOperators.GREATER_THAN);
		}

		String ending = values.get(1);
		if (StringUtils.isNotBlank(ending)) {
			SemanticSearchOperationUtils.appendNumericFilter(builder, ending, variable, ArithmeticOperators.LESS_THAN);
		}
	}

}
