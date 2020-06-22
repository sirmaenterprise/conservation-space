package com.sirmaenterprise.sep.bpm.camunda.bpmn.observers;

import static com.sirmaenterprise.sep.bpm.camunda.MockProvider.DEFAULT_ENGINE;
import static com.sirmaenterprise.sep.bpm.camunda.MockProvider.mockActivity;
import static com.sirmaenterprise.sep.bpm.camunda.MockProvider.mockDelegateExecution;
import static com.sirmaenterprise.sep.bpm.camunda.MockProvider.mockDelegateTask;
import static com.sirmaenterprise.sep.bpm.camunda.properties.BPMTaskProperties.TASK_ID;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.camunda.bpm.engine.cdi.BusinessProcessEventType;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.bpmn.behavior.IntermediateCatchEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.NoneEndEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.NoneStartEventActivityBehavior;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.bpm.model.bpmn.impl.BpmnModelInstanceImpl;
import org.camunda.bpm.model.bpmn.impl.BpmnParser;
import org.camunda.bpm.model.bpmn.instance.CatchEvent;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.Signal;
import org.camunda.bpm.model.bpmn.instance.SignalEventDefinition;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.internal.verification.Times;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.event.EmfEvent;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.actions.Actions;
import com.sirma.itt.seip.instance.actions.save.SaveRequest;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.itt.seip.testutil.mocks.InstanceContextServiceMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirmaenterprise.sep.bpm.camunda.bpmn.event.MultiEngineBusinessProcessEvent;
import com.sirmaenterprise.sep.bpm.camunda.bpmn.event.MultiEngineBusinessProcessEvent.ActivityType;
import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationRuntimeException;
import com.sirmaenterprise.sep.bpm.camunda.schedules.BPMEventScheduler;
import com.sirmaenterprise.sep.bpm.camunda.service.BPMPropertiesConverter;
import com.sirmaenterprise.sep.bpm.camunda.transitions.states.SequenceFlowParserTest;

/**
 * Test for {@link BPMDomainObserver}.
 *
 * @author bbanchev
 */
public class BPMDomainObserverTest {

	@InjectMocks
	private BPMDomainObserver bPMDomainObserver;

	@Mock
	private DomainInstanceService domainInstanceService;
	@Mock
	private InstanceTypeResolver instanceResolver;
	@Mock
	private BPMPropertiesConverter propertiesConverter;
	@Mock
	private BPMEventScheduler bpmEventScheduler;
	@Mock
	private SchedulerService schedulerService;
	@Mock
	private Actions actions;
	@Mock
	private DefinitionService definitionService;
	@Spy
	private InstanceContextServiceMock contextService;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	private static MultiEngineBusinessProcessEvent mockEvent(BusinessProcessEventType eventType, boolean taskDelegate) {
		ExecutionEntity execution = mockDelegateExecution(DEFAULT_ENGINE, ExecutionEntity.class);
		when(execution.getCurrentActivityId()).thenReturn("TASKST100");
		DelegateTask delegateTask = mockDelegateTask(execution);

		ProcessDefinitionEntity processDefinition = Mockito.mock(ProcessDefinitionEntity.class);
		when(execution.getProcessDefinition()).thenReturn(processDefinition);
		when(execution.getProcessDefinitionId()).thenReturn("processDefId");
		when(execution.getProcessBusinessKey()).thenReturn("processId");
		when(processDefinition.getId()).thenReturn("processDefId");
		when(delegateTask.getTaskDefinitionKey()).thenReturn("processDefId");
		when(delegateTask.getEventName()).thenReturn(TaskListener.EVENTNAME_CREATE);
		when(delegateTask.getId()).thenReturn("taskId");
		mockActivity(execution);
		Context.setExecutionContext(execution);
		if (taskDelegate) {
			return new MultiEngineBusinessProcessEvent(delegateTask, processDefinition, eventType);
		}
		return new MultiEngineBusinessProcessEvent(execution, processDefinition, eventType);
	}

