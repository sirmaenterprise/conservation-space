package com.sirmaenterprise.sep.bpm.camunda.service.event;

import static com.sirmaenterprise.sep.bpm.camunda.MockProvider.DEFAULT_ENGINE;
import static com.sirmaenterprise.sep.bpm.camunda.MockProvider.mockActivity;
import static com.sirmaenterprise.sep.bpm.camunda.MockProvider.mockDelegateExecution;
import static com.sirmaenterprise.sep.bpm.camunda.MockProvider.mockDelegateTask;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.camunda.bpm.engine.cdi.BusinessProcessEvent;
import org.camunda.bpm.engine.cdi.BusinessProcessEventType;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationRuntimeException;
import com.sirmaenterprise.sep.bpm.camunda.service.BPMSecurityService;

/**
 * @author bbanchev
 */
@RunWith(MockitoJUnitRunner.class)
public class MultiEngineEventListenerTest {
	@Mock
	private BPMSecurityService bpmSecurityService;
	@InjectMocks
	private MultiEngineEventListener multiEngineEventListener;


	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.service.event.MultiEngineEventListener#createEvent(org.camunda.bpm.engine.delegate.DelegateExecution)}.
	 */
	@Test
	public void testCreateEventDelegateExecution() throws Exception {
		ExecutionEntity execution = mockDelegateExecution(DEFAULT_ENGINE, ExecutionEntity.class);
		ProcessDefinitionEntity processDefinition = mock(ProcessDefinitionEntity.class);
		when(processDefinition.getId()).thenReturn("processDefId");
		when(execution.getProcessDefinition()).thenReturn(processDefinition);
		when(execution.getProcessDefinitionId()).thenReturn("processDefId");
		when(execution.getEventName()).thenReturn(ExecutionListener.EVENTNAME_START);
		Context.setExecutionContext(execution);
		mockActivity(execution);

		BusinessProcessEvent event = multiEngineEventListener.createEvent(execution);

		assertNotNull(event);
		assertNotNull(event.getProcessDefinition());
		assertNotNull(event.getExecutionId());
		assertEquals(BusinessProcessEventType.START_ACTIVITY, event.getType());
		assertEquals(processDefinition, event.getProcessDefinition());

	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.service.event.MultiEngineEventListener#createEvent(org.camunda.bpm.engine.delegate.DelegateExecution)}.
	 */
	@Test(expected = CamundaIntegrationRuntimeException.class)
	public void testCreateEventDelegateExecutionWithError() throws Exception {
		ExecutionEntity execution = mockDelegateExecution(DEFAULT_ENGINE, ExecutionEntity.class);
		ProcessDefinitionEntity processDefinition = Mockito.mock(ProcessDefinitionEntity.class);
		when(processDefinition.getId()).thenReturn("processDefId");
		when(execution.getProcessDefinition()).thenReturn(processDefinition);
		when(execution.getProcessDefinitionId()).thenReturn("processDefId");
		when(execution.getEventName()).thenReturn("wrongEvent");

		Context.setExecutionContext(execution);
		mockActivity(execution);
		multiEngineEventListener.createEvent(execution);
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.service.event.MultiEngineEventListener#notify(org.camunda.bpm.engine.delegate.DelegateTask)}.
	 */
	@Test
	public void testNotifyDelegateTask() throws Exception {
		ExecutionEntity execution = mockDelegateExecution("camunda_engine2", ExecutionEntity.class);
		DelegateTask delegateTask = mockDelegateTask(execution);
		multiEngineEventListener.notify(delegateTask);
		verify(bpmSecurityService).executeEngineOperation(eq("camunda_engine2"), any());
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.service.event.MultiEngineEventListener#createEvent(org.camunda.bpm.engine.delegate.DelegateTask)}.
	 */
	@Test
	public void testCreateEventDelegateTask() throws Exception {
		ExecutionEntity execution = mockDelegateExecution(DEFAULT_ENGINE, ExecutionEntity.class);
		DelegateTask delegateTask = mockDelegateTask(execution);

		ProcessDefinitionEntity processDefinition = Mockito.mock(ProcessDefinitionEntity.class);
		when(processDefinition.getId()).thenReturn("processDefId");
		when(execution.getProcessDefinition()).thenReturn(processDefinition);
		when(execution.getProcessDefinitionId()).thenReturn("processDefId");
		when(delegateTask.getEventName()).thenReturn(TaskListener.EVENTNAME_CREATE);
		Context.setExecutionContext(execution);
		mockActivity(execution);

		BusinessProcessEvent event = multiEngineEventListener.createEvent(delegateTask);

		assertNotNull(event);
		assertNotNull(event.getProcessDefinition());
		assertNotNull(event.getTaskId());
		assertEquals(BusinessProcessEventType.CREATE_TASK, event.getType());
		assertEquals(processDefinition, event.getProcessDefinition());

	}
}
