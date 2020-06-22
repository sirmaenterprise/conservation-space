package com.sirmaenterprise.sep.bpm.camunda.schedules;

import static com.sirmaenterprise.sep.bpm.camunda.schedules.BPMEventScheduler.EVENT_NAME;
import static com.sirmaenterprise.sep.bpm.camunda.schedules.BPMEventScheduler.EXECUTION_ID;
import static com.sirmaenterprise.sep.bpm.camunda.schedules.BPMEventScheduler.RELATION_ID;
import static com.sirmaenterprise.sep.bpm.camunda.schedules.BPMEventScheduler.SERVER_ACTION;
import static com.sirmaenterprise.sep.bpm.camunda.schedules.BPMEventScheduler.TARGET_DEF_ID;
import static com.sirmaenterprise.sep.bpm.camunda.schedules.BPMEventScheduler.USER_ACTION;
import static com.sirmaenterprise.sep.bpm.camunda.schedules.BPMEventScheduler.WORKFLOW_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.model.bpmn.instance.CatchEvent;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.Signal;
import org.camunda.bpm.model.bpmn.instance.SignalEventDefinition;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.actions.save.SaveRequest;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.instance.state.AfterOperationExecutedEvent;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntry;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirmaenterprise.sep.bpm.bpmn.ProcessService;
import com.sirmaenterprise.sep.bpm.camunda.bpmn.event.MultiEngineBusinessProcessEvent;
import com.sirmaenterprise.sep.bpm.camunda.schedule.BPMScheduleWrapperEvent;

/**
 * Test class for {@link BPMEventScheduler}.
 *
 * @author hlungov
 */
@SuppressWarnings("static-method")
public class BPMEventSchedulerTest {

	@Mock
	private InstanceTypeResolver instanceResolver;
	@Mock
	private RuntimeService runtimeService;
	@Mock
	private SchedulerService schedulerService;
	@Mock
	private TransactionSupport transactionSupport;
	@Mock
	private LinkService linkService;
	@Mock
	private SecurityContextManager securityContextManager;
	@Mock
	private ProcessService processService;

	@InjectMocks
	private BPMEventScheduler bpmEventScheduler;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void validateInputTest() {
		List<Pair<String, Class<?>>> pairList = bpmEventScheduler.validateInput();
		assertTrue(!pairList.isEmpty());
		assertTrue(pairList.get(0).getFirst().equalsIgnoreCase(WORKFLOW_ID));
	}

	@Test
	public void executeTest_signal_event_with_create_no_links() throws Exception {
		SchedulerContext schedulerContext = mockSchedulerContext();
		InstanceReference instanceReference = mock(InstanceReference.class);
		when(instanceResolver.resolveReference("testWFId")).thenReturn(Optional.of(instanceReference));
		when(linkService.getLinks(instanceReference, "testRelationId")).thenReturn(Collections.emptyList());

		bpmEventScheduler.execute(schedulerContext);
		verify(processService, never()).notify("testEventId", "testExecutionId", null);
	}

	@Test
	public void executeTest_signal_event_with_create() throws Exception {
		SchedulerContext schedulerContext = mockSchedulerContext();

		InstanceReference instanceReference = mock(InstanceReference.class);
		Instance wfInstance = mock(Instance.class);
		when(instanceReference.toInstance()).thenReturn(wfInstance);
		when(wfInstance.getIdentifier()).thenReturn("testDefId");
		when(instanceResolver.resolveReference("testWFId")).thenReturn(Optional.of(instanceReference));
		LinkReference correctLinkReference = mock(LinkReference.class);
		when(correctLinkReference.getTo()).thenReturn(instanceReference);

		LinkReference incorrectLinkReference = mock(LinkReference.class);
		InstanceReference incorrectInstanceReference = mock(InstanceReference.class);
		Instance incorrectInstance = mock(Instance.class);
		when(incorrectInstanceReference.toInstance()).thenReturn(incorrectInstance);
		when(incorrectInstance.getIdentifier()).thenReturn("otherDefId");
		when(incorrectLinkReference.getTo()).thenReturn(incorrectInstanceReference);

		when(linkService.getLinks(instanceReference, "testRelationId")).thenReturn(Arrays.asList(incorrectLinkReference, correctLinkReference));

		bpmEventScheduler.execute(schedulerContext);
		verify(processService).notify("testEventId", "testExecutionId", null);
	}

	@Test
	public void createConfigurationEventTest() {
		SchedulerContext schedulerContext = mockSchedulerContext();
		Instance instance = mock(Instance.class);

		assertTrue(BPMEventScheduler.createConfigurationEvent(schedulerContext,
				instance) instanceof BPMScheduleWrapperEvent);

		schedulerContext.put(SERVER_ACTION, "otherServerAction");

		assertTrue(BPMEventScheduler.createConfigurationEvent(schedulerContext,
				instance) instanceof AfterOperationExecutedEvent);
	}

