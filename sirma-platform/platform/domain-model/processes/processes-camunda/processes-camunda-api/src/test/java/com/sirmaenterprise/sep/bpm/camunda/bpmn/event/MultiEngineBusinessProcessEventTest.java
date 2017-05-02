package com.sirmaenterprise.sep.bpm.camunda.bpmn.event;

import static com.sirmaenterprise.sep.bpm.camunda.MockProvider.DEFAULT_ENGINE;
import static com.sirmaenterprise.sep.bpm.camunda.MockProvider.mockActivity;
import static com.sirmaenterprise.sep.bpm.camunda.MockProvider.mockDelegateExecution;
import static com.sirmaenterprise.sep.bpm.camunda.MockProvider.mockDelegateTask;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.camunda.bpm.engine.cdi.BusinessProcessEventType;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.impl.bpmn.behavior.TaskActivityBehavior;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.junit.Test;
import org.mockito.Mockito;

import com.sirmaenterprise.sep.bpm.camunda.bpmn.event.MultiEngineBusinessProcessEvent.ActivityType;

/**
 * Tests the event wrapper {@link MultiEngineBusinessProcessEvent}
 * 
 * @author bbanchev
 */
public class MultiEngineBusinessProcessEventTest {

	@Test
	public void testMultiEngineBusinessProcessEventDelegateExecution() throws Exception {

		MultiEngineBusinessProcessEvent event = new MultiEngineBusinessProcessEvent(mockExecution(),
				mock(ProcessDefinitionEntity.class), BusinessProcessEventType.START_ACTIVITY);
		assertNotNull(event.getExecution());
		assertNotNull(event.getExecutionId());
		assertNotNull(event.getActivityType());
		assertEquals(ActivityType.TASK, event.getActivityType());

		assertEquals(DEFAULT_ENGINE, event.getEngineName());

	}

	@Test
	public void testMultiEngineBusinessProcessEventDelegateTask() throws Exception {
		MultiEngineBusinessProcessEvent event = new MultiEngineBusinessProcessEvent(mockTask(),
				mock(ProcessDefinitionEntity.class), BusinessProcessEventType.START_ACTIVITY);
		assertNotNull(event.getExecution());
		assertNotNull(event.getExecutionId());
		assertNotNull(event.getActivityType());
		assertEquals(ActivityType.TASK, event.getActivityType());

		assertEquals(DEFAULT_ENGINE, event.getEngineName());

	}

	private ActivityExecution mockExecution() {
		ActivityExecution delegateExecution = mockActivity(
				mockDelegateExecution(DEFAULT_ENGINE, ActivityExecution.class));
		PvmActivity activity = Mockito.mock(PvmActivity.class);
		ActivityBehavior behavior = Mockito.mock(TaskActivityBehavior.class);
		when(activity.getActivityBehavior()).thenReturn(behavior);
		when(delegateExecution.getActivity()).thenReturn(activity);
		return delegateExecution;
	}

	private DelegateTask mockTask() {
		ActivityExecution delegateExecution = mockExecution();
		DelegateTask delegateTask = mockDelegateTask(delegateExecution);
		return delegateTask;

	}

}
