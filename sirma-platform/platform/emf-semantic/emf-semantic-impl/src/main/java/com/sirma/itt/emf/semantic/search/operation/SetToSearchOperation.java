package com.sirma.itt.emf.semantic.search.operation;

import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.CURLY_BRACKET_CLOSE;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.CURLY_BRACKET_OPEN;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.UNION;

import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Builds a SPARQL query statement for set relations from a provided {@link Rule}.
 *
 * @author nvelkov
 */
@Extension(target = SearchOperation.EXTENSION_NAME, order = 150)
public class SetToSearchOperation implements SearchOperation {

	private static final String SET_TO_OPERATION = "set_to";

	@Override
	public boolean isApplicable(Rule rule) {
		return SET_TO_OPERATION.equalsIgnoreCase(rule.getOperation())
				&& !SemanticSearchOperationUtils.isRuleEmpty(rule);
	}

	@Override
	public void buildOperation(StringBuilder builder, Rule rule) {
		appendTriple(builder, rule.getField(), rule.getValues().get(0));
		for (int i = 1; i < rule.getValues().size(); i++) {
			builder.append(UNION);
			appendTriple(builder, rule.getField(), rule.getValues().get(i));
		}
	}

	private static void appendTriple(StringBuilder builder, String field, String value) {
		builder.append(CURLY_BRACKET_OPEN);
		SemanticSearchOperationUtils.appendTriple(builder, SemanticSearchOperationUtils.INSTANCE_VAR, field, value);
		builder.append(CURLY_BRACKET_CLOSE);
	}
}
