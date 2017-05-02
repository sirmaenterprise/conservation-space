package com.sirma.itt.seip.expressions;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.seip.expressions.OrExpressionEvaluator;

/**
 * The Class OrExpressionEvaluatorTest.
 *
 * @author BBonev
 */
@Test
public class OrExpressionEvaluatorTest extends BaseEvaluatorTest {

	/**
	 * Test evaluation.
	 */
	public void testEvaluation() {
		OrExpressionEvaluator evaluator = new OrExpressionEvaluator();

		Assert.assertEquals(evaluator.evaluate("${or(true)}"), Boolean.TRUE);

		Assert.assertEquals(evaluator.evaluate("${or(true or false)}"), Boolean.TRUE);

		Assert.assertEquals(evaluator.evaluate("${or(true or true)}"), Boolean.TRUE);

		Assert.assertEquals(evaluator.evaluate("${or(true TRUE)}"), Boolean.TRUE);

		Assert.assertEquals(evaluator.evaluate("${or(true OR)}"), Boolean.TRUE);

		Assert.assertEquals(evaluator.evaluate("${or(true OR not false)}"), Boolean.TRUE);

		Assert.assertEquals(evaluator.evaluate("${or(false OR not false)}"), Boolean.TRUE);
	}
}
