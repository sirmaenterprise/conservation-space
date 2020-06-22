package com.sirma.sep.instance.properties.expression.evaluation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.testutil.mocks.DataTypeDefinitionMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;
import com.sirma.sep.instance.properties.expression.evaluation.BooleanPropertyValueEvaluator;

/**
 * Test for {@link BooleanPropertyValueEvaluator}.
 *
 * @author A. Kunchev
 */
public class BooleanPropertyValueEvaluatorTest {

	private BooleanPropertyValueEvaluator evaluator;

	@Before
	public void setup() {
		evaluator = new BooleanPropertyValueEvaluator();
	}

	@Test
	public void canEvaluate_notBooleanProperty_false() {
		PropertyDefinition propertyDefinition = new PropertyDefinitionMock();
		propertyDefinition.setDataType(new DataTypeDefinitionMock(DataTypeDefinition.ANY));
		assertFalse(evaluator.canEvaluate(propertyDefinition, null));
	}

	@Test
	public void canEvaluate_booleanProperty_true() {
		PropertyDefinition propertyDefinition = new PropertyDefinitionMock();
		propertyDefinition.setDataType(new DataTypeDefinitionMock(DataTypeDefinition.BOOLEAN));
		assertTrue(evaluator.canEvaluate(propertyDefinition, null));
	}

	@Test
	public void evaluate_falseValue() {
		Instance instance = new EmfInstance();
		instance.add("booleanPropertyName", false);
		assertEquals("false", evaluator.evaluate(instance, "booleanPropertyName"));
	}

	@Test
	public void evaluate_trueValue() {
		Instance instance = new EmfInstance();
		instance.add("booleanPropertyName", true);
		assertEquals("true", evaluator.evaluate(instance, "booleanPropertyName"));
	}

}
