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

import javax.inject.Singleton;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.domain.search.tree.Condition.Junction;
import com.sirma.itt.seip.domain.search.tree.ConditionBuilder;
import com.sirma.itt.seip.domain.search.tree.RuleBuilder;
import com.sirma.itt.seip.domain.search.tree.SearchCriteriaBuilder;

import com.sirma.itt.seip.domain.search.tree.SearchNode;
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
	 * @param conditionConfiguration
	 *            the object from which the {@link Condition} will be build. There are two possible configurations:
	 *            <ul>
	 *                <li>Single rule.<br>Example of configuration:
	 *                    <pre>
	 *                       {<br>
	 *                         "field":"emf:status",<br>
	 *                         "operator":"in",<br>
	 *                         "type": "codeList",<br>
	 *                         "value": ["DRAFT", "APPROVED"]<br>
	 *                       }
	 *                    </pre>
	 *                 </li>
	 *                <li>Several rules. <br>Example of configuration:
	 *                   <pre>
	 *                       {
	 *                          "condition": "AND",
	 *                          "rules": [
	 *                                     {
	 *                                        "field": "emf:status",
	 *                                        "operator": "in",
	 *                                        "type": "codeList",
	 *                                        "value": ["IN_PROGRESS", "INIT"]
	 *                                      }, {
	 *                                         "field": "emf:type",
	 *                                         "operator": "not_in",
	 *                                         "type": "codeList",
	 *                                         "value": ["TASKST20"]
	 *                                       }
	 *                                    ]
	 *                      }
	 *                   </pre>
	 *                </li>
	 *            </ul>
	 * @return {@link Condition} object
	 */
	public Condition parseCondition(JsonObject conditionConfiguration) {
		if (conditionConfiguration == null || conditionConfiguration.isEmpty()) {
			throw new IllegalArgumentException();
		}
		JsonObject configuration = !isConditionNode(conditionConfiguration) ? buildSingleRule(conditionConfiguration) : conditionConfiguration;
		ConditionBuilder conditionBuilder = SearchCriteriaBuilder.createConditionBuilder();
		conditionBuilder.setId(configuration.getString(ID, null));
		conditionBuilder.setCondition(Junction.fromString(configuration.getString(CONDITION)));

		JsonArray rules = configuration.getJsonArray(RULES);
		List<SearchNode> ruleNodes = new LinkedList<>();
		if (!JSON.isBlank(rules)) {
			for (JsonValue rule : rules) {
				SearchNode parsed = parseRule((JsonObject) rule);
				CollectionUtils.addNonNullValue(ruleNodes, parsed);
			}
		}
		conditionBuilder.setRules(ruleNodes);
		return conditionBuilder.build();
	}

	private JsonObject buildSingleRule(JsonObject rule) {
		return Json.createObjectBuilder()
				.add(CONDITION, Junction.AND.toString())
				.add(RULES, Json.createArrayBuilder().add(rule).build())
				.build();
	}

	private SearchNode parseRule(JsonObject source) {
		if (isConditionNode(source)) {
			return parseCondition(source);
		}

		RuleBuilder ruleBuilder = SearchCriteriaBuilder.createRuleBuilder();
		ruleBuilder.setId(source.getString(ID, null));

		ruleBuilder.setType(source.getString(TYPE, ""));
		ruleBuilder.setOperation(source.getString(OPERATOR, ""));
		ruleBuilder.setField(source.getString(FIELD, ""));

		JsonValue value = source.get(VALUE);
		if (JSON.isBlank(value)) {
			return null;
		}

		ruleBuilder.setValues(parseValueTypes(value));
		return ruleBuilder.build();
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

	private static boolean isConditionNode(JsonObject node) {
		return node.containsKey(CONDITION);
	}

}
