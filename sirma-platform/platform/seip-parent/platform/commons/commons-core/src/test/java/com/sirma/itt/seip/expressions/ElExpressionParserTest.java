package com.sirma.itt.seip.expressions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ElExpressionParserTest {

	@Test
	public void testParsing() {
		ElExpression expression = ElExpressionParser.parse("${eval(${get([test])})}");
		assertEquals("{0}", expression.getExpression());
		assertEquals(1, expression.getSubExpressions().size());
		assertEquals("$'{'eval({0})'}'", expression.getSubExpressions().get(0).getExpression());
		assertEquals("eval", expression.getSubExpressions().get(0).getExpressionId());
		assertEquals("$'{'get([test])'}'",
				expression.getSubExpressions().get(0).getSubExpressions().get(0).getExpression());
		assertEquals("get", expression.getSubExpressions().get(0).getSubExpressions().get(0).getExpressionId());

		expression = ElExpressionParser.parse("${get([test])}");
		assertEquals("{0}", expression.getExpression());
		assertEquals(null, expression.getExpressionId());
		assertEquals(1, expression.getSubExpressions().size());
		assertEquals("$'{'get([test])'}'", expression.getSubExpressions().get(0).getExpression());
		assertEquals("get", expression.getSubExpressions().get(0).getExpressionId());

		expression = ElExpressionParser.parse("${eval(${get([test], defaultTest)} and ${today})}");
		assertEquals("{0}", expression.getExpression());
		assertEquals(1, expression.getSubExpressions().size());
		assertEquals("$'{'eval({0} and {1})'}'", expression.getSubExpressions().get(0).getExpression());
		assertEquals(2, expression.getSubExpressions().get(0).getSubExpressions().size());
		assertEquals("$'{'get([test], defaultTest)'}'",
				expression.getSubExpressions().get(0).getSubExpressions().get(0).getExpression());
		assertEquals("$'{'today'}'", expression.getSubExpressions().get(0).getSubExpressions().get(1).getExpression());
		assertEquals("today", expression.getSubExpressions().get(0).getSubExpressions().get(1).getExpressionId());

		expression = ElExpressionParser.parse("${eval(${get([test], ${today(-7)})} and ${today})}");
		assertEquals("{0}", expression.getExpression());
		assertEquals(1, expression.getSubExpressions().size());
		assertEquals("$'{'eval({0} and {1})'}'", expression.getSubExpressions().get(0).getExpression());
		assertEquals(2, expression.getSubExpressions().get(0).getSubExpressions().size());
		assertEquals("$'{'get([test], {0})'}'",
				expression.getSubExpressions().get(0).getSubExpressions().get(0).getExpression());
		assertEquals("$'{'today(-7)'}'",
				expression
						.getSubExpressions()
							.get(0)
							.getSubExpressions()
							.get(0)
							.getSubExpressions()
							.get(0)
							.getExpression());
		assertEquals("$'{'today'}'", expression.getSubExpressions().get(0).getSubExpressions().get(1).getExpression());

		expression = ElExpressionParser.parse("${eval(${if(${get([test2])} == testValue).then(true).else(false)})}");
		assertEquals("{0}", expression.getExpression());
		assertEquals(1, expression.getSubExpressions().size());
		assertEquals("$'{'eval({0})'}'", expression.getSubExpressions().get(0).getExpression());
		expression = expression.getSubExpressions().get(0).getSubExpressions().get(0);
		assertEquals("$'{'if({0} == testValue).then(true).else(false)'}'", expression.getExpression());
		assertEquals("if", expression.getExpressionId());
		assertEquals(1, expression.getSubExpressions().size());
		assertEquals("$'{'get([test2])'}'", expression.getSubExpressions().get(0).getExpression());

	}

	/**
	 * Test lazy expression parsing.
	 */
	public void testLazyExpressionParsing() {
		ElExpression expression = ElExpressionParser.parse("#{eval(#{get([test])})}",
				ElExpressionParser.LAZY_EXPRESSION_ID);
		assertNotNull(expression);
		assertEquals("{0}", expression.getExpression());
		assertEquals(1, expression.getSubExpressions().size());
		assertEquals("#'{'eval({0})'}'", expression.getSubExpressions().get(0).getExpression());
		assertEquals("#'{'get([test])'}'",
				expression.getSubExpressions().get(0).getSubExpressions().get(0).getExpression());
	}

	@Test
	public void testMixedExpressionParsing() {
		ElExpression expression = ElExpressionParser.parse("${eval(#{get([test])})}");
		assertNotNull(expression);
		assertEquals("{0}", expression.getExpression());
		assertEquals(1, expression.getSubExpressions().size());
		assertEquals("$'{'eval(#'{'get([test])'}')'}'", expression.getSubExpressions().get(0).getExpression());

		expression = ElExpressionParser.parse(
				"${eval(Some text #{get([test])} some other text ${get([test])} and ${if(2==2).then(#{get[test2]})})}");
		assertNotNull(expression);
		assertEquals("{0}", expression.getExpression());
		assertEquals(1, expression.getSubExpressions().size());
		assertEquals("$'{'eval(Some text #'{'get([test])'}' some other text {0} and {1})'}'",
				expression.getSubExpressions().get(0).getExpression());
		assertEquals(2, expression.getSubExpressions().get(0).getSubExpressions().size());
		assertEquals("$'{'if(2==2).then(#'{'get[test2]'}')'}'",
				expression.getSubExpressions().get(0).getSubExpressions().get(1).getExpression());
	}

	@Test
	public void testIsExpression() {
		assertTrue(ElExpressionParser.isExpression("${}"));
		assertTrue(ElExpressionParser.isExpression("#{}"));
		assertTrue(ElExpressionParser.isExpression("   ${}"));
		assertTrue(ElExpressionParser.isExpression(" \n${}"));

		assertFalse(ElExpressionParser.isExpression("{}"));
		assertFalse(ElExpressionParser.isExpression("  {}"));
		assertFalse(ElExpressionParser.isExpression(" \n{}"));
	}

}
