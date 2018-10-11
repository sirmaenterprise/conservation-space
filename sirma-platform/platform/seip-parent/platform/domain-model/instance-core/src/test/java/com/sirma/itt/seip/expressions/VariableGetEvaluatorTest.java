package com.sirma.itt.seip.expressions;

import java.io.Serializable;
import java.util.HashMap;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.expressions.ExpressionContext;

/**
 * Test for get variable evaluator.
 *
 * @author BBonev
 * @author yasko
 */
@Test
public class VariableGetEvaluatorTest extends BaseEvaluatorTest {

	public void testVariable_spaces() {
		ExpressionsManager manager = createManager();
		String expression = "${eval(${var.var1 = \t\r\ntest}${var.var1})}";
		Instance target = new EmfInstance();
		target.setProperties(new HashMap<String, Serializable>());
		ExpressionContext context = manager.createDefaultContext(target, null, null);
		String result = manager.evaluateRule(expression, String.class, context);
		Assert.assertNotNull(result);
		Assert.assertEquals(result, "test");
	}

	@Test(expectedExceptions = EmfRuntimeException.class)
	public void testValueNotSet() {
		Assert.assertNotNull(new VariableGetEvaluator().evaluate("${var.var1}"));
	}
}
