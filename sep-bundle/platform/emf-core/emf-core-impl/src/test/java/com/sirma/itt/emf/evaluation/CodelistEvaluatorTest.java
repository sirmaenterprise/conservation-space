package com.sirma.itt.emf.evaluation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.codelist.CodelistService;
import com.sirma.itt.emf.codelist.model.CodeValue;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.FieldDefinitionImpl;
import com.sirma.itt.emf.definition.model.WritablePropertyDefinition;
import com.sirma.itt.emf.instance.model.EmfInstance;
import com.sirma.itt.emf.instance.model.Instance;


/**
 * The Class CodelistEvaluatorTest.
 *
 * @author BBonev
 */
@Test
public class CodelistEvaluatorTest extends BaseEvaluatorTest {

	/*
	 * Mock objects set int he tested evaluator
	 */
	/** The dictionary service. */
	private DictionaryService dictionaryService;
	/** The codelist service. */
	private CodelistService codelistService;

	/**
	 * Test fail evaluation.
	 */
	public void testFailEvaluation() {
		ExpressionsManager expressionsManager = createManager();
		Map<String, CodeValue> values = new HashMap<String, CodeValue>();
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

	/**
	 * Test evaluation.
	 */
	public void testEvaluation() {
		ExpressionsManager expressionsManager = createManager();
		Map<String, CodeValue> values = new HashMap<String, CodeValue>();
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
		ExpressionContext context = expressionsManager.createDefaultContext(target, definition,
				null);

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
		Mockito.when(dictionaryService.getProperty("property", 1L, target)).thenReturn(definition);
		evaluated = expressionsManager.evaluateRule("${CL([property])}", String.class, context);
		Assert.assertEquals(evaluated, "bgValue");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<ExpressionEvaluator> initializeEvaluators(ExpressionEvaluatorManager manager,
			TypeConverter converter) {
		List<ExpressionEvaluator> list = super.initializeEvaluators(manager, converter);
		// add id evaluator
		CodelistEvaluator evaluator = new CodelistEvaluator();
		dictionaryService = Mockito.mock(DictionaryService.class);
		codelistService = Mockito.mock(CodelistService.class);
		ReflectionUtils.setField(evaluator, "dictionaryService", dictionaryService);
		ReflectionUtils.setField(evaluator, "codelistService", codelistService);
		list.add(initEvaluator(evaluator, manager, createTypeConverter()));
		return list;
	}

}
