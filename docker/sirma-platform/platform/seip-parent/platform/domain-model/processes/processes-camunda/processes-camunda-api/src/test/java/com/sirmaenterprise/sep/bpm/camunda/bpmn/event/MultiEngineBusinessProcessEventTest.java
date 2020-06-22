package com.sirmaenterprise.sep.bpm.camunda.bpmn.event;

import static com.sirmaenterprise.sep.bpm.camunda.MockProvider.DEFAULT_ENGINE;
import static com.sirmaenterprise.sep.bpm.camunda.MockProvider.mockDelegateExecution;
import static com.sirmaenterprise.sep.bpm.camunda.MockProvider.mockDelegateTask;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.camunda.bpm.engine.cdi.BusinessProcessEventType;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.impl.bpmn.behavior.GatewayActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.IntermediateCatchEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.MailActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.NoneEndEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.NoneStartEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.ParallelMultiInstanceActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.SequentialMultiInstanceActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.SubProcessActivityBehavior;
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
		ActivityBehavior behavior = Mockito.mock(TaskActivityBehavior.class);
		MultiEngineBusinessProcessEvent event = new MultiEngineBusinessProcessEvent(mockExecution(behavior),
				mock(ProcessDefinitionEntity.class), BusinessProcessEventType.START_ACTIVITY);
		assertNotNull(event.getExecution());
		assertNotNull(event.getExecutionId());
		assertNotNull(event.getActivityType());
		assertEquals(ActivityType.TASK, event.getActivityType());

		assertEquals(DEFAULT_ENGINE, event.getEngineName());
		assertEquals("testProcessId", event.getProcessBusinessKey());
		assertTrue(event.toString().startsWith("Event 'null' ['start', 'TASK',"));
		assertNull(event.getLocalVariableScope());
	}

	@Test
	public void should_match_ParallelMultiInstanceActivityBehavior() throws Exception {
		ActivityBehavior behavior = Mockito.mock(ParallelMultiInstanceActivityBehavior.class);
		MultiEngineBusinessProcessEvent event = new MultiEngineBusinessProcessEvent(mockExecution(behavior),
																					mock(ProcessDefinitionEntity.class), BusinessProcessEventType.START_ACTIVITY);
		assertNotNull(event.getExecution());
		assertNotNull(event.getExecutionId());
		assertNotNull(event.getActivityType());
		assertEquals(ActivityType.TASK, event.getActivityType());

		assertEquals(DEFAULT_ENGINE, event.getEngineName());
	}

	@Test
	public void should_match_SequentialMultiInstanceActivityBehavior() throws Exception {
		ActivityBehavior behavior = Mockito.mock(SequentialMultiInstanceActivityBehavior.class);
		MultiEngineBusinessProcessEvent event = new MultiEngineBusinessProcessEvent(mockExecution(behavior),
																					mock(ProcessDefinitionEntity.class), BusinessProcessEventType.START_ACTIVITY);
		assertNotNull(event.getExecution());
		assertNotNull(event.getExecutionId());
		assertNotNull(event.getActivityType());
		assertEquals(ActivityType.TASK, event.getActivityType());

		assertEquals(DEFAULT_ENGINE, event.getEngineName());
	}

	@Test
	public void should_match_NoneStartEventActivityBehavior() throws Exception {
		ActivityBehavior noneStartEventActivityBehavior = Mockito.mock(NoneStartEventActivityBehavior.class);
		MultiEngineBusinessProcessEvent event = new MultiEngineBusinessProcessEvent(mockExecution(noneStartEventActivityBehavior),
																					mock(ProcessDefinitionEntity.class), BusinessProcessEventType.START_ACTIVITY);
		assertNotNull(event.getExecution());
		assertNotNull(event.getExecutionId());
		assertNotNull(event.getActivityType());
		assertEquals(ActivityType.EVENT, event.getActivityType());

		assertEquals(DEFAULT_ENGINE, event.getEngineName());
	}

	@Test
	public void should_match_NoneEndEventActivityBehavior() throws Exception {
		ActivityBehavior noneEndEventActivityBehavior = Mockito.mock(NoneEndEventActivityBehavior.class);
		MultiEngineBusinessProcessEvent event = new MultiEngineBusinessProcessEvent(mockExecution(noneEndEventActivityBehavior),
																					mock(ProcessDefinitionEntity.class), BusinessProcessEventType.START_ACTIVITY);
		assertNotNull(event.getExecution());
		assertNotNull(event.getExecutionId());
		assertNotNull(event.getActivityType());
		assertEquals(ActivityType.EVENT, event.getActivityType());

		assertEquals(DEFAULT_ENGINE, event.getEngineName());
	}

	@Test
	public void should_match_IntermediateCatchEventActivityBehavior() throws Exception {
		ActivityBehavior intermediateCatchEventActivityBehavior = Mockito.mock(IntermediateCatchEventActivityBehavior.class);
		MultiEngineBusinessProcessEvent event = new MultiEngineBusinessProcessEvent(mockExecution(intermediateCatchEventActivityBehavior),
																					mock(ProcessDefinitionEntity.class), BusinessProcessEventType.START_ACTIVITY);
		assertNotNull(event.getExecution());
		assertNotNull(event.getExecutionId());
		assertNotNull(event.getActivityType());
		assertEquals(ActivityType.EVENT, event.getActivityType());

		assertEquals(DEFAULT_ENGINE, event.getEngineName());
	}

	@Test
	public void should_match_SubProcessActivityBehavior() throws Exception {
		ActivityBehavior subProcessActivityBehavior = Mockito.mock(SubProcessActivityBehavior.class);
		MultiEngineBusinessProcessEvent event = new MultiEngineBusinessProcessEvent(mockExecution(subProcessActivityBehavior),
																					mock(ProcessDefinitionEntity.class), BusinessProcessEventType.START_ACTIVITY);
		assertNotNull(event.getExecution());
		assertNotNull(event.getExecutionId());
		assertNotNull(event.getActivityType());
		assertEquals(ActivityType.SUBPROCESS, event.getActivityType());

		assertEquals(DEFAULT_ENGINE, event.getEngineName());
	}

	@Test
	public void should_match_GatewayActivityBehavior() throws Exception {
		ActivityBehavior gatewayActivityBehavior = Mockito.mock(GatewayActivityBehavior.class);
		MultiEngineBusinessProcessEvent event = new MultiEngineBusinessProcessEvent(mockExecution(gatewayActivityBehavior),
																					mock(ProcessDefinitionEntity.class), BusinessProcessEventType.START_ACTIVITY);
		assertNotNull(event.getExecution());
		assertNotNull(event.getExecutionId());
		assertNotNull(event.getActivityType());
		assertEquals(ActivityType.GATEWAY, event.getActivityType());

		assertEquals(DEFAULT_ENGINE, event.getEngineName());
	}

	@Test
	public void should_not_match_MailActivityBehavior() throws Exception {
		ActivityBehavior mailActivityBehavior = Mockito.mock(MailActivityBehavior.class);
		MultiEngineBusinessProcessEvent event = new MultiEngineBusinessProcessEvent(mockExecution(mailActivityBehavior),
																					mock(ProcessDefinitionEntity.class), BusinessProcessEventType.START_ACTIVITY);
		assertNotNull(event.getExecution());
		assertNotNull(event.getExecutionId());
		assertNull(event.getActivityType());

		assertEquals(DEFAULT_ENGINE, event.getEngineName());
	}

	@Test
	public void should_not_find_activityType_missing_activity_behaviour() throws Exception {

		MultiEngineBusinessProcessEvent event = new MultiEngineBusinessProcessEvent(mockExecution(null),
																					mock(ProcessDefinitionEntity.class), BusinessProcessEventType.START_ACTIVITY);
		assertNotNull(event.getExecution());
		assertNotNull(event.getExecutionId());
		assertNull(event.getActivityType());

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

	private ActivityExecution mockExecution(ActivityBehavior activityBehavior) {
		ActivityExecution delegateExecution = mockDelegateExecution(DEFAULT_ENGINE, ActivityExecution.class);
		when(delegateExecution.getProcessBusinessKey()).thenReturn("testProcessId");
		PvmActivity activity = Mockito.mock(PvmActivity.class);
		when(activity.getActivityBehavior()).thenReturn(activityBehavior);
		when(delegateExecution.getActivity()).thenReturn(activity);
		return delegateExecution;
	}

	private DelegateTask mockTask() {
		ActivityBehavior behavior = Mockito.mock(TaskActivityBehavior.class);
		ActivityExecution delegateExecution = mockExecution(behavior);
		DelegateTask delegateTask = mockDelegateTask(delegateExecution);
		return delegateTask;
	}

}
