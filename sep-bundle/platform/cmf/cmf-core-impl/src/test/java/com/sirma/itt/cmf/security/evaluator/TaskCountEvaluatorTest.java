package com.sirma.itt.cmf.security.evaluator;

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.evaluators.TaskCountEvaluator;
import com.sirma.itt.cmf.services.TaskService;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.evaluation.BaseEvaluatorTest;
import com.sirma.itt.emf.evaluation.ExpressionContext;
import com.sirma.itt.emf.evaluation.ExpressionEvaluator;
import com.sirma.itt.emf.evaluation.ExpressionEvaluatorManager;
import com.sirma.itt.emf.evaluation.ExpressionsManager;
import com.sirma.itt.emf.instance.model.EmfInstance;
import com.sirma.itt.emf.instance.model.Instance;

/**
 * The Class TaskCountEvaluatorTest.
 *
 * @author BBonev
 */
@Test
public class TaskCountEvaluatorTest extends BaseEvaluatorTest {

	/** The task service. */
	private TaskService taskService;

	/**
	 * Test counting non workflow instance.
	 */
	public void testCountingNonWorkflowInstance() {
		ExpressionsManager expressionsManager = createManager();
		Instance target = new EmfInstance();
		target.setId("emf:id");
		ExpressionContext context = expressionsManager.createDefaultContext(target, null, null);

		Integer count = expressionsManager.evaluateRule("${taskCount}", Integer.class, context);
		Assert.assertEquals(count, Integer.valueOf(0));

		when(
taskService.getContextTasks(target, true)).thenReturn(
				new HashSet<String>(Arrays.asList("1")));
		// test default behavior of expression
		count = expressionsManager.evaluateRule("${taskCount}", Integer.class, context);
		Assert.assertEquals(count, Integer.valueOf(1));
		// verify calling the service with correct arguments
		verify(taskService, atLeast(1)).getContextTasks(target, true);

		// test for active
		count = expressionsManager.evaluateRule("${taskCount(active)}", Integer.class, context);
		Assert.assertEquals(count, Integer.valueOf(1));
		verify(taskService, atLeast(2)).getContextTasks(target, true);

		// test for inactive
		count = expressionsManager.evaluateRule("${taskCount(inactive)}", Integer.class, context);
		Assert.assertEquals(count, Integer.valueOf(0));
		verify(taskService, atLeast(1)).getContextTasks(target, false);

		when(
taskService.getContextTasks(target, false)).thenReturn(
				new LinkedHashSet<String>(Arrays.asList("1", "2")));
		count = expressionsManager.evaluateRule("${taskCount(inactive)}", Integer.class, context);
		Assert.assertEquals(count, Integer.valueOf(2));
		verify(taskService, atLeast(2)).getContextTasks(target, false);
	}

	/**
	 * Test counting workflow instance.
	 */
	public void testCountingWorkflowInstance() {
		ExpressionsManager expressionsManager = createManager();
		Instance target = new WorkflowInstanceContext();
		target.setId("emf:workflow");
		ExpressionContext context = expressionsManager.createDefaultContext(target, null, null);

		Integer count = expressionsManager.evaluateRule("${taskCount}", Integer.class, context);
		Assert.assertEquals(count, Integer.valueOf(0));

		when(taskService.getContextTasks(target, true)).thenReturn(
				new HashSet<String>(Arrays.asList("1")));
		count = expressionsManager.evaluateRule("${taskCount}", Integer.class, context);
		Assert.assertEquals(count, Integer.valueOf(1));
		verify(taskService, atLeast(1)).getContextTasks(target, true);

		count = expressionsManager.evaluateRule("${taskCount(active)}", Integer.class, context);
		Assert.assertEquals(count, Integer.valueOf(1));
		verify(taskService, atLeast(2)).getContextTasks(target, true);

		count = expressionsManager.evaluateRule("${taskCount(inactive)}", Integer.class, context);
		Assert.assertEquals(count, Integer.valueOf(0));
		verify(taskService, atLeast(1)).getContextTasks(target, false);

		when(taskService.getContextTasks(target, false)).thenReturn(
				new LinkedHashSet<String>(Arrays.asList("1", "2")));
		count = expressionsManager.evaluateRule("${taskCount(inactive)}", Integer.class, context);
		Assert.assertEquals(count, Integer.valueOf(2));
		verify(taskService, atLeast(2)).getContextTasks(target, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<ExpressionEvaluator> initializeEvaluators(ExpressionEvaluatorManager manager,
			TypeConverter converter) {
		List<ExpressionEvaluator> list = super.initializeEvaluators(manager, converter);
		ExpressionEvaluator evaluator = initEvaluator(new TaskCountEvaluator(), manager, converter);
		list.add(evaluator);
		taskService = mock(TaskService.class);
		ReflectionUtils.setField(evaluator, "taskService", taskService);
		return list;
	}

}
