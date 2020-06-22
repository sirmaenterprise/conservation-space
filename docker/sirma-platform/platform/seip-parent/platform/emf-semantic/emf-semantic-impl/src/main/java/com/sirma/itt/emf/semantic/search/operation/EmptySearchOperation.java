package com.sirma.itt.emf.semantic.search.operation;

import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Operation that constructs a SPARQL query for properties with or without a value (no statement for concrete property).
 *
 * @author Mihail Radkov
 */
@Extension(target = SearchOperation.SPARQL_SEARCH_OPERATION, order = 270)
public class EmptySearchOperation implements SearchOperation {

	private static final String EMPTY_OPERATION = "empty";

	@Override
	public boolean isApplicable(Rule rule) {
		return EMPTY_OPERATION.equalsIgnoreCase(rule.getOperation());
	}

	@Override
	public void buildOperation(StringBuilder builder, Rule rule) {
		boolean isEmpty = Boolean.parseBoolean(rule.getValues().get(0));
		if (isEmpty) {
			builder.append(SPARQLQueryHelper.FILTER).append(SPARQLQueryHelper.NOT_EXISTS_START);
		}
		appendTripleForExistence(builder, rule);
		if (isEmpty) {
			builder.append(SPARQLQueryHelper.BLOCK_END);
		}
	}

	private static void appendTripleForExistence(StringBuilder builder, Rule rule) {
		String subject = SemanticSearchOperationUtils.INSTANCE_VAR;
		String object = SPARQLQueryHelper.generateVarName();
		// Appending a triple for this property ensures to return instances having it
		SemanticSearchOperationUtils.appendTriple(builder, subject, rule.getField(), object);
	}
}
