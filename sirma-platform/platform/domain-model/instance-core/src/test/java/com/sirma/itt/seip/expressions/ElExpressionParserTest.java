package com.sirma.itt.seip.expressions;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.Serializable;
import java.util.HashMap;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.UriConverterProvider;
import com.sirma.itt.seip.convert.UriConverterProvider.StringUriProxy;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * The Class ElExpressionParserTest.
 *
 * @author BBonev
 */
@Test
public class ElExpressionParserTest extends BaseEvaluatorTest {

	/**
	 * Test parsing.
	 */
	public void testParsing() {
		ElExpression expression = ElExpressionParser.parse("${eval(${get([test])})}");
		Assert.assertEquals("{0}", expression.getExpression());
		Assert.assertEquals(1, expression.getSubExpressions().size());
		Assert.assertEquals("$'{'eval({0})'}'", expression.getSubExpressions().get(0).getExpression());
		Assert.assertEquals("eval", expression.getSubExpressions().get(0).getExpressionId());
		Assert.assertEquals("$'{'get([test])'}'",
				expression.getSubExpressions().get(0).getSubExpressions().get(0).getExpression());
		Assert.assertEquals("get", expression.getSubExpressions().get(0).getSubExpressions().get(0).getExpressionId());

		expression = ElExpressionParser.parse("${get([test])}");
		Assert.assertEquals("{0}", expression.getExpression());
		Assert.assertEquals(null, expression.getExpressionId());
		Assert.assertEquals(1, expression.getSubExpressions().size());
		Assert.assertEquals("$'{'get([test])'}'", expression.getSubExpressions().get(0).getExpression());
		Assert.assertEquals("get", expression.getSubExpressions().get(0).getExpressionId());

		expression = ElExpressionParser.parse("${eval(${get([test], defaultTest)} and ${today})}");
		Assert.assertEquals("{0}", expression.getExpression());
		Assert.assertEquals(1, expression.getSubExpressions().size());
		Assert.assertEquals("$'{'eval({0} and {1})'}'", expression.getSubExpressions().get(0).getExpression());
		Assert.assertEquals(2, expression.getSubExpressions().get(0).getSubExpressions().size());
		Assert.assertEquals("$'{'get([test], defaultTest)'}'",
				expression.getSubExpressions().get(0).getSubExpressions().get(0).getExpression());
		Assert.assertEquals("$'{'today'}'",
				expression.getSubExpressions().get(0).getSubExpressions().get(1).getExpression());
		Assert.assertEquals("today",
				expression.getSubExpressions().get(0).getSubExpressions().get(1).getExpressionId());

		expression = ElExpressionParser.parse("${eval(${get([test], ${today(-7)})} and ${today})}");
		Assert.assertEquals("{0}", expression.getExpression());
		Assert.assertEquals(1, expression.getSubExpressions().size());
		Assert.assertEquals("$'{'eval({0} and {1})'}'", expression.getSubExpressions().get(0).getExpression());
		Assert.assertEquals(2, expression.getSubExpressions().get(0).getSubExpressions().size());
		Assert.assertEquals("$'{'get([test], {0})'}'",
				expression.getSubExpressions().get(0).getSubExpressions().get(0).getExpression());
		Assert.assertEquals("$'{'today(-7)'}'", expression
				.getSubExpressions()
				.get(0)
				.getSubExpressions()
				.get(0)
				.getSubExpressions()
				.get(0)
				.getExpression());
		Assert.assertEquals("$'{'today'}'",
				expression.getSubExpressions().get(0).getSubExpressions().get(1).getExpression());

		expression = ElExpressionParser.parse("${eval(${if(${get([test2])} == testValue).then(true).else(false)})}");
		Assert.assertEquals("{0}", expression.getExpression());
		Assert.assertEquals(1, expression.getSubExpressions().size());
		Assert.assertEquals("$'{'eval({0})'}'", expression.getSubExpressions().get(0).getExpression());
		expression = expression.getSubExpressions().get(0).getSubExpressions().get(0);
		Assert.assertEquals("$'{'if({0} == testValue).then(true).else(false)'}'", expression.getExpression());
		Assert.assertEquals("if", expression.getExpressionId());
		Assert.assertEquals(1, expression.getSubExpressions().size());
		Assert.assertEquals("$'{'get([test2])'}'", expression.getSubExpressions().get(0).getExpression());

	}

	/**
	 * Test lazy expression parsing.
	 */
	public void testLazyExpressionParsing() {
		ElExpression expression = ElExpressionParser.parse("#{eval(#{get([test])})}",
				ElExpressionParser.LAZY_EXPRESSION_ID);
		Assert.assertNotNull(expression);
		Assert.assertEquals("{0}", expression.getExpression());
		Assert.assertEquals(1, expression.getSubExpressions().size());
		Assert.assertEquals("#'{'eval({0})'}'", expression.getSubExpressions().get(0).getExpression());
		Assert.assertEquals("#'{'get([test])'}'",
				expression.getSubExpressions().get(0).getSubExpressions().get(0).getExpression());
	}

