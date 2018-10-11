package com.sirma.itt.seip.expressions;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.mockito.Mock;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.expressions.ExpressionEvaluator;
import com.sirma.itt.seip.script.ScriptEvaluator;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * The Class ArithmeticEvaluatorTest.
 *
 * @author BBonev
 */
@Test
public class ArithmeticEvaluatorTest extends BaseEvaluatorTest {

	@Mock
	private ScriptEvaluator scriptEvaluator;

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
	protected List<ExpressionEvaluator> initializeEvaluators(ExpressionsManager manager,
			TypeConverter converter) {
		List<ExpressionEvaluator> list = super.initializeEvaluators(manager, converter);
		ArithmeticEvaluator evaluator = new ArithmeticEvaluator();
		ReflectionUtils.setFieldValue(evaluator, "scriptEvaluator", scriptEvaluator);
		when(scriptEvaluator.eval(anyString(), eq(null))).then(a -> {
			String script = a.getArgumentAt(0, String.class);
			ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
			ScriptEngine engine = scriptEngineManager.getEngineByName("javascript");
			return engine.eval(script);
		});
		list.add(initEvaluator(evaluator, manager, converter));
		return list;
	}
}