	@Test
	public void findContextInstancesTest_no_targetDefId_and_no_relationId() {
		SchedulerContext schedulerContext = mockSchedulerContext();
		schedulerContext.remove(TARGET_DEF_ID);
		schedulerContext.remove(RELATION_ID);
		MultiEngineBusinessProcessEvent event = mock(MultiEngineBusinessProcessEvent.class);
		Collection<Instance> contextInstances = bpmEventScheduler.findContextInstances(schedulerContext, event);
		assertTrue(contextInstances.isEmpty());
	}

	@Test
	public void findContextInstancesTest_return_WF_instance() {
		SchedulerContext schedulerContext = mockSchedulerContext();
		MultiEngineBusinessProcessEvent event = mock(MultiEngineBusinessProcessEvent.class);
		when(event.getProcessBusinessKey()).thenReturn("testWFId");
		InstanceReference workflowInstanceReference = mock(InstanceReference.class);
		Instance workflowInstance = mock(Instance.class);
		when(workflowInstanceReference.toInstance()).thenReturn(workflowInstance);
		when(instanceResolver.resolveReference("testWFId")).thenReturn(Optional.of(workflowInstanceReference));
		Collection<Instance> contextInstances = bpmEventScheduler.findContextInstances(schedulerContext, event);
		assertTrue(contextInstances.contains(workflowInstance));
	}

	@Test
	public void findContextInstancesTest_empty_list() {
		SchedulerContext schedulerContext = mockSchedulerContext();
		MultiEngineBusinessProcessEvent event = mock(MultiEngineBusinessProcessEvent.class);
		when(event.getProcessBusinessKey()).thenReturn("testWFId");
		InstanceReference workflowInstanceReference = mock(InstanceReference.class);
		when(instanceResolver.resolveReference("testWFId")).thenReturn(Optional.of(workflowInstanceReference));
		schedulerContext.put(SERVER_ACTION, "otherServerAction");
		when(linkService.getLinks(workflowInstanceReference, "testRelationId")).thenReturn(Arrays.asList());

		Collection<Instance> contextInstances = bpmEventScheduler.findContextInstances(schedulerContext, event);
		assertTrue(contextInstances.isEmpty());
	}

	@Test
	public void test_should_findContextInstances() {
		SchedulerContext schedulerContext = mockSchedulerContext();
		MultiEngineBusinessProcessEvent event = mock(MultiEngineBusinessProcessEvent.class);
		when(event.getProcessBusinessKey()).thenReturn("testWFId");
		InstanceReference workflowInstanceReference = mock(InstanceReference.class);
		Instance workflowInstance = mock(Instance.class);
		when(workflowInstance.getIdentifier()).thenReturn("testDefId");
		when(workflowInstanceReference.toInstance()).thenReturn(workflowInstance);
		when(instanceResolver.resolveReference("testWFId")).thenReturn(Optional.of(workflowInstanceReference));
		schedulerContext.put(SERVER_ACTION, "otherServerAction");
		LinkReference linkReference = mock(LinkReference.class);
		when(linkReference.getTo()).thenReturn(workflowInstanceReference);
		when(linkService.getLinks(workflowInstanceReference, "testRelationId")).thenReturn(Arrays.asList(linkReference));

		Collection<Instance> contextInstances = bpmEventScheduler.findContextInstances(schedulerContext, event);
		assertTrue(!contextInstances.isEmpty());
		assertTrue(contextInstances.contains(workflowInstance));
	}

	private static SchedulerContext mockSchedulerContext() {
		SchedulerContext schedulerContext = new SchedulerContext(8);
		SchedulerEntry schedulerEntry = mock(SchedulerEntry.class);
		SchedulerConfiguration schedulerConfiguration = mock(SchedulerConfiguration.class);
		when(schedulerEntry.getConfiguration()).thenReturn(schedulerConfiguration);
		schedulerContext.put(SchedulerContext.SCHEDULER_ENTRY, schedulerEntry);
		schedulerContext.put(WORKFLOW_ID, "testWFId");
		schedulerContext.put(EXECUTION_ID, "testExecutionId");
		schedulerContext.put(EVENT_NAME, "testEventId");
		schedulerContext.put(RELATION_ID, "testRelationId");
		schedulerContext.put(TARGET_DEF_ID, "testDefId");
		schedulerContext.put(USER_ACTION, "create");
		schedulerContext.put(SERVER_ACTION, SaveRequest.OPERATION_NAME);
		return schedulerContext;
	}

