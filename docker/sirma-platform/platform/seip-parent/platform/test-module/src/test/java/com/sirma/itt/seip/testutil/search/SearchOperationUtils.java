package com.sirma.itt.seip.testutil.search;

import java.util.List;

import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.domain.search.tree.SearchCriteriaBuilder;

/**
 * Helper class for testing implementations of {@link com.sirma.itt.seip.domain.search.SearchOperation}.
 *
 * @author Mihail Radkov
 */
public class SearchOperationUtils {

	public static Rule createRule(String field, String type, String op, List<String> val) {
		return SearchCriteriaBuilder.createRuleBuilder()
				.setField(field)
				.setType(type)
				.setOperation(op)
				.setValues(val)
				.build();

	}

	public static Rule createRule(String field, String type, String op, String val) {
		return SearchCriteriaBuilder.createRuleBuilder()
				.setField(field)
				.setType(type)
				.setOperation(op)
				.addValue(val)
				.build();
	}

	public static Rule createRule(String field, String type, String op) {
		return SearchCriteriaBuilder.createRuleBuilder().setField(field).setType(type).setOperation(op).build();
	}
}