	@Test
	public void workflowEndEventTest() {
		MultiEngineBusinessProcessEvent event = mock(MultiEngineBusinessProcessEvent.class);
		when(event.getActivityType()).thenReturn(ActivityType.EVENT);
		when(event.getType()).thenReturn(BusinessProcessEventType.END_ACTIVITY);
		NoneEndEventActivityBehavior activityBehavior = mock(NoneEndEventActivityBehavior.class);
		when(event.getActivityBehavior()).thenReturn(activityBehavior);
		when(event.getProcessBusinessKey()).thenReturn("processId");
		ExecutionEntity execution = mockDelegateExecution(DEFAULT_ENGINE, ExecutionEntity.class);

		when(event.getExecution()).thenReturn(execution);
		InstanceReference reference = mock(InstanceReference.class);
		Instance workflow = mock(Instance.class);
		when(reference.toInstance()).thenReturn(workflow);
		when(instanceResolver.resolveReference(eq(event.getProcessBusinessKey()))).thenReturn(Optional.of(reference));
		when(propertiesConverter.convertDataFromCamundaToSEIP(eq(execution.getVariablesTyped()), eq(workflow)))
				.thenReturn(Collections.emptyMap());
		bPMDomainObserver.handleBPMNEvent(event);
		verify(domainInstanceService).save(argThat(CustomMatcher.of((InstanceSaveContext context) -> {
			assertEquals(workflow, context.getInstance());
			assertEquals(ActionTypeConstants.COMPLETE, context.getOperation().getUserOperationId());
		})));
	}

	@Test(expected = CamundaIntegrationRuntimeException.class)
	public void workflowEndEventTest_exception_in_loading() {
		MultiEngineBusinessProcessEvent event = mock(MultiEngineBusinessProcessEvent.class);
		when(event.getActivityType()).thenReturn(ActivityType.EVENT);
		when(event.getType()).thenReturn(BusinessProcessEventType.END_ACTIVITY);
		NoneEndEventActivityBehavior activityBehavior = mock(NoneEndEventActivityBehavior.class);
		when(event.getActivityBehavior()).thenReturn(activityBehavior);
		when(event.getProcessBusinessKey()).thenReturn("processId");
		when(instanceResolver.resolveInstances(eq(Collections.singletonList(event.getProcessBusinessKey()))))
				.thenReturn(Collections.emptyList());
		bPMDomainObserver.handleBPMNEvent(event);
	}

	@Test
	public void workflowEndEventTest_not_endEvent() {
		MultiEngineBusinessProcessEvent event = mock(MultiEngineBusinessProcessEvent.class);
		when(event.getActivityType()).thenReturn(ActivityType.EVENT);
		when(event.getType()).thenReturn(BusinessProcessEventType.END_ACTIVITY);
		NoneStartEventActivityBehavior activityBehavior = mock(NoneStartEventActivityBehavior.class);
		when(event.getActivityBehavior()).thenReturn(activityBehavior);
		bPMDomainObserver.handleBPMNEvent(event);
		verify(propertiesConverter, never()).convertDataFromCamundaToSEIP(any(VariableMapImpl.class),
				any(Instance.class));
		verify(domainInstanceService, never()).save(any(InstanceSaveContext.class));
	}

	@Test
	public void workflowEndEventTest_not_endActivity() {
		MultiEngineBusinessProcessEvent event = mock(MultiEngineBusinessProcessEvent.class);
		when(event.getActivityType()).thenReturn(ActivityType.EVENT);
		when(event.getType()).thenReturn(BusinessProcessEventType.START_ACTIVITY);
		bPMDomainObserver.handleBPMNEvent(event);
		verify(propertiesConverter, never()).convertDataFromCamundaToSEIP(any(VariableMapImpl.class),
				any(Instance.class));
		verify(domainInstanceService, never()).save(any(InstanceSaveContext.class));
	}

