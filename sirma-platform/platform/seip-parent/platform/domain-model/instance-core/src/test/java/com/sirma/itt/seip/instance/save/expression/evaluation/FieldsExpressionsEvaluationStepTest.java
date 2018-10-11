package com.sirma.itt.seip.instance.save.expression.evaluation;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.ControlParam;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.state.Operation;

/**
 * Test for FieldsExpressionsEvaluationStep.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 10/07/2017
 */
public class FieldsExpressionsEvaluationStepTest {

	private static final String VALUE_WITHOUT_EXPRESSION = "value without expression";

	@InjectMocks
	private FieldsExpressionsEvaluationStep cut;

	@Mock
	private DefinitionService definitionService;
	@Mock
	private ExpressionsManager expressionManager;

	// Definition related mocks.
	@Mock
	private DefinitionModel definition;
	@Mock
	private ControlDefinition controlDefinition;
	@Mock
	private PropertyDefinition property;
	@Mock
	private ExpressionContext expressionContext;

	private Instance instance;
	private InstanceSaveContext context;

	@Before
	public void init() {
		cut = new FieldsExpressionsEvaluationStep();
		MockitoAnnotations.initMocks(this);

		mockExpressionManager();
		mockInstanceData("field", "${emf-expression-one} some text ${emf-expression-two}");

		when(definitionService.getInstanceDefinition(any())).thenReturn(definition);

		when(property.getControlDefinition()).thenReturn(controlDefinition);
		when(property.getName()).thenReturn("field");
		when(definition.fieldsStream()).thenReturn(Stream.of(property));
	}

	@Test
	public void beforeSave_expressionFunctions() {
		mockInstanceData("field-without-expression", VALUE_WITHOUT_EXPRESSION);
		cut.beforeSave(context);
		String resultExpression = (String) instance.get("field-without-expression");
		Assert.assertEquals(resultExpression, VALUE_WITHOUT_EXPRESSION);
	}

	@Test
	public void testGetName() {
		Assert.assertEquals(cut.getName(), "evaluateExpressionStep");
	}

	@Test(expected = FieldExpressionEvaluationException.class)
	public void beforeSave_expressionEvaluationFailNull() {
		mockControlParams();
		when(expressionManager.createDefaultContext(any(), any(), any())).thenReturn(expressionContext);
		when(expressionManager.evaluateRule("${emf-expression-one}", String.class, expressionContext)).thenReturn(null);
		cut.beforeSave(context);
	}

	@Test(expected = FieldExpressionEvaluationException.class)
	public void beforeSave_couldNotExtractDefinition() {
		mockControlParams();
		when(definitionService.getInstanceDefinition(any())).thenReturn(null);
		cut.beforeSave(context);
	}

	@Test(expected = FieldExpressionEvaluationException.class)
	public void beforeSave_expressionEvaluationFail() {
		mockControlParams();
		when(expressionManager.createDefaultContext(any(), any(), any())).thenReturn(expressionContext);
		when(expressionManager.evaluateRule("${emf-expression-one}", String.class, expressionContext))
				.thenReturn("${emf-expression-one}");
		cut.beforeSave(context);
	}

	@Test
	public void beforeSave_success() {
		mockControlParams();
		cut.beforeSave(context);
		String resultExpression = (String) instance.get("field");
		assertTrue(resultExpression.contains("evaluated-expression-one"));
		assertTrue(resultExpression.contains("evaluated-expression-two"));
	}

	@Test
	public void beforeSave_skipNull() {
		mockControlParams();
		instance.remove("field");
		cut.beforeSave(context);
		String resultExpression = (String) instance.get("field");
		assertNull(resultExpression);
	}

	@Test
	public void beforeSave_skipNoExpression() {
		mockControlParams();
		instance.add("field", "no_expr");
		cut.beforeSave(context);
		String resultExpression = (String) instance.get("field");
		assertEquals("no_expr", resultExpression);
	}

	private void mockControlParams() {
		ControlParam functionOne = mock(ControlParam.class);
		when(functionOne.getIdentifier()).thenReturn("function");
		when(functionOne.getType()).thenReturn("default_value_pattern");
		when(functionOne.getName()).thenReturn("first-label-id");
		when(functionOne.getValue()).thenReturn("${emf-expression-one}");

		ControlParam functionTwo = mock(ControlParam.class);
		when(functionTwo.getIdentifier()).thenReturn("function");
		when(functionTwo.getType()).thenReturn("default_value_pattern");
		when(functionTwo.getName()).thenReturn("second-label-id");
		when(functionTwo.getValue()).thenReturn("${emf-expression-two}");

		List<ControlParam> parameters = new ArrayList<>();
		parameters.add(functionOne);
		parameters.add(functionTwo);
		when(controlDefinition.getControlParams()).thenReturn(parameters);
	}

	private void mockExpressionManager() {
		when(expressionManager.createDefaultContext(any(), any(), any())).thenReturn(expressionContext);
		when(expressionManager.evaluateRule("${emf-expression-one}", String.class, expressionContext))
				.thenReturn("evaluated-expression-one");
		when(expressionManager.evaluateRule("${emf-expression-two}", String.class, expressionContext))
				.thenReturn("evaluated-expression-two");
	}

	private void mockInstanceData(String propertyName, String propertyValue) {
		instance = new ObjectInstance();
		instance.getProperties().put(propertyName, propertyValue);
		context = InstanceSaveContext.create(instance, new Operation("create"));
	}
}