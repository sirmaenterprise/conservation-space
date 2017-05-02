package com.sirma.itt.cmf.event.workflow;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;

/**
 * Tests for AfterTransitionEvent.
 *
 * @author Boyan Tonchev
 *
 */
@SuppressWarnings("static-method")
public class AfterTransitionEventTest {

	/**
	 * Test creation instance of class.
	 */
	@Test
	public void creationOfEventTest() {
		WorkflowInstanceContext context = Mockito.mock(WorkflowInstanceContext.class);
		TaskInstance instance = Mockito.mock(TaskInstance.class);
		String transitionId = "transitionId";
		AfterTransitionEvent event = new AfterTransitionEvent(context, instance, transitionId);

		Assert.assertEquals(event.getTransitionId(), transitionId);
		Assert.assertEquals(event.getContext(), context);
		Assert.assertEquals(event.getInstance(), instance);
	}
}
