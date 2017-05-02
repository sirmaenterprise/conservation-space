package com.sirma.itt.emf.semantic.search.operation;

import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.CURLY_BRACKET_CLOSE;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.CURLY_BRACKET_OPEN;
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
@Extension(target = SearchOperation.EXTENSION_NAME, order = 240)
public class NumericRangeSearchOperation implements SearchOperation {

	private static final String IS_BETWEEN = "between";

	@Override
	public boolean isApplicable(Rule rule) {
		return rule.getType().equalsIgnoreCase("numeric") && IS_BETWEEN.equalsIgnoreCase(rule.getOperation())
				&& !SemanticSearchOperationUtils.isRuleEmpty(rule) && rule.getValues().size() == 2;
	}

	@Override
	public void buildOperation(StringBuilder builder, Rule rule) {
		List<String> values = rule.getValues();
		String variable = SPARQLQueryHelper.generateVarName();

		builder.append(CURLY_BRACKET_OPEN);
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

		builder.append(CURLY_BRACKET_CLOSE);
	}

}
