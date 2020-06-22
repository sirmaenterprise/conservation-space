package com.sirma.itt.emf.semantic.search.operation.inverse;

import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

import com.sirma.itt.emf.semantic.search.operation.SemanticSearchOperationUtils;
import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Builds a SPARQL query for 'is not' boolean statement from provided {@link Rule}.
 *
 * @author smustafov
 */
@Extension(target = SearchOperation.SPARQL_SEARCH_OPERATION, order = 190)
public class IsNotSearchOperation implements SearchOperation {

	private static final String IS_NOT_OPERATION = "is_not";

	@Override
	public boolean isApplicable(Rule rule) {
		String booleanType = XMLSchema.BOOLEAN.getLocalName();
		return booleanType.equalsIgnoreCase(rule.getType()) && IS_NOT_OPERATION.equalsIgnoreCase(rule.getOperation())
				&& rule.getValues().size() == 1;
	}

	@Override
	public void buildOperation(StringBuilder builder, Rule rule) {
		SemanticSearchOperationUtils.appendBooleanStatement(builder, rule.getField(),
				inverseValue(rule.getValues().get(0)));
	}

	private static String inverseValue(String value) {
		boolean parsedValue = Boolean.parseBoolean(value);
		return Boolean.toString(!parsedValue);
	}

}
