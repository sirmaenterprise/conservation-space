package com.sirma.itt.emf.semantic.search;

import java.util.Collections;
import java.util.List;

import com.sirma.itt.seip.domain.search.tree.Rule;

/**
 * Helper class for testing implementations of {@link com.sirma.itt.seip.domain.search.SearchOperation}.
 *
 * @author Mihail Radkov
 */
public class SearchOperationUtils {
	public static Rule createRule(String field, String type, String op, List<String> val) {
		Rule rule = new Rule();
		rule.setField(field);
		rule.setType(type);
		rule.setOperation(op);
		rule.setValues(val);
		return rule;
	}

	public static Rule createRule(String field, String type, String op, String val) {
		Rule rule = new Rule();
		rule.setField(field);
		rule.setType(type);
		rule.setOperation(op);
		rule.setValues(Collections.singletonList(val));
		return rule;
	}

	public static Rule createRule(String field, String type, String op) {
		Rule rule = new Rule();
		rule.setField(field);
		rule.setType(type);
		rule.setOperation(op);
		return rule;
	}
}