	/**
	 * Test mixed expression parsing.
	 */
	public void testMixedExpressionParsing() {
		ElExpression expression = ElExpressionParser.parse("${eval(#{get([test])})}");
		Assert.assertNotNull(expression);
		Assert.assertEquals(expression.getExpression(), "{0}");
		Assert.assertEquals(1, expression.getSubExpressions().size());
		Assert.assertEquals(expression.getSubExpressions().get(0).getExpression(), "$'{'eval(#'{'get([test])'}')'}'");

		expression = ElExpressionParser.parse(
				"${eval(Some text #{get([test])} some other text ${get([test])} and ${if(2==2).then(#{get[test2]})})}");
		Assert.assertNotNull(expression);
		Assert.assertEquals(expression.getExpression(), "{0}");
		Assert.assertEquals(expression.getSubExpressions().size(), 1);
		Assert.assertEquals(expression.getSubExpressions().get(0).getExpression(),
				"$'{'eval(Some text #'{'get([test])'}' some other text {0} and {1})'}'");
		Assert.assertEquals(expression.getSubExpressions().get(0).getSubExpressions().size(), 2);
		Assert.assertEquals(expression.getSubExpressions().get(0).getSubExpressions().get(1).getExpression(),
				"$'{'if(2==2).then(#'{'get[test2]'}')'}'");
	}

	/**
	 * Test mixed expression eval.
	 */
	public void testMixedExpressionEval() {
		ExpressionsManager manager = createManager();
		Instance target = new EmfInstance();
		target.setProperties(new HashMap<String, Serializable>());
		target.getProperties().put("test", "testValue");
		target.getProperties().put("test2", "testValue2");
		ExpressionContext context = manager.createDefaultContext(target, null, null);
		TypeConverter converter = createTypeConverter();

		ElExpression exp = ElExpressionParser.parse("${eval(#{get([test])})}");
		Serializable serializable = ElExpressionParser.eval(exp, manager, converter, context,
				ElExpressionParser.DEFAULT_EXPRESSION_ID);
		Assert.assertEquals(serializable, "#{get([test])}");

		exp = ElExpressionParser.parse("${eval({get([test])})}");
		serializable = ElExpressionParser.eval(exp, manager, converter, context,
				ElExpressionParser.DEFAULT_EXPRESSION_ID);
		Assert.assertEquals(serializable, "{get([test])}");

		// first pass parsing and evaluation
		exp = ElExpressionParser.parse(
				"${eval(Some text #{get([test])} some other text ${get([test])} and ${if(2==2).then(#{get([test2])})})}");
		serializable = ElExpressionParser.eval(exp, manager, converter, context,
				ElExpressionParser.DEFAULT_EXPRESSION_ID);
		// the intermediate result
		Assert.assertEquals(serializable, "Some text #{get([test])} some other text testValue and #{get([test2])}");
		exp = ElExpressionParser.parse("#{eval(" + serializable + ")}", ElExpressionParser.LAZY_EXPRESSION_ID);

		// second pass parsing
		serializable = ElExpressionParser.eval(exp, manager, converter, context, ElExpressionParser.LAZY_EXPRESSION_ID);
		// final result
		Assert.assertEquals(serializable, "Some text testValue some other text testValue and testValue2");
	}

