package com.sirma.itt.seip.search.converters;

import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.domain.search.tree.ConditionBuilder;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.domain.search.tree.RuleBuilder;
import com.sirma.itt.seip.domain.search.tree.SearchCriteriaBuilder;
import com.sirma.itt.seip.domain.search.tree.SearchNode;
import com.sirma.itt.seip.domain.search.tree.SearchNode.NodeType;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.itt.seip.time.DateRange;
import com.sirma.itt.seip.time.ISO8601DateFormat;
import org.joda.time.DateTime;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import java.util.LinkedList;
import java.util.List;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

/**
 * Convert {@link JsonObject} to @{link DateRange) object. The json have to be something like:
 *
 * <pre>
 * {
 * 	"field": "emf:createdOn",
 * 	"operator": "after",
 * 	"type": "dateTime",
 * 	"value": "2016-12-06T22:00:00.000Z"
 * 	}
 * </pre>
 *
 * OR
 *
 * <pre>
 * {
 * 	"field": "emf:createdOn",
 * 	"operator": "is",
 * 	"type": "dateTime",
 * 	"value": ["2016-12-05T22:00:00.000Z", "2016-12-06T21:59:59.999Z"]
 * 	}
 * </pre>
 *
 * @author Boyan Tonchev
 */
@ApplicationScoped
public class JsonToDateRangeConverter {

	private static final String WITHIN_OPERATION = "within";
	private static final String DATETIME_TYPE = "dateTime";

	@Inject
	@ExtensionPoint(AbstractDateRangeConverter.PLUGIN_NAME)
	private Plugins<DateRangeConverter> dateRangeConverters;

	/**
	 * Convert {@link JsonObject} to @{link DateRange) object. It looking for converter of current operator and if find
	 * call it.
	 *
	 * <pre>
	 * {
	 * 	"field": "emf:createdOn",
	 * 	"operator": "after",
	 * 	"type": "dateTime",
	 * 	"value": "2016-12-06T22:00:00.000Z"
	 * 	}
	 * </pre>
	 *
	 * OR
	 *
	 * <pre>
	 * {
	 * 	"field": "emf:createdOn",
	 * 	"operator": "is",
	 * 	"type": "dateTime",
	 * 	"value": ["2016-12-05T22:00:00.000Z", "2016-12-06T21:59:59.999Z"]
	 * 	}
	 * </pre>
	 *
	 * @param filterCriteria
	 *            - the {@link JsonObject} which have to be converted.
	 * @return created object.
	 */
	public DateRange convertDateRange(JsonObject filterCriteria) {
		String operator = filterCriteria.getString(JsonKeys.OPERATOR);
		for (DateRangeConverter converter : dateRangeConverters) {
			if (converter.canConvert(operator)) {
				return converter.convert(new DateTime(), filterCriteria);
			}
		}

		return new DateRange(null, null);
	}

	/**
	 * Converts dynamic date range {@link Rule} with proper date range dates. It recursively traverse the whole search
	 * tree and populates the builder with the converted values.
	 * <p>
	 * To be considered a dynamic date range rule, its type should be "dateTime", the operation "within" and have 3
	 * values for step, offset and offset type. Example: ["last", "1" ,"weeks"]
	 *
	 * @param builder
	 * 		The builder that is mutated and its immutable condition equivalent
	 * @param tree
	 * 		condition tree
	 */
	public void populateConditionRuleWithDateRange(ConditionBuilder builder, Condition tree) {
		if (tree == null || isEmpty(tree.getRules())) {
			return;
		}

		for (SearchNode searchNode : tree.getRules()) {
			if (NodeType.CONDITION.equals(searchNode.getNodeType())) {
				ConditionBuilder conditionBuilder = SearchCriteriaBuilder.createConditionBuilder()
						.from((Condition) searchNode);
				populateConditionRuleWithDateRange(conditionBuilder, (Condition) searchNode);
				builder.addRule(conditionBuilder.build());
			} else {
				Rule rule = (Rule) searchNode;
				RuleBuilder ruleBuilder = SearchCriteriaBuilder.createRuleBuilder().from(rule);
				if (isRuleDynamicDateRange(rule)) {
					innerPopulate(ruleBuilder, rule);
					builder.addRule(ruleBuilder.build());
				} else {
					builder.addRule(rule);
				}
			}
		}
	}

	private static boolean isRuleDynamicDateRange(Rule rule) {
		return DATETIME_TYPE.equalsIgnoreCase(rule.getType()) && WITHIN_OPERATION.equalsIgnoreCase(rule.getOperation())
				&& rule.getValues().size() > 2;
	}

	/**
	 * Populate rule by dateRangeConverters.
	 *
	 * @param ruleBuilder
	 * 		The builder that is used to create the populated rule
	 * @param rule
	 * 		the rule that is used for read only prototype
	 */
	private void innerPopulate(RuleBuilder ruleBuilder, Rule rule) {
		for (DateRangeConverter converter : dateRangeConverters) {
			if (converter.canConvert(rule.getOperation())) {
				convertRule(ruleBuilder, rule, converter);
			}
		}
	}

	private static void convertRule(RuleBuilder ruleBuilder, Rule rule, DateRangeConverter converter) {
		JsonObject filterCriteria = buildFilterCriteriaJson(rule);
		DateRange dateRange = converter.convert(new DateTime(), filterCriteria);
		List<String> values = new LinkedList<>();
		values.add(ISO8601DateFormat.format(dateRange.getFirst()));
		values.add(ISO8601DateFormat.format(dateRange.getSecond()));
		ruleBuilder.setValues(values);
	}

	private static JsonObject buildFilterCriteriaJson(Rule rule) {
		List<String> ruleValues = rule.getValues();
		JsonArrayBuilder arrayBuilder = Json
				.createArrayBuilder()
					.add(ruleValues.get(0))
					.add(Integer.valueOf(ruleValues.get(1)))
					.add(ruleValues.get(2));

		return Json
				.createObjectBuilder()
					.add(JsonKeys.OPERATOR, rule.getOperation())
					.add(JsonKeys.VALUE, arrayBuilder)
					.build();
	}

}