	@Test
	public void workflowEndEventTest_not_an_event() {
		MultiEngineBusinessProcessEvent event = mock(MultiEngineBusinessProcessEvent.class);
		when(event.getActivityType()).thenReturn(ActivityType.SUBPROCESS);
		bPMDomainObserver.handleBPMNEvent(event);
		verify(propertiesConverter, never()).convertDataFromCamundaToSEIP(any(VariableMapImpl.class),
				any(Instance.class));
		verify(domainInstanceService, never()).save(any(InstanceSaveContext.class));
	}

	@Test
	public void test_OnCreateTaskEvent() {
		MultiEngineBusinessProcessEvent event = mockEvent(BusinessProcessEventType.CREATE_TASK, true);
		InstanceReferenceMock instance = InstanceReferenceMock.createGeneric(new EmfInstance("processDefId"));
		when(domainInstanceService.createInstance(eq("processDefId"), any())).thenReturn(instance.toInstance());
		when(actions.callSlowAction(any(SaveRequest.class))).thenReturn(instance.toInstance());
		DataTypeDefinition dataTypeDefinition = mock(DataTypeDefinition.class);
		when(definitionService.getDataTypeDefinition(eq(ObjectInstance.class.getName())))
				.thenReturn(dataTypeDefinition);
		bPMDomainObserver.handleBPMNEvent(event);

		verify(domainInstanceService).createInstance(eq("processDefId"), eq("processId"));
		verify(actions).callSlowAction(argThat(CustomMatcher.of((SaveRequest request) -> {
			assertEquals(instance.toInstance(), request.getTarget());
			assertEquals(ActionTypeConstants.CREATE, request.getUserOperation());
			assertEquals(SaveRequest.OPERATION_NAME, request.getOperation());
		})));
	}

	@Test
	public void testOnCreateExecution() throws IOException {
		MultiEngineBusinessProcessEvent event = mockEvent(BusinessProcessEventType.START_ACTIVITY, false);
		DelegateExecution execution = event.getExecution();
		VariableMapImpl hashMap = new VariableMapImpl();
		hashMap.put("title", "oldTitle");
		when(execution.getVariablesLocal()).thenReturn(hashMap);
		ExecutionEntity process = mock(ExecutionEntity.class);
		when(execution.getProcessInstance()).thenReturn(process);
		TypedValue value = mock(TypedValue.class);
		Map<String, Object> newValues = new HashMap<>();
		newValues.put("title", "newTitle");
		newValues.put("plannedStartDate", new Date());
		newValues.put("missingField", "value");
		newValues.put("taskNotes", "notes");

		when(value.getValue()).thenReturn(newValues);
		when(process.getVariableTyped("TASKST100")).thenReturn(value);

		try (InputStream in = SequenceFlowParserTest.class.getClassLoader().getResourceAsStream("transitions3.bpmn")) {
			BpmnModelInstanceImpl parseModelFromStream = new BpmnParser().parseModelFromStream(in);
			when(execution.getBpmnModelInstance()).thenReturn(parseModelFromStream);
		}
		bPMDomainObserver.handleBPMNEvent(event);
		Map<String, Object> newValuesSet = new HashMap<>(newValues);
		newValuesSet.remove("title");// already contained
		newValuesSet.remove("missingField");// not in form
		verify(execution).setVariablesLocal(eq(newValuesSet));
	}

	@Test
	public void testOnDeleteTaskEvent() {
		MultiEngineBusinessProcessEvent event = mock(MultiEngineBusinessProcessEvent.class);
		when(event.getActivityType()).thenReturn(ActivityType.TASK);
		when(event.getType()).thenReturn(BusinessProcessEventType.DELETE_TASK);
		ExecutionEntity execution = mockDelegateExecution(DEFAULT_ENGINE, ExecutionEntity.class);
		when(execution.getVariable(TASK_ID)).thenReturn("taskId");
		when(event.getExecution()).thenReturn(execution);
		InstanceReference taskReference = mock(InstanceReference.class);
		Instance taskInstance = mock(Instance.class);
		when(taskReference.toInstance()).thenReturn(taskInstance);
		when(instanceResolver.resolveReference(eq("taskId"))).thenReturn(Optional.of(taskReference));
		bPMDomainObserver.handleBPMNEvent(event);
		verify(domainInstanceService).save(argThat(CustomMatcher.of((InstanceSaveContext context) -> {
			assertEquals(taskInstance, context.getInstance());
			assertEquals(ActionTypeConstants.STOP, context.getOperation().getUserOperationId());
		})));
	}

