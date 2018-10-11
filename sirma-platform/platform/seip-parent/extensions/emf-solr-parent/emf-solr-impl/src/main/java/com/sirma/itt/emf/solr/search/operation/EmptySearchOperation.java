package com.sirma.itt.emf.solr.search.operation;

import com.sirma.itt.seip.domain.search.SearchOperation;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Operation that constructs a Solr query for properties with or without a value (no statement for concrete property).
 *
 * @author Hristo Lungov
 */
@Extension(target = SearchOperation.SOLR_SEARCH_OPERATION, order = 270)
public class EmptySearchOperation implements SearchOperation {

	private static final String EMPTY_OPERATION = "empty";

	@Override
	public boolean isApplicable(Rule rule) {
		return EMPTY_OPERATION.equalsIgnoreCase(rule.getOperation());
	}

	@Override
	public void buildOperation(StringBuilder builder, Rule rule) {
		String value = rule.getValues().get(0);
		boolean isEmpty = Boolean.parseBoolean(value);
		if (isEmpty) {
			builder.append("-");
		}
		builder.append(rule.getField()).append(":*");
	}
}
