package com.sirma.itt.seip.expressions.conditions;

import java.util.Arrays;
import java.util.Collections;

import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.sirma.itt.seip.definition.jaxb.RenderAsType;
import com.sirma.itt.seip.domain.definition.Condition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Test class for ConditionsEvaluatorManager.
 * 
 * @author Hristo Lungov
 */
public class ConditionsEvaluatorManagerTest {

	@InjectMocks
	private ConditionsEvaluatorManager conditionsEvaluatorManager;

	@BeforeClass
	public void init() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void test_null_conditionType() {
		PropertyDefinition propertyDefinition = Mockito.mock(PropertyDefinition.class);
		Condition condition = Mockito.mock(Condition.class);
		Mockito.when(condition.getRenderAs()).thenReturn(RenderAsType.HIDDEN.value());
		Mockito.when(condition.getExpression()).thenReturn("[status] IN ('COMPLETED', 'STOPPED')");
		Instance instance = Mockito.mock(Instance.class);
		Mockito.when(instance.getAsString("status")).thenReturn("INIT");
		Mockito.when(propertyDefinition.getConditions()).thenReturn(Arrays.asList(condition, null));
		Assert.assertEquals(
				conditionsEvaluatorManager.evalPropertyConditions(propertyDefinition, null, instance),
				false);
	}
	
	@Test
	public void test_conditions_wiht_null_value() {
		PropertyDefinition propertyDefinition = Mockito.mock(PropertyDefinition.class);
		Condition condition = Mockito.mock(Condition.class);
		Mockito.when(condition.getRenderAs()).thenReturn(RenderAsType.HIDDEN.value());
		Mockito.when(condition.getExpression()).thenReturn("[status] IN ('COMPLETED', 'STOPPED')");
		Instance instance = Mockito.mock(Instance.class);
		Mockito.when(instance.getAsString("status")).thenReturn("INIT");
		Mockito.when(propertyDefinition.getConditions()).thenReturn(Arrays.asList(condition, null));
		Assert.assertEquals(
				conditionsEvaluatorManager.evalPropertyConditions(propertyDefinition, ConditionType.HIDDEN, instance),
				false);
	}

	@Test
	public void test_null_conditions() {
		PropertyDefinition propertyDefinition = Mockito.mock(PropertyDefinition.class);
		Instance instance = Mockito.mock(Instance.class);
		Mockito.when(propertyDefinition.getConditions()).thenReturn(null);
		Assert.assertEquals(
				conditionsEvaluatorManager.evalPropertyConditions(propertyDefinition, ConditionType.HIDDEN, instance),
				false);
	}

	@Test
	public void test_empty_conditions() {
		PropertyDefinition propertyDefinition = Mockito.mock(PropertyDefinition.class);
		Instance instance = Mockito.mock(Instance.class);
		Mockito.when(propertyDefinition.getConditions()).thenReturn(Collections.emptyList());
		Assert.assertEquals(
				conditionsEvaluatorManager.evalPropertyConditions(propertyDefinition, ConditionType.HIDDEN, instance),
				false);
	}

	@Test
	public void test_hidden_condition() {
		PropertyDefinition propertyDefinition = Mockito.mock(PropertyDefinition.class);
		Condition condition = Mockito.mock(Condition.class);
		Mockito.when(condition.getRenderAs()).thenReturn(RenderAsType.HIDDEN.value());
		Mockito.when(condition.getExpression()).thenReturn("[status] IN ('COMPLETED', 'STOPPED')");
		Instance instance = Mockito.mock(Instance.class);
		Mockito.when(instance.getAsString("status")).thenReturn("INIT").thenReturn("COMPLETED");
		Mockito.when(propertyDefinition.getConditions()).thenReturn(Arrays.asList(condition));
		Assert.assertEquals(
				conditionsEvaluatorManager.evalPropertyConditions(propertyDefinition, ConditionType.HIDDEN, instance),
				false);
		Assert.assertEquals(
				conditionsEvaluatorManager.evalPropertyConditions(propertyDefinition, ConditionType.HIDDEN, instance),
				true);
	}

}