	@Test(expected = CamundaIntegrationRuntimeException.class)
	public void testOnDeleteTaskEvent_null_taskId() {
		MultiEngineBusinessProcessEvent event = mock(MultiEngineBusinessProcessEvent.class);
		when(event.getActivityType()).thenReturn(ActivityType.TASK);
		when(event.getType()).thenReturn(BusinessProcessEventType.DELETE_TASK);
		ExecutionEntity execution = mockDelegateExecution(DEFAULT_ENGINE, ExecutionEntity.class);
		when(execution.getVariable(TASK_ID)).thenReturn(null);
		when(event.getExecution()).thenReturn(execution);
		bPMDomainObserver.handleBPMNEvent(event);
	}

	@Test(expected = CamundaIntegrationRuntimeException.class)
	public void testOnDeleteTaskEvent_blank_taskId() {
		MultiEngineBusinessProcessEvent event = mock(MultiEngineBusinessProcessEvent.class);
		when(event.getActivityType()).thenReturn(ActivityType.TASK);
		when(event.getType()).thenReturn(BusinessProcessEventType.DELETE_TASK);
		ExecutionEntity execution = mockDelegateExecution(DEFAULT_ENGINE, ExecutionEntity.class);
		when(execution.getVariable(TASK_ID)).thenReturn("");
		when(event.getExecution()).thenReturn(execution);
		bPMDomainObserver.handleBPMNEvent(event);
	}

	@Test
	public void testCatchEvent_null_camundaProperties_object() {
		MultiEngineBusinessProcessEvent event = mock(MultiEngineBusinessProcessEvent.class);
		when(event.getActivityType()).thenReturn(ActivityType.EVENT);
		when(event.getType()).thenReturn(BusinessProcessEventType.START_ACTIVITY);
		IntermediateCatchEventActivityBehavior activityBehavior = mock(IntermediateCatchEventActivityBehavior.class);
		when(event.getActivityBehavior()).thenReturn(activityBehavior);
		when(event.getProcessBusinessKey()).thenReturn("processId");
		ExecutionEntity execution = mockDelegateExecution(DEFAULT_ENGINE, ExecutionEntity.class);

		when(event.getExecution()).thenReturn(execution);
		CatchEvent catchEvent = mock(CatchEvent.class);
		when(execution.getBpmnModelElementInstance()).thenReturn(catchEvent);
		ExtensionElements extensionElements = mock(ExtensionElements.class);
		when(catchEvent.getExtensionElements()).thenReturn(extensionElements);
		SignalEventDefinition signalEventDefinition = mock(SignalEventDefinition.class);
		Signal signal = mock(Signal.class);
		when(signal.getName()).thenReturn("testSignalName");
		when(signalEventDefinition.getSignal()).thenReturn(signal);
		when(catchEvent.getEventDefinitions()).thenReturn(Arrays.asList(signalEventDefinition));
		bPMDomainObserver.handleBPMNEvent(event);
		verify(schedulerService, never()).schedule(eq(BPMEventScheduler.BEAN_ID), any(), any());
	}

