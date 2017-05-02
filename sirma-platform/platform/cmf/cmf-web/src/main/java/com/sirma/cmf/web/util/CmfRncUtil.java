package com.sirma.cmf.web.util;

import java.lang.invoke.MethodHandles;
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

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.model.ConditionDefinitionImpl;
import com.sirma.itt.seip.domain.definition.Condition;

/**
 * Utility methods for working with conditions defined in instances and for conversion to json.
 *
 * @author svelikov
 */
public class CmfRncUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public static final CmfConditionsComparator CONDITIONS_COMPARATOR = new CmfConditionsComparator();

	private static final String MODEL_RNC_ATTRIBUTE_ID = "id";
	private static final String MODEL_RNC_ATTRIBUTE_BASEPATH = "basePath";
	private static final String MODEL_RNC_ATTRIBUTE_MESSAGES = "messages";
	private static final String MODEL_RNC_ATTRIBUTE_CONDITION = "condition";
	private static final String MODEL_RNC_ATTRIBUTE_CONDITIONS = "conditions";
	private static final String MODEL_RNC_ATTRIBUTE_SAVECONDITION = "saveCondition";

	private static final String MODEL_CONDITION_ATTRIBUTE_EXPRESSION = "expression";
	private static final String MODEL_CONDITION_ATTRIBUTE_RENDER_AS = "renderAs";
	private static final String EXPRESSION_AND = "AND";
	private static final String EXPRESSION_FALSE = "[false]";
	private static final String EXPRESSION_RENDERAS = "ENABLED";

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
	 * @param evaluatedScripts
	 * 			  the result from script evaluation
	 * @return JSON object as string or null if conditions is empty and saveCondition is null
	 */
	public static String createCmfRncJson(Map<Pair<String, Priority>, List<Condition>> conditions, String basePath,
			String saveCondition, Map<String, List<Object>> evaluatedScripts) {

		String json = null;
		if (!conditions.isEmpty() || !StringUtils.isNullOrEmpty(saveCondition)) {

			JSONObject rncModel = new JSONObject();
			try {
				rncModel.put(MODEL_RNC_ATTRIBUTE_BASEPATH, basePath);
				rncModel.put(MODEL_RNC_ATTRIBUTE_SAVECONDITION, saveCondition);

				JSONArray conditionsArray = new JSONArray();
				for (Entry<Pair<String, Priority>, List<Condition>> entry : conditions.entrySet()) {
					JSONObject condition = new JSONObject();
					condition.put(MODEL_RNC_ATTRIBUTE_ID, entry.getKey().getFirst());
					condition.put(MODEL_RNC_ATTRIBUTE_CONDITION,
							getConditionsForTarget(entry.getValue(), evaluatedScripts));
					conditionsArray.put(condition);
				}
				rncModel.put(MODEL_RNC_ATTRIBUTE_CONDITIONS, conditionsArray);
			} catch (JSONException e) {
				LOGGER.error("CmfRncUtil.createCmfRncJson cannot create RNC model", e);
			}

			json = "CMF.RNC = " + rncModel + ";";
		}

		return json;
	}

	/**
	 * Getter for RNC conditions model.
	 *
	 * @param conditionsList
	 *            transition conditions
	 * @param evaluatedScripts
	 *            transition script results
	 * @return RNC conditions model
	 */
	public static String getConditionsForTarget(List<Condition> conditionsList,
			Map<String, List<Object>> evaluatedScripts) {
		JSONArray conditionsArray = new JSONArray();
		try {
			if (CollectionUtils.isNotEmpty(conditionsList)) {
				for (Condition condition : conditionsList) {
					// build and store current transition
					conditionsArray.put(CmfRncUtil.buildConditionForRncModel(condition, evaluatedScripts));
				}
			}
			CmfRncUtil.buildAdditionalConditionsOnDemand(conditionsList, evaluatedScripts, conditionsArray);
		} catch (JSONException e) {
			LOGGER.error("CmfRncUtil.getConditionsForTarget - json object creation failed!", e);
		}
		return conditionsArray.toString();
	}

	/**
	 * Method that will build condition if there is no dynamic one.
	 *
	 * @param conditionsList
	 *            list with available conditions
	 * @param evaluatedScripts
	 *            map with transition identifiers and evaluated results from the script conditions
	 * @param conditionsArray
	 *            RNC model holder
	 * @throws JSONException
	 */
	private static void buildAdditionalConditionsOnDemand(List<Condition> conditionsList,
			Map<String, List<Object>> evaluatedScripts, JSONArray conditionsArray) throws JSONException {
		for (Map.Entry<String, List<Object>> entry : evaluatedScripts.entrySet()) {
			String key = entry.getKey();
			// check and build new condition if does not exist
			if (!CmfRncUtil.isScriptConditionAlreadyExist(key, conditionsList)) {
				ConditionDefinitionImpl condition = new ConditionDefinitionImpl();
				condition.setIdentifier(key);
				condition.setRenderAs(EXPRESSION_RENDERAS);
				conditionsArray.put(CmfRncUtil.buildConditionForRncModel(condition, evaluatedScripts));
			}
		}
	}

	/**
	 * Find condition based on identifier.
	 *
	 * @param identifier
	 *            transition identifier
	 * @param conditionsList
	 *            list with transition conditions
	 * @return true if the condition is found
	 */
	private static boolean isScriptConditionAlreadyExist(String identifier, List<Condition> conditionsList) {
		for (Condition condition : conditionsList) {
			if (identifier.equals(condition.getIdentifier())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Build condition with appropriate attributes for RNC model.
	 *
	 * @param condition
	 *            transition condition
	 * @param evaluatedScripts
	 *            transition script result
	 * @return JSON object, representing RNC condition model
	 * @throws JSONException
	 */
	public static JSONObject buildConditionForRncModel(Condition condition, Map<String, List<Object>> evaluatedScripts)
			throws JSONException {
		JSONObject conditionAsJson = new JSONObject();
		String expression = condition.getExpression();
		// if the executed script return error message, the expression will be updated
		StringBuilder expressionBuilder = new StringBuilder();
		if (evaluatedScripts.containsKey(condition.getIdentifier())) {
			if (StringUtils.isNullOrEmpty(expression)) {
				expression = expressionBuilder.append(EXPRESSION_FALSE).toString();
			} else {
				expression = expressionBuilder.append(expression).append(EXPRESSION_AND).append(EXPRESSION_FALSE).toString();
			}
			conditionAsJson.put(MODEL_RNC_ATTRIBUTE_MESSAGES, evaluatedScripts.get(condition.getIdentifier()));
			conditionAsJson.put(MODEL_RNC_ATTRIBUTE_ID, condition.getIdentifier());
		}

		conditionAsJson.put(MODEL_CONDITION_ATTRIBUTE_RENDER_AS, condition.getRenderAs());
		conditionAsJson.put(MODEL_CONDITION_ATTRIBUTE_EXPRESSION, expression);
		return conditionAsJson;
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
		List<Map.Entry<Pair<String, Priority>, List<Condition>>> list = new ArrayList<>(conditions.entrySet());
		Collections.sort(list, CONDITIONS_COMPARATOR);

		Map<Pair<String, Priority>, List<Condition>> result = new LinkedHashMap<>();
		for (Map.Entry<Pair<String, Priority>, List<Condition>> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}

		return result;
	}
}