	/**
	 * Test eval.
	 */
	public void testEval() {
		ExpressionsManager manager = createManager();
		Instance target = new EmfInstance();
		target.setProperties(new HashMap<String, Serializable>());
		target.getProperties().put("test", "testValue");
		ExpressionContext context = manager.createDefaultContext(target, null, null);
		TypeConverter converter = createTypeConverter();

		ElExpression exp = ElExpressionParser.parse("${get([test])}");
		Serializable serializable = ElExpressionParser.eval(exp, manager, converter, context,
				ElExpressionParser.DEFAULT_EXPRESSION_ID);
		Assert.assertEquals("testValue", serializable);

		exp = ElExpressionParser.parse("${get([test2], otherValue)}");
		serializable = ElExpressionParser.eval(exp, manager, converter, context,
				ElExpressionParser.DEFAULT_EXPRESSION_ID);
		Assert.assertEquals("otherValue", serializable);

		exp = ElExpressionParser.parse("${get([localId], n/a)}");
		serializable = ElExpressionParser.eval(exp, manager, converter, context,
				ElExpressionParser.DEFAULT_EXPRESSION_ID);
		Assert.assertEquals("n/a", serializable);

		target.getProperties().put("localId", "testValue2");
		exp = ElExpressionParser.parse("${get([localId], n/a)}");
		serializable = ElExpressionParser.eval(exp, manager, converter, context,
				ElExpressionParser.DEFAULT_EXPRESSION_ID);
		// tests the context
		Assert.assertEquals("n/a", serializable);

		context = manager.createDefaultContext(target, null, null);
		exp = ElExpressionParser.parse("${get([localId], n/a)}");
		serializable = ElExpressionParser.eval(exp, manager, converter, context,
				ElExpressionParser.DEFAULT_EXPRESSION_ID);
		// tests the context
		Assert.assertEquals("testValue2", serializable);

		target.getProperties().remove("localId");
		context = manager.createDefaultContext(target, null, null);

		exp = ElExpressionParser.parse("${eval(${if(${get([test2])} == testValue).then(true).else(false)})}");
		serializable = ElExpressionParser.eval(exp, manager, converter, context,
				ElExpressionParser.DEFAULT_EXPRESSION_ID);
		Assert.assertEquals("false", serializable);

		exp = ElExpressionParser.parse("${eval(${if(${get([test])} == testValue).then(true).else(false)})}");
		serializable = ElExpressionParser.eval(exp, manager, converter, context,
				ElExpressionParser.DEFAULT_EXPRESSION_ID);
		Assert.assertEquals("true", serializable);

		String evaluate = manager.evaluateRule("${eval(${if(${get([test])} == testValue).then(true).else(false)})}",
				String.class, context);
		Assert.assertEquals("true", evaluate);

		evaluate = manager.evaluateRule("${eval(${if(${get([localId], n/a)} == testValue).then(true).else(false)})}",
				String.class, context);
		Assert.assertEquals("false", evaluate);

		evaluate = manager.evaluateRule(
				"${eval(<a href=\"\"><b>(\\${CL([type])}) ${get([localId], n/a)} ${get([title])}</b></a><br/> ${get([createdDate])}, ${get([creator])}, medium: ${get([mediumGeneralInfo])})}",
				String.class, context);
		Assert.assertNotNull(evaluate);

	}

	/**
	 * Test regex expression parsing.
	 */
	public void testRegexExpressionParsing() {
		ExpressionsManager manager = createManager();
		Instance target = new EmfInstance();
		target.setProperties(new HashMap<String, Serializable>());
		target.getProperties().put("emf:revisionType",
				new StringUriProxy("http://sirma.bg/ontologi/enterprise#latest"));
		ExpressionContext context = manager.createDefaultContext(target, null, null);
		String evaluate = manager.evaluateRule(
				"${eval(${if((${get([emf:revisionType])}).matches(.*?latest)).then(Latest)})}", String.class, context);
		Assert.assertNotNull(evaluate);
		Assert.assertEquals(evaluate, "Latest");
	}

	/**
	 * Test clear ws.
	 */
	public void testClearWS() {
		ExpressionsManager manager = createManager();
		String evaluate = manager.evaluate("${eval(<a>    test:   nul  fdf sdf  </a>)}", String.class);
		Assert.assertNotNull(evaluate);
		Assert.assertEquals(evaluate, "<a> test: nul fdf sdf </a>");

		manager = createManager();
		evaluate = manager.evaluate("${eval(<a>  </a>)}", String.class);
		Assert.assertNotNull(evaluate);
		Assert.assertEquals(evaluate, "<a></a>");
	}

	/**
	 * Test null clear.
	 */
	public void testNullClear_onlyNull() {
		ExpressionsManager manager = createManager();
		String evaluate = manager.evaluate("${eval(<a>null</a>)}", String.class);
		Assert.assertNotNull(evaluate);
		Assert.assertEquals(evaluate, "<a></a>");

		evaluate = manager.evaluate("${eval(<a null></a>)}", String.class);
		Assert.assertNotNull(evaluate);
		Assert.assertEquals(evaluate, "<a ></a>");
	}

	/**
	 * Test null clear.
	 */
	public void testNullClear_withOtherData_latin() {
		ExpressionsManager manager = createManager();
		String evaluate = manager.evaluate("${eval(<a>test: null</a>)}", String.class);
		Assert.assertNotNull(evaluate);
		Assert.assertEquals(evaluate, "<a>test </a>");
	}

	/**
	 * Test null clear.
	 */
	public void testNullClear_withOtherData_unicode() {
		ExpressionsManager manager = createManager();
		String evaluate = manager.evaluate("${eval(<a>Ревизия: null</a>)}", String.class);
		Assert.assertNotNull(evaluate);
		Assert.assertEquals(evaluate, "<a></a>");
	}

	public void testIsExpression() {
		assertTrue(ElExpressionParser.isExpression("${}"));
		assertTrue(ElExpressionParser.isExpression("#{}"));
		assertTrue(ElExpressionParser.isExpression("   ${}"));
		assertTrue(ElExpressionParser.isExpression(" \n${}"));

		assertFalse(ElExpressionParser.isExpression("{}"));
		assertFalse(ElExpressionParser.isExpression("  {}"));
		assertFalse(ElExpressionParser.isExpression(" \n{}"));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TypeConverter createTypeConverter() {
		TypeConverter typeConverter = super.createTypeConverter();
		UriConverterProvider provider = new UriConverterProvider();
		provider.register(typeConverter);
		return typeConverter;
	}

}
