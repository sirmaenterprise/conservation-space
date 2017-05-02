package com.sirmaenterprise.sep.bpm.camunda.schedules;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.actions.save.CreateOrUpdateRequest;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.instance.state.AfterOperationExecutedEvent;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntry;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirmaenterprise.sep.bpm.camunda.bpmn.event.MultiEngineBusinessProcessEvent;
import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationRuntimeException;
import com.sirmaenterprise.sep.bpm.camunda.schedule.BPMScheduleWrapperEvent;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.EventSubscriptionQuery;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.sirmaenterprise.sep.bpm.camunda.schedules.BPMEventScheduler.EVENT_NAME;
import static com.sirmaenterprise.sep.bpm.camunda.schedules.BPMEventScheduler.EXECUTION_ID;
import static com.sirmaenterprise.sep.bpm.camunda.schedules.BPMEventScheduler.RELATION_ID;
import static com.sirmaenterprise.sep.bpm.camunda.schedules.BPMEventScheduler.SERVER_ACTION;
import static com.sirmaenterprise.sep.bpm.camunda.schedules.BPMEventScheduler.TARGET_DEF_ID;
import static com.sirmaenterprise.sep.bpm.camunda.schedules.BPMEventScheduler.USER_ACTION;
import static com.sirmaenterprise.sep.bpm.camunda.schedules.BPMEventScheduler.WORKFLOW_ID;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link BPMEventScheduler}.
 *
 * @author hlungov
 */
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
	public void executeTest_signal_event() throws Exception {
		SchedulerContext schedulerContext = new SchedulerContext(8);
		SchedulerEntry schedulerEntry = mock(SchedulerEntry.class);
		SchedulerConfiguration schedulerConfiguration = mock(SchedulerConfiguration.class);
		when(schedulerEntry.getConfiguration()).thenReturn(schedulerConfiguration);
		schedulerContext.put(SchedulerContext.SCHEDULER_ENTRY, schedulerEntry);
		schedulerContext.put(WORKFLOW_ID, "testWFId");
		schedulerContext.put(EXECUTION_ID, "testExecutionId");
		schedulerContext.put(EVENT_NAME, "testEventId");
		schedulerContext.put(TARGET_DEF_ID, "testDefId");
		schedulerContext.put(USER_ACTION, "testUserAction");
		schedulerContext.put(SERVER_ACTION, "testServerAction");
		EventSubscription eventSubscription = mock(EventSubscription.class);
		when(eventSubscription.getEventType()).thenReturn("signal");
		when(eventSubscription.getEventName()).thenReturn("testEventId");
		EventSubscriptionQuery eventSubscriptionQuery = mock(EventSubscriptionQuery.class);
		when(runtimeService.createEventSubscriptionQuery()).thenReturn(eventSubscriptionQuery);
		when(eventSubscriptionQuery.executionId("testExecutionId")).thenReturn(eventSubscriptionQuery);
		when(eventSubscriptionQuery.eventName("testEventId")).thenReturn(eventSubscriptionQuery);
		when(eventSubscriptionQuery.list()).thenReturn(Arrays.asList(eventSubscription));
		bpmEventScheduler.execute(schedulerContext);
		verify(runtimeService).signalEventReceived("testEventId", "testExecutionId");
	}

	@Test
	public void executeTest_message_event() throws Exception {
		SchedulerContext schedulerContext = new SchedulerContext(8);
		SchedulerEntry schedulerEntry = mock(SchedulerEntry.class);
		SchedulerConfiguration schedulerConfiguration = mock(SchedulerConfiguration.class);
		when(schedulerEntry.getConfiguration()).thenReturn(schedulerConfiguration);
		schedulerContext.put(SchedulerContext.SCHEDULER_ENTRY, schedulerEntry);
		schedulerContext.put(WORKFLOW_ID, "testWFId");
		schedulerContext.put(EXECUTION_ID, "testExecutionId");
		schedulerContext.put(EVENT_NAME, "testEventId");
		schedulerContext.put(TARGET_DEF_ID, "testDefId");
		schedulerContext.put(USER_ACTION, "testUserAction");
		schedulerContext.put(SERVER_ACTION, "testServerAction");
		EventSubscription eventSubscription = mock(EventSubscription.class);
		when(eventSubscription.getEventType()).thenReturn("message");
		when(eventSubscription.getEventName()).thenReturn("testEventId");
		EventSubscriptionQuery eventSubscriptionQuery = mock(EventSubscriptionQuery.class);
		when(runtimeService.createEventSubscriptionQuery()).thenReturn(eventSubscriptionQuery);
		when(eventSubscriptionQuery.executionId("testExecutionId")).thenReturn(eventSubscriptionQuery);
		when(eventSubscriptionQuery.eventName("testEventId")).thenReturn(eventSubscriptionQuery);
		when(eventSubscriptionQuery.list()).thenReturn(Arrays.asList(eventSubscription));
		bpmEventScheduler.execute(schedulerContext);
		verify(runtimeService).messageEventReceived("testEventId", "testExecutionId");
	}

	@Test
	public void executeTest_signal_event_only_operation_match() throws Exception {
		SchedulerContext schedulerContext = new SchedulerContext(8);
		SchedulerEntry schedulerEntry = mock(SchedulerEntry.class);
		SchedulerConfiguration schedulerConfiguration = mock(SchedulerConfiguration.class);
		when(schedulerEntry.getConfiguration()).thenReturn(schedulerConfiguration);
		schedulerContext.put(SchedulerContext.SCHEDULER_ENTRY, schedulerEntry);
		schedulerContext.put(WORKFLOW_ID, "testWFId");
		schedulerContext.put(EXECUTION_ID, "testExecutionId");
		schedulerContext.put(EVENT_NAME, "testEventId");
		schedulerContext.put(TARGET_DEF_ID, "testDefId");
		schedulerContext.put(USER_ACTION, "testUserAction");
		schedulerContext.put(SERVER_ACTION, CreateOrUpdateRequest.OPERATION_NAME);
		EventSubscription eventSubscription = mock(EventSubscription.class);
		when(eventSubscription.getEventType()).thenReturn("signal");
		when(eventSubscription.getEventName()).thenReturn("testEventId");
		EventSubscriptionQuery eventSubscriptionQuery = mock(EventSubscriptionQuery.class);
		when(runtimeService.createEventSubscriptionQuery()).thenReturn(eventSubscriptionQuery);
		when(eventSubscriptionQuery.executionId("testExecutionId")).thenReturn(eventSubscriptionQuery);
		when(eventSubscriptionQuery.eventName("testEventId")).thenReturn(eventSubscriptionQuery);
		when(eventSubscriptionQuery.list()).thenReturn(Arrays.asList(eventSubscription));
		bpmEventScheduler.execute(schedulerContext);
		verify(runtimeService).signalEventReceived("testEventId", "testExecutionId");
	}

	@Test
	public void executeTest_signal_event_only_operation_and_useraction_match() throws Exception {
		SchedulerContext schedulerContext = new SchedulerContext(8);
		SchedulerEntry schedulerEntry = mock(SchedulerEntry.class);
		SchedulerConfiguration schedulerConfiguration = mock(SchedulerConfiguration.class);
		when(schedulerEntry.getConfiguration()).thenReturn(schedulerConfiguration);
		schedulerContext.put(SchedulerContext.SCHEDULER_ENTRY, schedulerEntry);
		schedulerContext.put(WORKFLOW_ID, "testWFId");
		schedulerContext.put(EXECUTION_ID, "testExecutionId");
		schedulerContext.put(EVENT_NAME, "testEventId");
		schedulerContext.put(TARGET_DEF_ID, "testDefId");
		schedulerContext.put(USER_ACTION, "create");
		schedulerContext.put(SERVER_ACTION, CreateOrUpdateRequest.OPERATION_NAME);
		EventSubscription eventSubscription = mock(EventSubscription.class);
		when(eventSubscription.getEventType()).thenReturn("signal");
		when(eventSubscription.getEventName()).thenReturn("testEventId");
		EventSubscriptionQuery eventSubscriptionQuery = mock(EventSubscriptionQuery.class);
		when(runtimeService.createEventSubscriptionQuery()).thenReturn(eventSubscriptionQuery);
		when(eventSubscriptionQuery.executionId("testExecutionId")).thenReturn(eventSubscriptionQuery);
		when(eventSubscriptionQuery.eventName("testEventId")).thenReturn(eventSubscriptionQuery);
		when(eventSubscriptionQuery.list()).thenReturn(Arrays.asList(eventSubscription));
		bpmEventScheduler.execute(schedulerContext);
		verify(runtimeService).signalEventReceived("testEventId", "testExecutionId");
	}

	@Test
	public void executeTest_signal_event_with_create_no_links() throws Exception {
		SchedulerContext schedulerContext = mockSchedulerContext();
		EventSubscription eventSubscription = mock(EventSubscription.class);
		when(eventSubscription.getEventType()).thenReturn("signal");
		when(eventSubscription.getEventName()).thenReturn("testEventId");
		EventSubscriptionQuery eventSubscriptionQuery = mock(EventSubscriptionQuery.class);
		when(runtimeService.createEventSubscriptionQuery()).thenReturn(eventSubscriptionQuery);
		when(eventSubscriptionQuery.executionId("testExecutionId")).thenReturn(eventSubscriptionQuery);
		when(eventSubscriptionQuery.eventName("testEventId")).thenReturn(eventSubscriptionQuery);
		when(eventSubscriptionQuery.list()).thenReturn(Arrays.asList(eventSubscription));

		InstanceReference instanceReference = mock(InstanceReference.class);
		when(instanceResolver.resolveReference("testWFId")).thenReturn(Optional.of(instanceReference));
		when(linkService.getLinks(instanceReference, "testRelationId")).thenReturn(Collections.emptyList());

		bpmEventScheduler.execute(schedulerContext);
		verify(runtimeService, never()).signalEventReceived("testEventId", "testExecutionId");
	}

	@Test
	public void executeTest_signal_event_with_create() throws Exception {
		SchedulerContext schedulerContext = mockSchedulerContext();
		EventSubscription eventSubscription = mock(EventSubscription.class);
		when(eventSubscription.getEventType()).thenReturn("signal");
		when(eventSubscription.getEventName()).thenReturn("testEventId");
		EventSubscriptionQuery eventSubscriptionQuery = mock(EventSubscriptionQuery.class);
		when(runtimeService.createEventSubscriptionQuery()).thenReturn(eventSubscriptionQuery);
		when(eventSubscriptionQuery.executionId("testExecutionId")).thenReturn(eventSubscriptionQuery);
		when(eventSubscriptionQuery.eventName("testEventId")).thenReturn(eventSubscriptionQuery);
		when(eventSubscriptionQuery.list()).thenReturn(Arrays.asList(eventSubscription));

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
		verify(runtimeService).signalEventReceived("testEventId", "testExecutionId");
	}

	@Test
	public void createConfigurationEventTest() {
		SchedulerContext schedulerContext = mockSchedulerContext();
		Instance instance = mock(Instance.class);

		assertTrue(bpmEventScheduler.createConfigurationEvent(schedulerContext, instance) instanceof BPMScheduleWrapperEvent);

		schedulerContext.put(SERVER_ACTION, "otherServerAction");

		assertTrue(bpmEventScheduler.createConfigurationEvent(schedulerContext, instance) instanceof AfterOperationExecutedEvent);
	}

	@Test(expected = CamundaIntegrationRuntimeException.class)
	public void findContextInstancesTest_no_relationId() {
		SchedulerContext schedulerContext = mockSchedulerContext();
		schedulerContext.remove(RELATION_ID);
		MultiEngineBusinessProcessEvent event = mock(MultiEngineBusinessProcessEvent.class);
		bpmEventScheduler.findContextInstances(schedulerContext, event);
	}

	@Test(expected = CamundaIntegrationRuntimeException.class)
	public void findContextInstancesTest_no_targetDefId() {
		SchedulerContext schedulerContext = mockSchedulerContext();
		schedulerContext.remove(TARGET_DEF_ID);
		MultiEngineBusinessProcessEvent event = mock(MultiEngineBusinessProcessEvent.class);
		bpmEventScheduler.findContextInstances(schedulerContext, event);
	}

	@Test(expected = CamundaIntegrationRuntimeException.class)
	public void findContextInstancesTest_no_targetDefId_and_no_relationId() {
		SchedulerContext schedulerContext = mockSchedulerContext();
		schedulerContext.remove(TARGET_DEF_ID);
		schedulerContext.remove(RELATION_ID);
		MultiEngineBusinessProcessEvent event = mock(MultiEngineBusinessProcessEvent.class);
		bpmEventScheduler.findContextInstances(schedulerContext, event);
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
	public void findContextInstancesTest() {
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
		schedulerContext.put(SERVER_ACTION, CreateOrUpdateRequest.OPERATION_NAME);
		return schedulerContext;
	}
}