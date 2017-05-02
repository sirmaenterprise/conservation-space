package com.sirma.itt.seip.search.converters;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

import org.joda.time.DateTime;

import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.domain.search.tree.SearchTreeNode;
import com.sirma.itt.seip.domain.search.tree.SearchTreeNode.NodeType;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.itt.seip.time.DateRange;
import com.sirma.itt.seip.time.ISO8601DateFormat;

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
	 * Populate condition rule with date range.
	 *
	 * @param tree
	 *            condition tree
	 */
	public void populateConditionRuleWithDateRange(Condition tree) {
		if (tree == null || isEmpty(tree.getRules())) {
			return;
		}

		for (SearchTreeNode searchTreeNode : tree.getRules()) {
			if (NodeType.CONDITION.equals(searchTreeNode.getNodeType())) {
				populateConditionRuleWithDateRange((Condition) searchTreeNode);
			} else {
				Rule rule = (Rule) searchTreeNode;
				if (rule.getValues() != null && rule.getValues().size() > 2) {
					innerPopulate(rule);
				}
			}
		}
	}

	/**
	 * Populate rule by dateRangeConverters.
	 *
	 * @param rule
	 *            the rule to populate
	 */
	private void innerPopulate(Rule rule) {
		for (DateRangeConverter converter : dateRangeConverters) {
			if (converter.canConvert(rule.getOperation())) {
				convertRule(rule, converter);
			}
		}
	}

	private static void convertRule(Rule rule, DateRangeConverter converter) {
		JsonObject filterCriteria = buildFilterCriteriaJson(rule);
		DateRange dateRange = converter.convert(new DateTime(), filterCriteria);
		List<String> values = new LinkedList<>();
		values.add(ISO8601DateFormat.format(dateRange.getFirst()));
		values.add(ISO8601DateFormat.format(dateRange.getSecond()));
		rule.setValues(values);
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
