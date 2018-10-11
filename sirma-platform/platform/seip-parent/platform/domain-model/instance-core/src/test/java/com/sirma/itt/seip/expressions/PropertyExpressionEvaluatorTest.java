package com.sirma.itt.seip.expressions;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.expressions.ExpressionEvaluator;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.ResourceProperties;

/**
 * The Class PropertyExressionTest.
 *
 * @author BBonev
 */
@Test
public class PropertyExpressionEvaluatorTest extends BaseEvaluatorTest {

	/**
	 * Test evaluation.
	 */
	public void testEvaluation() {
		EmfUser user = new EmfUser("admin");
		user.getProperties().put(ResourceProperties.LANGUAGE, "en");

		ExpressionsManager expressionsManager = createManager();

		EmfInstance target = new EmfInstance();
		target.setId("1");
		target.add("key", "value");
		target.add("mvKey1", (Serializable) Arrays.asList("value"));
		target.add("mvKey2", (Serializable) Arrays.asList("value1", "value2"));
		Root root = new Root();
		root.setId("2");
		root.getProperties().put("key", "rootValue");
		contextService.bindContext(target, root);
		ExpressionContext context = expressionsManager.createDefaultContext(target, null, null);

		ExpressionEvaluator evaluator = expressionsManager.getEvaluator("get", "${get([key])}");

		Assert.assertEquals("value", evaluator.evaluate("${get([key])}", context));
		// should flatten the single multi value key
		Assert.assertEquals("value", evaluator.evaluate("${get([mvKey1])}", context));

		// do not modify multiple multi value field
		Assert.assertEquals(Arrays.asList("value1", "value2"), evaluator.evaluate("${get([mvKey2])}", context));
		Assert.assertEquals("otherValue", evaluator.evaluate("${get([otherKey], otherValue)}", context));
		Assert.assertEquals("value", evaluator.evaluate("${get([key], otherValue)}", context));
		Assert.assertEquals(2, evaluator.evaluate("${get([otherKey], cast(2 as int))}", context));
		Assert.assertEquals(2, evaluator.evaluate("${get([otherKey], cast(2 as integer))}", context));
		Assert.assertEquals(2l, evaluator.evaluate("${get([otherKey], cast(2 as long))}", context));
		Assert.assertEquals(2f, evaluator.evaluate("${get([otherKey], cast(2 as float))}", context));
		// date conversion
		Date value = new Date();
		String dateString = createTypeConverter().convert(String.class, value);
		Serializable result = evaluator.evaluate("${get([otherKey], cast(" + dateString + " as date))}", context);
		Assert.assertNotNull(result);
		Assert.assertEquals(Date.class, result.getClass());
		Assert.assertEquals(dateString, createTypeConverter().convert(String.class, result));
		result = evaluator.evaluate("${get([otherKey], cast(" + dateString + " as datetime))}", context);
		Assert.assertNotNull(result);
		Assert.assertEquals(Date.class, result.getClass());
		Assert.assertEquals(dateString, createTypeConverter().convert(String.class, result));
		result = evaluator.evaluate("${get([otherKey], cast(" + dateString + " as time))}", context);
		Assert.assertNotNull(result);
		Assert.assertEquals(Date.class, result.getClass());
		Assert.assertEquals(dateString, createTypeConverter().convert(String.class, result));

		Assert.assertEquals("rootValue", evaluator.evaluate("${get([key]).from(rootContext)}", context));
	}

	/**
	 * Test escaping.
	 */
	public void testEscaping() {
		ExpressionsManager expressionsManager = createManager();
		EmfInstance target = new EmfInstance();
		target.setProperties(new HashMap<String, Serializable>());
		String value = "<code>function force() { return callForce(); }</code>";
		target.getProperties().put(DefaultProperties.HEADER_DEFAULT, value);
		target.getProperties().put("someRandomKeyName", value);
		ExpressionContext context = expressionsManager.createDefaultContext(target, null, null);

		String unescaped = expressionsManager.evaluateRule("${get([" + DefaultProperties.HEADER_DEFAULT + "])}",
				String.class, context);
		Assert.assertEquals(unescaped, value);
		String escaped = expressionsManager.evaluateRule("${get([someRandomKeyName])}", String.class, context);
		Assert.assertNotEquals(escaped, value);
		Assert.assertEquals(escaped, StringEscapeUtils.escapeHtml(value));

	}

	/**
	 * The Class Root.
	 */
	static class Root extends EmfInstance {
		@Override
		public Map<String, Serializable> getProperties() {
			if (super.getProperties() == null) {
				setProperties(new HashMap<String, Serializable>());
			}
			return super.getProperties();
		}
	}

}
