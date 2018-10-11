package com.sirma.itt.seip.expressions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.WritablePropertyDefinition;
import com.sirma.itt.seip.definition.model.FieldDefinitionImpl;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.expressions.ExpressionEvaluator;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * The Class CodelistEvaluatorTest.
 *
 * @author BBonev
 */
@Test
public class CodelistEvaluatorTest extends BaseEvaluatorTest {

	private DefinitionService definitionService;
	private CodelistService codelistService;

	public void testFailEvaluation() {
		ExpressionsManager expressionsManager = createManager();
		Map<String, CodeValue> values = new HashMap<>();
		CodeValue value = new CodeValue();
		value.setCodelist(1);
		value.setValue("value");
		value.setProperties(new HashMap<String, Serializable>());
		value.getProperties().put("en", "enValue");
		value.getProperties().put("bg", "bgValue");
		values.put("value", value);
		Mockito.when(codelistService.getCodeValues(1)).thenReturn(values);

		// missing information we return the value
		String evaluated = expressionsManager.evaluate("${CL1(value1)}", String.class);
		Assert.assertEquals(evaluated, "value1");
		evaluated = expressionsManager.evaluate("${CL(value1)}", String.class);
		Assert.assertEquals(evaluated, "value1");
		evaluated = expressionsManager.evaluate("${CL1([property])}", String.class);
		Assert.assertEquals(evaluated, null);
		evaluated = expressionsManager.evaluate("${CL([property])}", String.class);
		Assert.assertEquals(evaluated, null);
		evaluated = expressionsManager.evaluate("${CL(value1).test}", String.class);
		Assert.assertEquals(evaluated, "value1");

		// invalid expressions
		evaluated = expressionsManager.evaluate("${CL([value1)}", String.class);
		Assert.assertEquals(evaluated, "${CL([value1)}");
		evaluated = expressionsManager.evaluate("${CL(value1])}", String.class);
		Assert.assertEquals(evaluated, "${CL(value1])}");
	}

	public void testEvaluation() {
		ExpressionsManager expressionsManager = createManager();
		Map<String, CodeValue> values = new HashMap<>();
		CodeValue value = new CodeValue();
		value.setCodelist(1);
		value.setValue("value");
		value.setProperties(new HashMap<String, Serializable>());
		value.getProperties().put("en", "enValue");
		value.getProperties().put("bg", "bgValue");
		values.put("value", value);
		Mockito.when(codelistService.getCodeValues(1)).thenReturn(values);

		// basic scenario
		String evaluated = expressionsManager.evaluate("${CL1(value)}", String.class);
		// this works because the base test is set to initialize the current user default language
		// as bg. See EmfTest.setCurrentUser()
		Assert.assertEquals(evaluated, "bgValue");
		evaluated = expressionsManager.evaluate("${CL1(value).en}", String.class);
		Assert.assertEquals(evaluated, "enValue");

		WritablePropertyDefinition definition = new FieldDefinitionImpl();
		definition.setIdentifier("property");
		definition.setCodelist(1);
		Instance target = new EmfInstance();
		target.setProperties(new HashMap<String, Serializable>());
		target.getProperties().put("property", "value");
		target.setRevision(1L);
		ExpressionContext context = expressionsManager.createDefaultContext(target, definition, null);

		evaluated = expressionsManager.evaluateRule("${CL1([property])}", String.class, context);
		Assert.assertEquals(evaluated, "bgValue");

		// the codelist is fetched from the current property definition fetched from the context and
		// not from the definition service
		evaluated = expressionsManager.evaluateRule("${CL([property])}", String.class, context);
		Assert.assertEquals(evaluated, "bgValue");
		// same as above
		evaluated = expressionsManager.evaluateRule("${CL(value)}", String.class, context);
		Assert.assertEquals(evaluated, "bgValue");

		// not let try it from the dictionary service
		Mockito.when(definitionService.getProperty("property", 1L, target)).thenReturn(definition);
		evaluated = expressionsManager.evaluateRule("${CL([property])}", String.class, context);
		Assert.assertEquals(evaluated, "bgValue");
	}

