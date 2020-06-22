package com.sirmaenterprise.sep.bpm.camunda.observers;

import static com.sirmaenterprise.sep.bpm.camunda.model.DomainProcessConstants.COMPLETED_ON;
import static com.sirmaenterprise.sep.bpm.model.ProcessConstants.ACTIVITY_ID;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.domain.instance.event.ObjectPropertyAddEvent;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.context.InstanceContextService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.security.context.ContextualExecutor;
import com.sirma.itt.seip.security.context.ContextualWrapper;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirmaenterprise.sep.bpm.camunda.bpmn.CamundaBPMNService;
import com.sirmaenterprise.sep.bpm.camunda.properties.BPMTaskProperties;
import com.sirmaenterprise.sep.bpm.camunda.service.BPMSecurityService;

/**
 * Tests {@link OperationsExecutionListener}
 *
 * @author bbanchev
 */
public class OperationsExecutionListenerTest {

	@Mock
	private CamundaBPMNService camundaBPMNService;
	@Mock
	private BPMSecurityService bpmSecurityService;
	@Mock
	private InstanceTypeResolver instanceResolver;
	@Mock
	private TransactionSupport transactionSupport;
	@Mock
	private SecurityContextManager securityContextManager;

	@InjectMocks
	private OperationsExecutionListener operationsExecutionListener;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void handleSEPObjectEventsTest_unsupported_event_object_property_name() {
		when(bpmSecurityService.isEngineAvailable()).thenReturn(Boolean.TRUE);
		ObjectPropertyAddEvent linkAddedEvent = mock(ObjectPropertyAddEvent.class);
		when(linkAddedEvent.getObjectPropertyName()).thenReturn(InstanceContextService.PART_OF_URI);
		operationsExecutionListener.handleSEPObjectEvents(linkAddedEvent);
		verify(transactionSupport, never()).invokeOnSuccessfulTransactionInTx(any());
	}

	@Test
	public void handleSEPObjectEventsTest_unsupported_operation() {
		when(bpmSecurityService.isEngineAvailable()).thenReturn(Boolean.TRUE);
		ObjectPropertyAddEvent linkAddedEvent = mock(ObjectPropertyAddEvent.class);
		when(linkAddedEvent.getObjectPropertyName()).thenReturn(InstanceContextService.HAS_PARENT);
		Options.CURRENT_OPERATION.set(new Operation("edit", "edit", true));
		operationsExecutionListener.handleSEPObjectEvents(linkAddedEvent);
		verify(transactionSupport, never()).invokeOnSuccessfulTransactionInTx(any());
		Options.CURRENT_OPERATION.clear();

		Options.CURRENT_OPERATION.set(new Operation("create", "edit", true));
		operationsExecutionListener.handleSEPObjectEvents(linkAddedEvent);
		verify(transactionSupport, never()).invokeOnSuccessfulTransactionInTx(any());
		Options.CURRENT_OPERATION.clear();

		Options.CURRENT_OPERATION.set(new Operation("edit", "create", true));
		operationsExecutionListener.handleSEPObjectEvents(linkAddedEvent);
		verify(transactionSupport, never()).invokeOnSuccessfulTransactionInTx(any());
		Options.CURRENT_OPERATION.clear();
	}

	@Test
	public void handleSEPObjectEventsTest() {
		when(bpmSecurityService.isEngineAvailable()).thenReturn(Boolean.TRUE);
		ObjectPropertyAddEvent linkAddedEvent = mock(ObjectPropertyAddEvent.class);
		when(linkAddedEvent.getSourceId()).thenReturn("emf:task1");
		when(linkAddedEvent.getTargetId()).thenReturn("emf:hristo");
		InstanceReference taskReference = mock(InstanceReference.class);
		when(instanceResolver.resolveReference("emf:task1")).thenReturn(Optional.of(taskReference));
		Instance taskInstance = mock(Instance.class);
		when(taskReference.toInstance()).thenReturn(taskInstance);
		InstanceReference resourceReference = mock(InstanceReference.class);
		when(instanceResolver.resolveReference("emf:hristo")).thenReturn(Optional.of(resourceReference));
		Instance resourceInstance = mock(Instance.class);
		when(resourceReference.toInstance()).thenReturn(resourceInstance);
		when(linkAddedEvent.getObjectPropertyName()).thenReturn(InstanceContextService.HAS_PARENT);
		Options.CURRENT_OPERATION.set(new Operation("create", "create", true));
		ContextualExecutor contextualExecutor = mock(ContextualExecutor.class);
		ContextualWrapper contextualWrapper = mock(ContextualWrapper.class);
		when(contextualExecutor.toWrapper()).thenReturn(contextualWrapper);
		when(securityContextManager.executeAsAdmin()).thenReturn(contextualExecutor);
		operationsExecutionListener.handleSEPObjectEvents(linkAddedEvent);
		verify(transactionSupport).invokeOnSuccessfulTransactionInTx(any());
		Options.CURRENT_OPERATION.clear();
	}

