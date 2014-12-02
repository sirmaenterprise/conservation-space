package com.sirma.itt.emf.evaluation;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Instance;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.instance.model.EmfInstance;
import com.sirma.itt.emf.instance.model.RootInstanceContext;
import com.sirma.itt.emf.resources.ResourceProperties;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.security.model.EmfUser;
import com.sirma.itt.emf.util.EmfTest;
import com.sirma.itt.emf.util.InstanceProxyMock;

/**
 * The Class PropertyExressionTest.
 *
 * @author BBonev
 */
public class PropertyExressionTest extends EmfTest {

	/**
	 * Test evaluation.
	 */
	@Test
	public void testEvaluation() {
		EmfUser user = new EmfUser("admin");
		user.getProperties().put(ResourceProperties.LANGUAGE, "en");
		SecurityContextManager.authenticateFullyAs(user);

		PropertyExpressionEvaluator evaluator = new PropertyExpressionEvaluator();
		ReflectionUtils.setField(evaluator, "converter", createTypeConverter());

		ExpressionEvaluator fromEvaluator = new FromEvaluator();
		ReflectionUtils.setField(fromEvaluator, "converter", createTypeConverter());

		ExpressionsManager expressionsManager = new ExpressionEvaluatorManager();
		Instance<ExpressionEvaluator> evaluators = new InstanceProxyMock<ExpressionEvaluator>(
				fromEvaluator, evaluator);
		ReflectionUtils.setField(expressionsManager, "evaluators", evaluators);
		ReflectionUtils.setField(expressionsManager, "typeConverter", createTypeConverter());

		Instance<ExpressionsManager> expressionManagerInstance = new InstanceProxyMock<ExpressionsManager>(
				expressionsManager);

		ReflectionUtils.setField(evaluator, "expressionManager", expressionManagerInstance);

		EmfInstance target = new EmfInstance();
		target.setProperties(new HashMap<String, Serializable>());
		target.getProperties().put("key", "value");
		Root root = new Root();
		root.getProperties().put("key", "rootValue");
		target.setOwningInstance(root);
		ExpressionContext context = expressionsManager.createDefaultContext(target, null, null);

		Assert.assertEquals("value", evaluator.evaluate("${get([key])}", context));
		Assert.assertEquals("otherValue", evaluator.evaluate("${get([otherKey], otherValue)}", context));
		Assert.assertEquals("value", evaluator.evaluate("${get([key], otherValue)}", context));
		Assert.assertEquals(2, evaluator.evaluate("${get([otherKey], cast(2 as int))}", context));
		Assert.assertEquals(2, evaluator.evaluate("${get([otherKey], cast(2 as integer))}", context));
		Assert.assertEquals(2l, evaluator.evaluate("${get([otherKey], cast(2 as long))}", context));
		Assert.assertEquals(2f, evaluator.evaluate("${get([otherKey], cast(2 as float))}", context));
		// date conversion
		Date value = new Date();
		String dateString = createTypeConverter().convert(String.class, value);
		Serializable result = evaluator.evaluate("${get([otherKey], cast(" + dateString
				+ " as date))}", context);
		Assert.assertNotNull(result);
		Assert.assertEquals(Date.class, result.getClass());
		Assert.assertEquals(dateString, createTypeConverter().convert(String.class, result));
		result = evaluator.evaluate("${get([otherKey], cast(" + dateString + " as datetime))}",
				context);
		Assert.assertNotNull(result);
		Assert.assertEquals(Date.class, result.getClass());
		Assert.assertEquals(dateString, createTypeConverter().convert(String.class, result));
		result = evaluator
				.evaluate("${get([otherKey], cast(" + dateString + " as time))}", context);
		Assert.assertNotNull(result);
		Assert.assertEquals(Date.class, result.getClass());
		Assert.assertEquals(dateString, createTypeConverter().convert(String.class, result));

		Assert.assertEquals("rootValue", evaluator.evaluate("${get([key]).from(rootContext)}", context));
	}

	/**
	 * The Class Root.
	 */
	static class Root extends EmfInstance implements RootInstanceContext {
		@Override
		public Map<String, Serializable> getProperties() {
			if (super.getProperties() == null) {
				setProperties(new HashMap<String, Serializable>());
			}
			return super.getProperties();
		}
	}

}
