package com.sirma.itt.seip.search.converters;

import static com.sirma.itt.seip.rest.utils.JsonKeys.CONDITION;
import static com.sirma.itt.seip.rest.utils.JsonKeys.FIELD;
import static com.sirma.itt.seip.rest.utils.JsonKeys.ID;
import static com.sirma.itt.seip.rest.utils.JsonKeys.OPERATOR;
import static com.sirma.itt.seip.rest.utils.JsonKeys.RULES;
import static com.sirma.itt.seip.rest.utils.JsonKeys.TYPE;
import static com.sirma.itt.seip.rest.utils.JsonKeys.VALUE;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.inject.Singleton;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.domain.search.tree.Condition.Junction;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.domain.search.tree.SearchTreeNode;
import com.sirma.itt.seip.rest.utils.JSON;

/**
 * Converts {@link JsonObject} to {@link Condition}.
 *
 * @author A. Kunchev
 */
@Singleton
public class JsonToConditionConverter {

	/**
	 * Parses the passed json object to condition. This conditions are primary used in searches.
	 *
	 * @param json
	 *            the object from which the {@link Condition} will be build
	 * @return {@link Condition} object
	 */
	public Condition parseCondition(JsonObject json) {
		if (json == null || json.isEmpty()) {
			throw new IllegalArgumentException();
		}

		Condition condition = new Condition();
		addNodeId(json, condition);
		condition.setCondition(Junction.fromString(json.getString(CONDITION)));

		JsonArray rules = json.getJsonArray(RULES);
		List<SearchTreeNode> ruleNodes = new LinkedList<>();
		if (!JSON.isBlank(rules)) {
			for (JsonValue rule : rules) {
				SearchTreeNode parsed = parseRule((JsonObject) rule);
				CollectionUtils.addNonNullValue(ruleNodes, parsed);
			}
		}
		condition.setRules(ruleNodes);
		return condition;
	}

	private SearchTreeNode parseRule(JsonObject source) {
		if (isConditionNode(source)) {
			return parseCondition(source);
		}

		Rule rule = new Rule();
		addNodeId(source, rule);
		rule.setType(source.getString(TYPE, ""));
		rule.setOperation(source.getString(OPERATOR, ""));
		rule.setField(source.getString(FIELD));

		JsonValue value = source.get(VALUE);
		if (JSON.isBlank(value)) {
			return null;
		}

		rule.setValues(parseValueTypes(value));
		return rule;
	}

	private static List<String> parseValueTypes(JsonValue value) {
		List<String> values = new LinkedList<>();

		switch (value.getValueType()) {
			case STRING:
				values.add(((JsonString) value).getString());
				break;
			case NUMBER:
				values.add(value.toString());
				break;
			case OBJECT:
				values.add(value.toString());
				break;
			case ARRAY:
				for (JsonValue item : (JsonArray) value) {
					values.addAll(parseValueTypes(item));
				}
				break;
			default:
				break;
		}
		return values;
	}

	private static void addNodeId(JsonObject source, SearchTreeNode node) {
		String id = source.getString(ID, null);
		if (id == null) {
			// Ensuring the search tree node has ID
			id = UUID.randomUUID().toString();
		}
		node.setId(id);
	}

	private static boolean isConditionNode(JsonObject node) {
		return node.containsKey(CONDITION);
	}

}