	@Test
	public void handleSEPObjectEventsTest_camundaEngine_notAvailable() {
		when(bpmSecurityService.isEngineAvailable()).thenReturn(Boolean.FALSE);
		ObjectPropertyAddEvent linkAddedEvent = mock(ObjectPropertyAddEvent.class);
		operationsExecutionListener.handleSEPObjectEvents(linkAddedEvent);
		verify(linkAddedEvent, never()).getObjectPropertyName();
	}

	@Test
	public void handleSEPObjectEventsTest_hasAssignee_change_wrong_operation() {
		when(bpmSecurityService.isEngineAvailable()).thenReturn(Boolean.TRUE);
		ObjectPropertyAddEvent linkAddedEvent = mock(ObjectPropertyAddEvent.class);
		Options.CURRENT_OPERATION.set(new Operation("create", "create", false));
		when(linkAddedEvent.getObjectPropertyName()).thenReturn(BPMTaskProperties.HAS_ASSIGNEE);
		operationsExecutionListener.handleSEPObjectEvents(linkAddedEvent);
		verify(camundaBPMNService, never()).reassignTask(any(Instance.class), any(String.class));
		Options.CURRENT_OPERATION.clear();
	}

	@Test
	public void handleSEPObjectEventsTest_hasAssignee_change_current_operation_wrong_object() {
		when(bpmSecurityService.isEngineAvailable()).thenReturn(Boolean.TRUE);
		ObjectPropertyAddEvent linkAddedEvent = mock(ObjectPropertyAddEvent.class);
		Options.CURRENT_OPERATION.set("test");
		when(linkAddedEvent.getObjectPropertyName()).thenReturn(BPMTaskProperties.HAS_ASSIGNEE);
		operationsExecutionListener.handleSEPObjectEvents(linkAddedEvent);
		verify(camundaBPMNService, never()).reassignTask(any(Instance.class), any(String.class));
		Options.CURRENT_OPERATION.clear();
	}

	@Test
	public void handleSEPObjectEventsTest_hasAssignee_change() {
		when(bpmSecurityService.isEngineAvailable()).thenReturn(Boolean.TRUE);
		ObjectPropertyAddEvent linkAddedEvent = mock(ObjectPropertyAddEvent.class);
		when(linkAddedEvent.getSourceId()).thenReturn("emf:task1");
		when(linkAddedEvent.getTargetId()).thenReturn("emf:hristo");
		Options.CURRENT_OPERATION.set(new Operation("addRelation", ActionTypeConstants.REASSIGN_TASK, true));
		when(linkAddedEvent.getObjectPropertyName()).thenReturn(BPMTaskProperties.HAS_ASSIGNEE);
		InstanceReference taskReference = mock(InstanceReference.class);
		Instance taskInstance = mock(Instance.class);
		taskInstance.add(COMPLETED_ON, new Date());
		InstanceType instanceType = mock(InstanceType.class);
		when(instanceType.is("workflowinstancecontext")).thenReturn(Boolean.FALSE).thenReturn(Boolean.TRUE);
		when(taskInstance.isValueNotNull(ACTIVITY_ID)).thenReturn(Boolean.TRUE).thenReturn(Boolean.FALSE).thenReturn(Boolean.TRUE);
		when(taskInstance.get(COMPLETED_ON)).thenReturn(new Date()).thenReturn(null);
		when(taskInstance.type()).thenReturn(instanceType);
		when(taskReference.toInstance()).thenReturn(taskInstance);
		when(instanceResolver.resolveReference("emf:task1")).thenReturn(Optional.of(taskReference));
		operationsExecutionListener.handleSEPObjectEvents(linkAddedEvent);
		verify(camundaBPMNService, never()).reassignTask(eq(taskInstance), eq("emf:hristo"));
		operationsExecutionListener.handleSEPObjectEvents(linkAddedEvent);
		verify(camundaBPMNService, never()).reassignTask(eq(taskInstance), eq("emf:hristo"));
		operationsExecutionListener.handleSEPObjectEvents(linkAddedEvent);
		verify(camundaBPMNService).reassignTask(eq(taskInstance), eq("emf:hristo"));
		Options.CURRENT_OPERATION.clear();
	}
}