	@Test
	public void test_should_context_be_empty_createExecutorContext() {
		MultiEngineBusinessProcessEvent event = mock(MultiEngineBusinessProcessEvent.class);
		DelegateExecution execution = mock(DelegateExecution.class);
		when(event.getExecution()).thenReturn(execution);
		SchedulerContext executorContext = BPMEventScheduler.createExecutorContext(event);
		assertTrue(executorContext.isEmpty());
	}

	@Test
	public void test_should_miss_event_properties_createExecutorContext() {
		MultiEngineBusinessProcessEvent event = mock(MultiEngineBusinessProcessEvent.class);
		DelegateExecution execution = mock(DelegateExecution.class);
		when(event.getExecution()).thenReturn(execution);
		CatchEvent catchEvent = mock(CatchEvent.class);
		when(execution.getBpmnModelElementInstance()).thenReturn(catchEvent);
		SignalEventDefinition eventDefinition = mock(SignalEventDefinition.class);
		when(catchEvent.getEventDefinitions()).thenReturn(Collections.singletonList(eventDefinition));
		ExtensionElements extensionElements = mock(ExtensionElements.class);
		when(catchEvent.getExtensionElements()).thenReturn(extensionElements);
		Signal signal = mock(Signal.class);
		when(eventDefinition.getSignal()).thenReturn(signal);
		when(signal.getName()).thenReturn("testEventId");
		SchedulerContext context = BPMEventScheduler.createExecutorContext(event);
		assertEquals(0, context.size());
	}

	@Test
	public void test_should_empty_event_properties_createExecutorContext() {
		MultiEngineBusinessProcessEvent event = mock(MultiEngineBusinessProcessEvent.class);
		DelegateExecution execution = mock(DelegateExecution.class);
		when(event.getExecution()).thenReturn(execution);
		CatchEvent catchEvent = mock(CatchEvent.class);
		when(execution.getBpmnModelElementInstance()).thenReturn(catchEvent);
		SignalEventDefinition eventDefinition = mock(SignalEventDefinition.class);
		when(catchEvent.getEventDefinitions()).thenReturn(Collections.singletonList(eventDefinition));
		ExtensionElements extensionElements = mock(ExtensionElements.class);
		when(catchEvent.getExtensionElements()).thenReturn(extensionElements);
		CamundaProperties camundaProperties = mock(CamundaProperties.class);
		when(camundaProperties.getCamundaProperties()).thenReturn(null);
		when(extensionElements.getElements()).thenReturn(Collections.singletonList(camundaProperties));
		Signal signal = mock(Signal.class);
		when(eventDefinition.getSignal()).thenReturn(signal);
		when(signal.getName()).thenReturn("testEventId");
		SchedulerContext context = BPMEventScheduler.createExecutorContext(event);
		assertEquals(0, context.size());
	}

	@Test
	public void test_should_createExecutorContext() {
		MultiEngineBusinessProcessEvent event = mock(MultiEngineBusinessProcessEvent.class);
		when(event.getProcessBusinessKey()).thenReturn("testWFId");
		DelegateExecution execution = mock(DelegateExecution.class);
		when(execution.getId()).thenReturn("testCamundaWFId");
		when(event.getExecution()).thenReturn(execution);
		CatchEvent catchEvent = mock(CatchEvent.class);
		when(execution.getBpmnModelElementInstance()).thenReturn(catchEvent);
		SignalEventDefinition eventDefinition = mock(SignalEventDefinition.class);
		when(catchEvent.getEventDefinitions()).thenReturn(Collections.singletonList(eventDefinition));
		ExtensionElements extensionElements = mock(ExtensionElements.class);
		when(catchEvent.getExtensionElements()).thenReturn(extensionElements);
		CamundaProperties camundaProperties = mock(CamundaProperties.class);
		CamundaProperty camundaProperty = mock(CamundaProperty.class);
		when(camundaProperties.getCamundaProperties()).thenReturn(Collections.singletonList(camundaProperty));
		when(extensionElements.getElements()).thenReturn(Collections.singletonList(camundaProperties));
		Signal signal = mock(Signal.class);
		when(eventDefinition.getSignal()).thenReturn(signal);
		when(signal.getName()).thenReturn("testEventId");
		SchedulerContext schedulerContext = BPMEventScheduler.createExecutorContext(event);
		assertEquals("testWFId", schedulerContext.get(WORKFLOW_ID));
		assertEquals("testCamundaWFId", schedulerContext.get(EXECUTION_ID));
		assertEquals("testEventId", schedulerContext.get(EVENT_NAME));
	}
}