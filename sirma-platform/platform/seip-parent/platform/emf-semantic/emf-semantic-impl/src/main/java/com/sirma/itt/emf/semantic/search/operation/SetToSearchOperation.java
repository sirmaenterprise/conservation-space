package com.sirma.itt.emf.semantic.search.operation;

import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.UNION;

import java.util.List;

import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Builds a SPARQL query statement for set relations from a provided {@link Rule}.
 *
 * @author nvelkov
 */
@Extension(target = SearchOperation.SPARQL_SEARCH_OPERATION, order = 150)
public class SetToSearchOperation implements SearchOperation {

	private static final String SET_TO_OPERATION = "set_to";

	@Override
	public boolean isApplicable(Rule rule) {
		return SET_TO_OPERATION.equalsIgnoreCase(rule.getOperation());
	}

	@Override
	public void buildOperation(StringBuilder builder, Rule rule) {
		List<String> values = rule.getValues();
		if(values.size() > 1000) {
			String variableName = SPARQLQueryHelper.generateVarName();
			appendTriple(builder, rule.getField(), variableName);
			SemanticSearchOperationUtils.appendValuesBlock(builder, variableName, values);
		} else {
			Runnable firstTriple = () -> appendTriple(builder, rule.getField(), values.get(0));
			if (values.size() > 1) {
				// when more than one value is present then surround the statement in brackets
				SemanticSearchOperationUtils.appendBracketBlock(builder, firstTriple);
				for (int i = 1; i < rule.getValues().size(); i++) {
					int index = i;
					builder.append(UNION);
					SemanticSearchOperationUtils.appendBracketBlock(builder,
							() -> appendTriple(builder, rule.getField(), values.get(index)));
				}
			} else {
				// one value is present no bracket block is required
				firstTriple.run();
			}
		}
	}

	private static void appendTriple(StringBuilder builder, String field, String value) {
		SemanticSearchOperationUtils.appendTriple(builder, SemanticSearchOperationUtils.INSTANCE_VAR, field, value);
	}
}