	@Test
	public void testCatchEvent_null_camundaProperties() {
		MultiEngineBusinessProcessEvent event = mock(MultiEngineBusinessProcessEvent.class);
		when(event.getActivityType()).thenReturn(ActivityType.EVENT);
		when(event.getType()).thenReturn(BusinessProcessEventType.START_ACTIVITY);
		IntermediateCatchEventActivityBehavior activityBehavior = mock(IntermediateCatchEventActivityBehavior.class);
		when(event.getActivityBehavior()).thenReturn(activityBehavior);
		when(event.getProcessBusinessKey()).thenReturn("processId");
		ExecutionEntity execution = mockDelegateExecution(DEFAULT_ENGINE, ExecutionEntity.class);

		when(event.getExecution()).thenReturn(execution);
		CatchEvent catchEvent = mock(CatchEvent.class);
		when(execution.getBpmnModelElementInstance()).thenReturn(catchEvent);
		ExtensionElements extensionElements = mock(ExtensionElements.class);
		when(catchEvent.getExtensionElements()).thenReturn(extensionElements);
		when(extensionElements.getElements()).thenReturn(Collections.emptyList());
		SignalEventDefinition signalEventDefinition = mock(SignalEventDefinition.class);
		Signal signal = mock(Signal.class);
		when(signal.getName()).thenReturn("testSignalName");
		when(signalEventDefinition.getSignal()).thenReturn(signal);
		when(catchEvent.getEventDefinitions()).thenReturn(Arrays.asList(signalEventDefinition));
		bPMDomainObserver.handleBPMNEvent(event);
		verify(schedulerService, never()).schedule(eq(BPMEventScheduler.BEAN_ID), any(), any());
	}

	@Test
	public void testCatchEvent_empty_camundaProperties() {
		MultiEngineBusinessProcessEvent event = mock(MultiEngineBusinessProcessEvent.class);
		when(event.getActivityType()).thenReturn(ActivityType.EVENT);
		when(event.getType()).thenReturn(BusinessProcessEventType.START_ACTIVITY);
		IntermediateCatchEventActivityBehavior activityBehavior = mock(IntermediateCatchEventActivityBehavior.class);
		when(event.getActivityBehavior()).thenReturn(activityBehavior);
		when(event.getProcessBusinessKey()).thenReturn("processId");
		ExecutionEntity execution = mockDelegateExecution(DEFAULT_ENGINE, ExecutionEntity.class);

		when(event.getExecution()).thenReturn(execution);
		CatchEvent catchEvent = mock(CatchEvent.class);
		when(execution.getBpmnModelElementInstance()).thenReturn(catchEvent);
		ExtensionElements extensionElements = mock(ExtensionElements.class);
		when(catchEvent.getExtensionElements()).thenReturn(extensionElements);
		CamundaProperties camundaProperties = mock(CamundaProperties.class);
		when(camundaProperties.getCamundaProperties()).thenReturn(null);
		when(extensionElements.getElements()).thenReturn(Arrays.asList(camundaProperties));
		SignalEventDefinition signalEventDefinition = mock(SignalEventDefinition.class);
		Signal signal = mock(Signal.class);
		when(signal.getName()).thenReturn("testSignalName");
		when(signalEventDefinition.getSignal()).thenReturn(signal);
		when(catchEvent.getEventDefinitions()).thenReturn(Arrays.asList(signalEventDefinition));
		bPMDomainObserver.handleBPMNEvent(event);
		verify(schedulerService, never()).schedule(eq(BPMEventScheduler.BEAN_ID), any(), any());
	}

