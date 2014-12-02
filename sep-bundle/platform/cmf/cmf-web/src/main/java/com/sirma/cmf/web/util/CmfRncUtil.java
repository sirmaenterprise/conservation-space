package com.sirma.cmf.web.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.minlog.Log;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.definition.model.Condition;
import com.sirma.itt.emf.domain.Pair;

/**
 * Utility methods for working with conditions defined in instances and for conversion to json.
 * 
 * @author svelikov
 */
public class CmfRncUtil {

	private static final Logger log = LoggerFactory.getLogger(CmfRncUtil.class);

	public static final CmfConditionsComparator CONDITIONS_COMPARATOR = new CmfConditionsComparator();

	/** The Constant EXPRESSION. */
	private static final String EXPRESSION = "expression";

	/** The Constant RENDER_AS. */
	private static final String RENDER_AS = "renderAs";

	/**
	 * Build a json object like following.
	 * 
	 * <pre>
	 *  CMF.RNC = {
	 *    "conditions":[
	 *       {
	 *          "id":"field2",
	 *          "condition":"[{\"expression\":\"+[f1] AND +[f2]\",\"renderAs\":\"HIDDEN\"},{\"expression\":\"[f2] IN ('opt1', 'opt2')\",\"renderAs\":\"DISABLED\"}]"
	 *       },
	 *       {
	 *          "id":"field1",
	 *          "condition":"[{\"expression\":\"+[f1] AND +[f2]\",\"renderAs\":\"HIDDEN\"},{\"expression\":\"[f2] IN ('opt1', 'opt2')\",\"renderAs\":\"DISABLED\"}]"
	 *       }
	 *    ],
	 *    "basePath":"basePath:",
	 *    "saveCondition":"saveCondition"
	 * }
	 * </pre>
	 * 
	 * @param conditions
	 *            The conditions map.
	 * @param basePath
	 *            the base path
	 * @param saveCondition
	 *            the save condition
	 * @return JSON object as string or null if conditions is empty and saveCondition is null
	 */
	public static String createCmfRncJson(Map<Pair<String, Priority>, List<Condition>> conditions,
			String basePath, String saveCondition) {

		String json = null;
		if (!conditions.isEmpty() || !StringUtils.isNullOrEmpty(saveCondition)) {

			JSONObject root = new JSONObject();
			try {

				root.put("basePath", basePath);
				root.put("saveCondition", saveCondition);

				JSONArray conditionsArray = new JSONArray();
				for (Entry<Pair<String, Priority>, List<Condition>> entry : conditions.entrySet()) {
					JSONObject condition = new JSONObject();
					condition.put("id", entry.getKey().getFirst());
					condition.put("condition", getConditionsForTarget(entry.getValue()));

					conditionsArray.put(condition);
				}
				root.put("conditions", conditionsArray);
			} catch (JSONException e) {
				Log.error("", e);
			}

			json = "CMF.RNC = " + root.toString() + ";";
		}

		return json;
	}

	/**
	 * Gets the conditions for target.
	 * 
	 * @param conditionsList
	 *            the conditions list
	 * @return the conditions for target
	 */
	public static String getConditionsForTarget(List<Condition> conditionsList) {

		JSONArray conditionsArray = new JSONArray();

		try {

			if ((conditionsList != null) && !conditionsList.isEmpty()) {
				for (Condition condition : conditionsList) {
					JSONObject conditionAsJson = new JSONObject();
					conditionAsJson.put(RENDER_AS, condition.getRenderAs());
					conditionAsJson.put(EXPRESSION, condition.getExpression());
					conditionsArray.put(conditionAsJson);
				}
			}

		} catch (JSONException e) {
			Log.error("", e);
		}
		return conditionsArray.toString();
	}

	/**
	 * Sort conditions by priority.
	 * 
	 * @param conditions
	 *            the conditions
	 * @return the map
	 */
	public static Map<Pair<String, Priority>, List<Condition>> sortConditionsByPriority(
			Map<Pair<String, Priority>, List<Condition>> conditions) {
		List<Map.Entry<Pair<String, Priority>, List<Condition>>> list = new ArrayList<Map.Entry<Pair<String, Priority>, List<Condition>>>(
				conditions.entrySet());
		Collections.sort(list, CONDITIONS_COMPARATOR);

		Map<Pair<String, Priority>, List<Condition>> result = new LinkedHashMap<Pair<String, Priority>, List<Condition>>();
		for (Map.Entry<Pair<String, Priority>, List<Condition>> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}

		return result;
	}
}