	@Test
	public void testMultipeValueEvaluation() {
		ExpressionsManager expressionsManager = createManager();
		Map<String, CodeValue> values = new HashMap<>();
		CodeValue value = new CodeValue();
		value.setCodelist(1);
		value.setValue("value");
		value.setProperties(new HashMap<String, Serializable>());
		value.getProperties().put("en", "enValue");
		value.getProperties().put("bg", "bgValue");
		values.put("value", value);
		CodeValue value2 = new CodeValue();
		value2.setCodelist(1);
		value2.setValue("value2");
		value2.setProperties(new HashMap<String, Serializable>());
		value2.getProperties().put("en", "enValue2");
		value2.getProperties().put("bg", "bgValue2");
		values.put("value2", value2);
		Mockito.when(codelistService.getCodeValues(1)).thenReturn(values);

		WritablePropertyDefinition definition = new FieldDefinitionImpl();
		definition.setIdentifier("value");
		definition.setCodelist(1);

		ArrayList<String> properties = new ArrayList<>();
		properties.add("value");
		properties.add("value2");

		Instance target = new EmfInstance();
		target.setProperties(new HashMap<String, Serializable>());
		target.getProperties().put("value", properties);
		target.setRevision(1L);
		ExpressionContext context = expressionsManager.createDefaultContext(target, definition, null);

		String evaluated = expressionsManager.evaluateRule("${CL1([value])}", String.class, context);
		Assert.assertEquals(evaluated, "bgValue,bgValue2");
		evaluated = expressionsManager.evaluateRule("${CL1([value]).en}", String.class, context);
		Assert.assertEquals(evaluated, "enValue,enValue2");
	}

	/**
	 * If no value is present, the key will be returned.
	 */
	@Test
	public void testMultipeMissingPropertyEvaluation() {
		ExpressionsManager expressionsManager = createManager();
		Map<String, CodeValue> values = new HashMap<>();
		Mockito.when(codelistService.getCodeValues(1)).thenReturn(values);

		WritablePropertyDefinition definition = new FieldDefinitionImpl();
		definition.setIdentifier("value");
		definition.setCodelist(1);

		ArrayList<String> properties = new ArrayList<>();
		properties.add("value");
		properties.add("value2");

		Instance target = new EmfInstance();
		target.setProperties(new HashMap<String, Serializable>());
		target.getProperties().put("value", properties);
		target.setRevision(1L);
		ExpressionContext context = expressionsManager.createDefaultContext(target, definition, null);

		String evaluated = expressionsManager.evaluateRule("${CL1([value])}", String.class, context);
		Assert.assertEquals(evaluated, "value,value2");
		evaluated = expressionsManager.evaluateRule("${CL1([value]).en}", String.class, context);
		Assert.assertEquals(evaluated, "value,value2");
	}

	/**
	 * If no a null property is present, the key will be returned.
	 */
	@Test
	public void testMultipeNullPropertyEvaluation() {
		ExpressionsManager expressionsManager = createManager();
		Map<String, CodeValue> values = new HashMap<>();
		CodeValue value = new CodeValue();
		value.setCodelist(1);
		value.setValue("value");
		value.setProperties(new HashMap<String, Serializable>());
		value.getProperties().put("en", null);
		value.getProperties().put("bg", "bgValue");
		values.put("value", value);
		CodeValue value2 = new CodeValue();
		value2.setCodelist(1);
		value2.setValue("value2");
		value2.setProperties(new HashMap<String, Serializable>());
		value2.getProperties().put("en", "enValue2");
		value2.getProperties().put("bg", null);
		values.put("value2", value2);
		Mockito.when(codelistService.getCodeValues(1)).thenReturn(values);

		WritablePropertyDefinition definition = new FieldDefinitionImpl();
		definition.setIdentifier("value");
		definition.setCodelist(1);

		ArrayList<String> properties = new ArrayList<>();
		properties.add("value");
		properties.add("value2");

		Instance target = new EmfInstance();
		target.setProperties(new HashMap<String, Serializable>());
		target.getProperties().put("value", properties);
		target.setRevision(1L);
		ExpressionContext context = expressionsManager.createDefaultContext(target, definition, null);

		String evaluated = expressionsManager.evaluateRule("${CL1([value])}", String.class, context);
		Assert.assertEquals(evaluated, "bgValue,value2");
		evaluated = expressionsManager.evaluateRule("${CL1([value]).en}", String.class, context);
		Assert.assertEquals(evaluated, "value,enValue2");
	}

	@Override
	protected List<ExpressionEvaluator> initializeEvaluators(ExpressionsManager manager,
			TypeConverter converter) {
		List<ExpressionEvaluator> list = super.initializeEvaluators(manager, converter);
		// add id evaluator
		CodelistEvaluator evaluator = new CodelistEvaluator();
		definitionService = Mockito.mock(DefinitionService.class);
		codelistService = Mockito.mock(CodelistService.class);
		ReflectionUtils.setFieldValue(evaluator, "definitionService", definitionService);
		ReflectionUtils.setFieldValue(evaluator, "codelistService", codelistService);
		list.add(initEvaluator(evaluator, manager, createTypeConverter()));
		return list;
	}
}