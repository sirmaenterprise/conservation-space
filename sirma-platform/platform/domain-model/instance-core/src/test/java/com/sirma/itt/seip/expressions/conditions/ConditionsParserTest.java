package com.sirma.itt.seip.expressions.conditions;

import java.util.function.Predicate;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Test class for ConditionsParser.
 * 
 * @author Hristo Lungov
 */
@SuppressWarnings("static-method")
public class ConditionsParserTest {

	@Test
	public void simple_IN_false_Test() {
		Instance instance = Mockito.mock(Instance.class);
		Mockito.when(instance.getAsString("status")).thenReturn("INIT");
		Predicate<Instance> evaluate = ConditionsEvaluator.evaluate("[status] IN ('COMPLETED', 'STOPPED')");
		Assert.assertEquals(evaluate.test(instance), false);
	}

	@Test
	public void simple_IN_true_Test() {
		Instance instance = Mockito.mock(Instance.class);
		Mockito.when(instance.getAsString("status")).thenReturn("COMPLETED");
		Predicate<Instance> evaluate = ConditionsEvaluator.evaluate("[status] IN ('COMPLETED', 'STOPPED')");
		Assert.assertEquals(evaluate.test(instance), true);
	}

	@Test
	public void simple_NOTIN_true_Test() {
		Instance instance = Mockito.mock(Instance.class);
		Mockito.when(instance.getAsString("status")).thenReturn("INIT");
		Predicate<Instance> evaluate = ConditionsEvaluator.evaluate("[status] NOTIN ('COMPLETED', 'STOPPED')");
		Assert.assertEquals(evaluate.test(instance), true);
	}

	@Test
	public void simple_NOTIN_false_Test() {
		Instance instance = Mockito.mock(Instance.class);
		Mockito.when(instance.getAsString("status")).thenReturn("COMPLETED");
		Predicate<Instance> evaluate = ConditionsEvaluator.evaluate("[status] NOTIN ('COMPLETED', 'STOPPED')");
		Assert.assertEquals(evaluate.test(instance), false);
	}

	@Test
	public void simple_required_true_Test() {
		Instance instance = Mockito.mock(Instance.class);
		Mockito.when(instance.getAsString("status")).thenReturn("COMPLETED");
		Predicate<Instance> evaluate = ConditionsEvaluator.evaluate("+[status]");
		Assert.assertEquals(evaluate.test(instance), true);

		evaluate = ConditionsEvaluator.evaluate("[status]");
		Assert.assertEquals(evaluate.test(instance), true);
	}

	@Test
	public void simple_notrequired_true_Test() {
		Instance instance = Mockito.mock(Instance.class);
		Mockito.when(instance.getAsString("status")).thenReturn("").thenReturn(null);
		Predicate<Instance> evaluate = ConditionsEvaluator.evaluate("-[status]");
		Assert.assertEquals(evaluate.test(instance), true);

		evaluate = ConditionsEvaluator.evaluate("-[status]");
		Assert.assertEquals(evaluate.test(instance), true);
	}

	@Test
	public void simple_chain_AND_true_Test() {
		Instance instance = Mockito.mock(Instance.class);
		Mockito.when(instance.getAsString("status")).thenReturn("COMPLETED");
		Mockito.when(instance.getAsString("title")).thenReturn("Test Title");
		Mockito.when(instance.getAsString("test")).thenReturn("").thenReturn("Test");
		Predicate<Instance> evaluate = ConditionsEvaluator.evaluate("[title] AND [status] AND -[test]");
		Assert.assertEquals(evaluate.test(instance), true);
		Assert.assertEquals(evaluate.test(instance), false);
	}

	@Test
	public void simple_chain_AND_false_Test() {
		Instance instance = Mockito.mock(Instance.class);
		Mockito.when(instance.getAsString("status")).thenReturn("COMPLETED");
		Mockito.when(instance.getAsString("title")).thenReturn("Test Title");
		Mockito.when(instance.getAsString("test")).thenReturn("Test");
		Predicate<Instance> evaluate = ConditionsEvaluator.evaluate("[title] AND [status] AND -[test]");
		Assert.assertEquals(evaluate.test(instance), false);
	}

	@Test
	public void simple_chain_OR_Test() {
		Instance instance = Mockito.mock(Instance.class);
		Mockito.when(instance.getAsString("status")).thenReturn("COMPLETED");
		Mockito.when(instance.getAsString("title")).thenReturn("Test Title");
		Mockito.when(instance.getAsString("test")).thenReturn("").thenReturn("Test");
		Predicate<Instance> evaluate = ConditionsEvaluator.evaluate("[title] AND [status] OR -[test]");
		Assert.assertEquals(evaluate.test(instance), true);
		Assert.assertEquals(evaluate.test(instance), true);
	}

	@Test
	public void invalid_expression() {
		Instance instance = Mockito.mock(Instance.class);
		Mockito.when(instance.getAsString("status")).thenReturn("COMPLETED");
		Mockito.when(instance.getAsString("title")).thenReturn("Test Title");
		Mockito.when(instance.getAsString("test")).thenReturn("").thenReturn("Test");
		Predicate<Instance> evaluate = ConditionsEvaluator.evaluate("[title] AND [status] OR asdad");
		Assert.assertEquals(evaluate.test(instance), true);
	}

}