	@Test
	public void testCatchEvent_not_found_context_instance() {
		MultiEngineBusinessProcessEvent event = mock(MultiEngineBusinessProcessEvent.class);
		when(event.getActivityType()).thenReturn(ActivityType.EVENT);
		when(event.getType()).thenReturn(BusinessProcessEventType.START_ACTIVITY);
		IntermediateCatchEventActivityBehavior activityBehavior = mock(IntermediateCatchEventActivityBehavior.class);
		when(event.getActivityBehavior()).thenReturn(activityBehavior);
		when(event.getProcessBusinessKey()).thenReturn("processId");
		ExecutionEntity execution = mockDelegateExecution(DEFAULT_ENGINE, ExecutionEntity.class);

		when(event.getExecution()).thenReturn(execution);
		CatchEvent catchEvent = mock(CatchEvent.class);
		when(execution.getBpmnModelElementInstance()).thenReturn(catchEvent);
		ExtensionElements extensionElements = mock(ExtensionElements.class);
		when(catchEvent.getExtensionElements()).thenReturn(extensionElements);
		CamundaProperties camundaProperties = mock(CamundaProperties.class);
		when(camundaProperties.getCamundaProperties()).thenReturn(Collections.emptyList());
		when(extensionElements.getElements()).thenReturn(Arrays.asList(camundaProperties));
		SignalEventDefinition signalEventDefinition = mock(SignalEventDefinition.class);
		Signal signal = mock(Signal.class);
		when(signal.getName()).thenReturn("testSignalName");
		when(signalEventDefinition.getSignal()).thenReturn(signal);
		when(catchEvent.getEventDefinitions()).thenReturn(Arrays.asList(signalEventDefinition));
		when(bpmEventScheduler.findContextInstances(any(SchedulerContext.class),
				any(MultiEngineBusinessProcessEvent.class))).thenReturn(Collections.emptyList());
		bPMDomainObserver.handleBPMNEvent(event);
		verify(schedulerService, never()).schedule(eq(BPMEventScheduler.BEAN_ID), any(), any());
	}

	@Test
	public void testCatchEvent() {
		MultiEngineBusinessProcessEvent event = mock(MultiEngineBusinessProcessEvent.class);
		when(event.getActivityType()).thenReturn(ActivityType.EVENT);
		when(event.getType()).thenReturn(BusinessProcessEventType.START_ACTIVITY);
		IntermediateCatchEventActivityBehavior activityBehavior = mock(IntermediateCatchEventActivityBehavior.class);
		when(event.getActivityBehavior()).thenReturn(activityBehavior);
		when(event.getProcessBusinessKey()).thenReturn("processId");
		ExecutionEntity execution = mockDelegateExecution(DEFAULT_ENGINE, ExecutionEntity.class);

		when(event.getExecution()).thenReturn(execution);
		CatchEvent catchEvent = mock(CatchEvent.class);
		when(execution.getBpmnModelElementInstance()).thenReturn(catchEvent);
		ExtensionElements extensionElements = mock(ExtensionElements.class);
		when(catchEvent.getExtensionElements()).thenReturn(extensionElements);
		CamundaProperties camundaProperties = mock(CamundaProperties.class);
		when(camundaProperties.getCamundaProperties()).thenReturn(Collections.emptyList());
		when(extensionElements.getElements()).thenReturn(Arrays.asList(camundaProperties));
		SignalEventDefinition signalEventDefinition = mock(SignalEventDefinition.class);
		Signal signal = mock(Signal.class);
		when(signal.getName()).thenReturn("testSignalName");
		when(signalEventDefinition.getSignal()).thenReturn(signal);
		when(catchEvent.getEventDefinitions()).thenReturn(Arrays.asList(signalEventDefinition));
		Instance workflow = mock(Instance.class);

		when(bpmEventScheduler.findContextInstances(any(SchedulerContext.class),
				any(MultiEngineBusinessProcessEvent.class))).thenReturn(Collections.singletonList(workflow));
		SchedulerConfiguration schedulerConfiguration = mock(SchedulerConfiguration.class);
		when(schedulerService.buildConfiguration(any(EmfEvent.class), any(Instance.class)))
				.thenReturn(schedulerConfiguration);

		bPMDomainObserver.handleBPMNEvent(event);

		verify(schedulerService).schedule(anyString(), any(SchedulerConfiguration.class), any(SchedulerContext.class));
	}

	@Test
	public void testOnOtherEvent() {
		MultiEngineBusinessProcessEvent event = mockEvent(BusinessProcessEventType.TAKE, true);
		bPMDomainObserver.handleBPMNEvent(event);
		verify(domainInstanceService, new Times(0)).createInstance(eq("processDefId"), isNull(String.class));
	}
}
