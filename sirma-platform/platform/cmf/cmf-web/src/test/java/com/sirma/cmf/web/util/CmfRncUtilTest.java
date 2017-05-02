package com.sirma.cmf.web.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.definition.model.ConditionDefinitionImpl;
import com.sirma.itt.seip.domain.definition.Condition;

import net.javacrumbs.jsonunit.JsonAssert;

/**
 * The Class CmfRncUtilTest.
 */
@Test
public class CmfRncUtilTest {

	/**
	 * Default constructor for the test
	 */
	public CmfRncUtilTest() {
		// nothing to do here
	}

	/**
	 * Create base RNC model.
	 */
	public void createCmfRncJsonTest() {
		String json = CmfRncUtil.createCmfRncJson(getConditionsMap(), "basePath:", "saveCondition",
				new HashMap<String, List<Object>>());
		Assert.assertNotNull(json);

	}

	/**
	 * Test conditions received from script result
	 */
	public void additionConditionAfterScriptExecuteTest() {
		Map<String, List<Object>> executedScriptResult = new HashMap<>();
		List<Object> messagesOne = new ArrayList<>(1);
		messagesOne.add("MESSAGE_ONE");
		List<Object> messagesTwo = new ArrayList<>(1);
		messagesTwo.add("MESSAGE_TWO");
		executedScriptResult.put("IDENTIFIER_ONE", messagesOne);
		executedScriptResult.put("IDENTIFIER_TWO", messagesTwo);
		List<Condition> conditions = getConditions();
		conditions.get(0).setIdentifier("IDENTIFIER_TWO");
		JsonAssert.assertJsonEquals(
				"[{\"expression\":\"+[f1] AND +[f2]AND[false]\",\"messages\":[\"MESSAGE_TWO\"],\"renderAs\":\"HIDDEN\",\"id\":\"IDENTIFIER_TWO\"},{\"expression\":\"[f2] IN ('opt1', 'opt2')\",\"renderAs\":\"DISABLED\"},{\"expression\":\"[false]\",\"messages\":[\"MESSAGE_ONE\"],\"renderAs\":\"ENABLED\",\"id\":\"IDENTIFIER_ONE\"}]",
				CmfRncUtil.getConditionsForTarget(conditions, executedScriptResult));
	}

	/**
	 * Gets the conditions for target test.
	 */
	public void getConditionsForTargetTest() {
		Assert.assertNotNull(CmfRncUtil.getConditionsForTarget(getConditions(), new HashMap<String, List<Object>>()));
	}

	/**
	 * Gets the conditions map.
	 *
	 * @return the conditions map
	 */
	private Map<Pair<String, Priority>, List<Condition>> getConditionsMap() {
		Map<Pair<String, Priority>, List<Condition>> conditions = new HashMap<>();
		conditions.put(new Pair<>("field1", Priority.SECOND), getConditions());
		conditions.put(new Pair<>("field2", Priority.SECOND), getConditions());
		return conditions;
	}

	/**
	 * Gets the conditions.
	 *
	 * @return the conditions
	 */
	private List<Condition> getConditions() {
		List<Condition> conditions = new ArrayList<>();
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
		Map<Pair<String, Priority>, List<Condition>> conditions = CmfRncUtil
				.sortConditionsByPriority(getCustomConditions());

		List<Priority> priorities = new ArrayList<>(Arrays.asList(Priority.FIRST, Priority.SECOND, Priority.SECOND,
				Priority.SECOND, Priority.THIRD, Priority.FOURTH));

		int i = 0;
		for (Pair<String, Priority> condition : conditions.keySet()) {
			Priority second = condition.getSecond();
			Assert.assertEquals(second, priorities.get(i));
			i++;
		}
	}

	/**
	 * Populate custom conditions for testing the order CmfRncUtil#sortConditionsByPriority
	 * 
	 * @return custom conditions
	 */
	private Map<Pair<String, Priority>, List<Condition>> getCustomConditions() {
		Map<Pair<String, Priority>, List<Condition>> conditions = new HashMap<>();
		conditions.put(new Pair<>("trans-1", Priority.FOURTH), getConditions());
		conditions.put(new Pair<>("field-2", Priority.SECOND), getConditions());
		conditions.put(new Pair<>("save", Priority.THIRD), getConditions());
		conditions.put(new Pair<>("field-1", Priority.SECOND), getConditions());
		conditions.put(new Pair<>("region-1", Priority.FIRST), getConditions());
		conditions.put(new Pair<>("field-3", Priority.SECOND), getConditions());
		return conditions;
	}

}
