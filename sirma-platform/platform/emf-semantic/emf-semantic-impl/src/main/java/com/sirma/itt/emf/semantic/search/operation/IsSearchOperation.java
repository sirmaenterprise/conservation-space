package com.sirma.itt.emf.semantic.search.operation;

import org.openrdf.model.vocabulary.XMLSchema;

import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Builds a SPARQL query for 'is' boolean statement from provided {@link Rule}.
 *
 * @author smustafov
 */
@Extension(target = SearchOperation.EXTENSION_NAME, order = 180)
public class IsSearchOperation implements SearchOperation {

	private static final String IS_OPERATION = "is";

	@Override
	public boolean isApplicable(Rule rule) {
		String booleanType = XMLSchema.BOOLEAN.getLocalName();
		return booleanType.equalsIgnoreCase(rule.getType()) && IS_OPERATION.equalsIgnoreCase(rule.getOperation())
				&& !SemanticSearchOperationUtils.isRuleEmpty(rule) && rule.getValues().size() == 1;
	}

	@Override
	public void buildOperation(StringBuilder builder, Rule rule) {
		SemanticSearchOperationUtils.appendBooleanStatement(builder, rule.getField(), rule.getValues().get(0));
	}

}
