package com.sirma.itt.emf.evaluation;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * The Class ConditionalExpressionEvaluatorTest.
 * 
 * @author BBonev
 */
public class ConditionalExpressionEvaluatorTest {

	/**
	 * Test evaluation.
	 */
	@Test
	public void testEvaluation() {
		ConditionalExpressionEvaluator evaluator = new ConditionalExpressionEvaluator();
		String expression = "3==5";
		Assert.assertEquals(evaluator.evaluate("${if(" + expression + ").then(then).else(else)}"),
				"else");
		expression = "3==3";
		Assert.assertEquals(evaluator.evaluate("${if(" + expression + ").then(then).else(else)}"),
				"then");
		expression = "3<5";
		Assert.assertEquals(evaluator.evaluate("${if(" + expression + ").then(then).else(else)}"),
				"then");
		expression = "3>5";
		Assert.assertEquals(evaluator.evaluate("${if(" + expression + ").then(then).else(else)}"),
				"else");
		expression = "3<=3";
		Assert.assertEquals(evaluator.evaluate("${if(" + expression + ").then(then).else(else)}"),
				"then");
		expression = "3<=5";
		Assert.assertEquals(evaluator.evaluate("${if(" + expression + ").then(then).else(else)}"),
				"then");
		expression = "3>=5";
		Assert.assertEquals(evaluator.evaluate("${if(" + expression + ").then(then).else(else)}"),
				"else");
		expression = "3>=3";
		Assert.assertEquals(evaluator.evaluate("${if(" + expression + ").then(then).else(else)}"),
				"then");

		expression = "test==";
		Assert.assertEquals(evaluator.evaluate("${if(" + expression + ").then(then).else(else)}"),
				"else");
		expression = "  == ";
		Assert.assertEquals(evaluator.evaluate("${if(" + expression + ").then(then).else(else)}"),
				"then");
		expression = "==";
		Assert.assertEquals(evaluator.evaluate("${if(" + expression + ").then(then).else(else)}"),
				"then");

		Assert.assertEquals(evaluator.evaluate("${if(true).then(then).else(else)}"), "then");
		Assert.assertEquals(evaluator.evaluate("${if(false).then(then).else(else)}"), "else");

		Assert.assertEquals(
				evaluator.evaluate("${if((null).matches(test)).then(then).else(else)}"), "else");
		Assert.assertEquals(evaluator.evaluate("${if((null).matches(test)).then(then)}"), "");

		Assert.assertEquals(evaluator.evaluate("${if(().matches(\\s*)).then(then).else(else)}"),
				"then");
		Assert.assertEquals(
				evaluator.evaluate("${if((test).matches(\\s*)).then(then).else(else)}"), "else");

		Assert.assertEquals(
				evaluator
						.evaluate("${if((test).matches(\\s*)).then(then\nsdsd  \n).else(else\non second line)}"),
				"else\non second line");
	}
}
