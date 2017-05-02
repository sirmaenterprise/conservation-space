package com.sirma.itt.emf.web.action.event;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import com.sirma.itt.emf.security.action.ActionTypeBinding;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.actions.OperationInvoker;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.permissions.action.EmfAction;

/**
 * Test for {@link ImmediateActionObserver}.
 *
 * @author A. Kunchev
 */
public class ImmediateActionObserverTest {

	@InjectMocks
	private ImmediateActionObserver observer = new ImmediateActionObserver();

	@Mock
	private EventService eventService;

	@Mock
	private OperationInvoker operationInvoker;

	@Mock
	private InstanceService instanceService;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void onUserOperation_nullInstace_eventServiceNotCalled() {
		observer.onUserOperation(new EMFActionEvent(null, null, "", new EmfAction("")));
		verify(eventService, never()).fire(any(EmfImmediateActionEvent.class), any(ActionTypeBinding.class));
	}

	@Test
	public void onUserOperation_nullAction_eventServiceNotCalled() {
		observer.onUserOperation(new EMFActionEvent(new EmfInstance(), null, null, null));
		verify(eventService, never()).fire(any(EmfImmediateActionEvent.class), any(ActionTypeBinding.class));
	}

	@Test
	public void onUserOperation_notImmediateAction_eventServiceNotCalled() {
		observer.onUserOperation(new EMFActionEvent(new EmfInstance(), null, "", new EmfAction("")));
		verify(eventService, never()).fire(any(EmfImmediateActionEvent.class), any(ActionTypeBinding.class));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void onUserOperation_notStandartAction_instanceServiceNotCalled_and_calledOperationInvoker() {
		EmfAction action = new EmfAction(ActionTypeConstants.CONFIRM_READ);
		action.setImmediate(true);
		action.setPurpose("auditable");
		observer.onUserOperation(new EMFActionEvent(new EmfInstance(), null, ActionTypeConstants.CONFIRM_READ, action));
		verify(instanceService, never()).save(any(Instance.class), any(Operation.class));
		verify(operationInvoker).createDefaultContext(any(Instance.class), any(Operation.class));
		verify(operationInvoker).invokeOperation(any(Context.class));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void onUserOperation_standartAction_instanceServiceNotCalled_and_calledOperationInvoker() {
		EmfAction action = new EmfAction(ActionTypeConstants.CONFIRM_READ);
		action.setImmediate(true);
		action.setPurpose("action");
		observer.onUserOperation(new EMFActionEvent(new EmfInstance(), null, ActionTypeConstants.UPLOAD, action));
		verify(instanceService).save(any(Instance.class), any(Operation.class));
		verify(operationInvoker, never()).createDefaultContext(any(Instance.class), any(Operation.class));
		verify(operationInvoker, never()).invokeOperation(any(Context.class));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void onUserOperation_handled_instanceServiceNotCalled_and_operationInvokerNotCalled() {
		EmfAction action = new EmfAction(ActionTypeConstants.CONFIRM_READ);
		action.setImmediate(true);
		action.setPurpose("action");
		Mockito.doAnswer(handledAnswer()).when(eventService).fire(any(EmfImmediateActionEvent.class),
				any(ActionTypeBinding.class));
		observer.onUserOperation(new EMFActionEvent(new EmfInstance(), null, ActionTypeConstants.UPLOAD, action));
		verify(instanceService, never()).save(any(Instance.class), any(Operation.class));
		verify(operationInvoker, never()).createDefaultContext(any(Instance.class), any(Operation.class));
		verify(operationInvoker, never()).invokeOperation(any(Context.class));
	}

	private static Answer<?> handledAnswer() {
		return invocation -> {
			((EmfImmediateActionEvent) invocation.getArguments()[0]).setHandled(true);
			return null;
		};
	}

}
