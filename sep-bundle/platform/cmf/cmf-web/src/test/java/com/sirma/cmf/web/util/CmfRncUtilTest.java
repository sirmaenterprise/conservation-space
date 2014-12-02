package com.sirma.cmf.web.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.emf.definition.model.Condition;
import com.sirma.itt.emf.definition.model.ConditionDefinitionImpl;
import com.sirma.itt.emf.domain.Pair;

/**
 * The Class CmfRncUtilTest.
 */
@Test
public class CmfRncUtilTest {

	/**
	 * Instantiates a new cmf rnc util test.
	 */
	public CmfRncUtilTest() {
	}

	/**
	 * Creates the cmf rnc json.
	 */
	public void createCmfRncJsonTest() {
		Map<Pair<String, Priority>, List<Condition>> conditions = getConditionsMap();

		String json = CmfRncUtil.createCmfRncJson(conditions, "basePath:", "saveCondition");

		Assert.assertNotNull(json);

		// System.out.println(json);
	}

	/**
	 * Gets the conditions for target test.
	 */
	public void getConditionsForTargetTest() {
		String conditionsForTarget = CmfRncUtil.getConditionsForTarget(getConditions());

		Assert.assertNotNull(conditionsForTarget);

		// System.out.println(conditionsForTarget);
	}

	/**
	 * Gets the conditions map.
	 * 
	 * @return the conditions map
	 */
	private Map<Pair<String, Priority>, List<Condition>> getConditionsMap() {

		Map<Pair<String, Priority>, List<Condition>> conditions = new HashMap<Pair<String, Priority>, List<Condition>>();

		conditions.put(new Pair<String, Priority>("field1", Priority.SECOND), getConditions());
		conditions.put(new Pair<String, Priority>("field2", Priority.SECOND), getConditions());

		return conditions;
	}

	/**
	 * Gets the conditions.
	 * 
	 * @return the conditions
	 */
	private List<Condition> getConditions() {
		List<Condition> conditions = new ArrayList<Condition>();

		conditions.add(getCondition("+[f1] AND +[f2]", "HIDDEN"));
		conditions.add(getCondition("[f2] IN ('opt1', 'opt2')", "DISABLED"));

		return conditions;
	}

	/**
	 * Gets the condition.
	 * 
	 * @param expression
	 *            the expression
	 * @param renderAs
	 *            the render as
	 * @return the condition
	 */
	private ConditionDefinitionImpl getCondition(String expression, String renderAs) {
		ConditionDefinitionImpl condition = new ConditionDefinitionImpl();

		condition.setExpression(expression);
		condition.setRenderAs(renderAs);

		return condition;
	}

	/**
	 * Sort conditions by priority test.
	 */
	public void sortConditionsByPriorityTest() {
		Map<Pair<String, Priority>, List<Condition>> conditions = new HashMap<Pair<String, Priority>, List<Condition>>();
		conditions.put(new Pair<String, Priority>("trans-1", Priority.FOURTH), getConditions());
		conditions.put(new Pair<String, Priority>("field-2", Priority.SECOND), getConditions());
		conditions.put(new Pair<String, Priority>("save", Priority.THIRD), getConditions());
		conditions.put(new Pair<String, Priority>("field-1", Priority.SECOND), getConditions());
		conditions.put(new Pair<String, Priority>("region-1", Priority.FIRST), getConditions());
		conditions.put(new Pair<String, Priority>("field-3", Priority.SECOND), getConditions());

		//
		Map<Pair<String, Priority>, List<Condition>> sortedConditionsByPriority = CmfRncUtil
				.sortConditionsByPriority(conditions);

		// System.out.println("AFTER-----------------------------------");
		for (Pair<String, Priority> key : sortedConditionsByPriority.keySet()) {
			// System.out.println(key);
		}
	}

	/**
	 * Gets the conditions list.
	 * 
	 * @return the conditions list
	 */
	protected List<String> getConditionsList() {
		List<String> conditions = new ArrayList<String>();
		conditions.add("condition-1");
		conditions.add("condition-2");
		return conditions;
	}

}
