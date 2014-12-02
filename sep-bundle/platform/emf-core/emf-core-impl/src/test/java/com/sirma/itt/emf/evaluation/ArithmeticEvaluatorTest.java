package com.sirma.itt.emf.evaluation;

import java.util.Collections;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.script.ScriptEvaluatorImpl;

/**
 * The Class ArithmeticEvaluatorTest.
 * 
 * @author BBonev
 */
@Test
public class ArithmeticEvaluatorTest extends BaseEvaluatorTest {

	/**
	 * Test arithmetic eval.
	 */
	public void testArithmeticEval() {
		ExpressionsManager manager = createManager();
		Integer evaluated = manager.evaluate("${=2 + 2 * 1/1 + (0*0)}", Integer.class);
		Assert.assertEquals(evaluated, Integer.valueOf(4));
	}

	/**
	 * Test arithmetic eval with null.
	 */
	public void testArithmeticEvalWithNull() {
		ExpressionsManager manager = createManager();
		Integer evaluated = manager.evaluate("${=2 + 2 * 1/1 + (0*null)}", Integer.class);
		Assert.assertEquals(evaluated, Integer.valueOf(4));
	}

	/**
	 * Test arithmetic eval format.
	 */
	public void testArithmeticEvalFormat() {
		ExpressionsManager manager = createManager();
		String evaluated = manager.evaluate("${=0/60}", String.class);
		Assert.assertEquals(evaluated, "0");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<ExpressionEvaluator> initializeEvaluators(ExpressionEvaluatorManager manager,
			TypeConverter converter) {
		List<ExpressionEvaluator> list = super.initializeEvaluators(manager, converter);
		ArithmeticEvaluator evaluator = new ArithmeticEvaluator();
		ScriptEvaluatorImpl scriptEvaluator = new ScriptEvaluatorImpl();
		ReflectionUtils.setField(scriptEvaluator, "scriptEngineName", "javascript");
		ReflectionUtils.setField(scriptEvaluator, "globalBindings", Collections.EMPTY_LIST);
		scriptEvaluator.initialize();
		ReflectionUtils.setField(evaluator, "scriptEvaluator", scriptEvaluator);
		list.add(initEvaluator(evaluator, manager, converter));
		return list;
	}
}
