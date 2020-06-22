package com.sirma.itt.seip.expressions;

import java.io.Serializable;
import java.util.HashMap;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.expressions.ExpressionContext;

@Test
public class VariableSetEvaluatorTest extends BaseEvaluatorTest {

	public void testSetVariable() {
		ExpressionsManager manager = createManager();
		String expression = "${eval(${var.var1 = \t\r\ntest})}";

		Instance target = new EmfInstance();
		target.setProperties(new HashMap<String, Serializable>());
		ExpressionContext context = manager.createDefaultContext(target, null, null);
		manager.evaluateRule(expression, String.class, context);

		String result = context.getIfSameType("var_var1", String.class);
		Assert.assertNotNull(result);
		Assert.assertEquals(result, "test");
	}
}
