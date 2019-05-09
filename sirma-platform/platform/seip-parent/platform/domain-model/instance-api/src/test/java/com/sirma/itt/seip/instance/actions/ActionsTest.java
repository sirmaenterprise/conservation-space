package com.sirma.itt.seip.instance.actions;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.lock.LockInfo;
import com.sirma.itt.seip.instance.lock.LockService;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Test for {@link Actions}.
 *
 * @author BBonev
 * @author A. Kunchev
 */
public class ActionsTest {

	private static final String REQUEST_UUID = "request-UUID";

	@InjectMocks
	private Actions actions;

	private List<Action<ActionRequest>> actionsList = new LinkedList<>();

	@Spy
	private Plugins<Action<ActionRequest>> actionInstances = new Plugins<>("", actionsList);

	@Mock
	private DomainInstanceService domainInstanceService;

	@Mock
	private Action<ActionRequest> action;

	@Mock
	private LockService lockService;

	@Mock
	private SecurityContext securityContext;

	@Mock
	private TransactionSupport transactionSupport;

	@Mock
	private TransactionManager transactionManager;

	@Mock
	private TransactionalActionExecutor transactionalActionExecutor;

	@Mock
	private NonTransactionalActionExecutor nonTransactionalActionExecutor;

	@Before
	@SuppressWarnings("unchecked")
	public void setup() {
		MockitoAnnotations.initMocks(this);

		actionsList.clear();
		actionsList.add(action);

		when(action.getName()).thenReturn("actionName");

		when(domainInstanceService.loadInstance("emf:instance"))
				.thenAnswer(a -> buildInstance(a.getArgumentAt(0, String.class)));

		when(transactionSupport.invokeInTx(any(Callable.class))).then(a -> a.getArgumentAt(0, Callable.class).call());
		doAnswer(a -> a.getArgumentAt(0, Executable.class).asSupplier().get())
				.when(transactionSupport).invokeInNewTx(any(Executable.class));
		when(securityContext.getRequestId()).thenReturn(REQUEST_UUID);
	}

	private static EmfInstance buildInstance(String id) {
		EmfInstance instance = new EmfInstance(id);
		instance.setReference(mock(InstanceReference.class));
		return instance;
	}

	@Test(expected = NullPointerException.class)
	public void callAction_missingRequest() {
		actions.callAction(null);
	}

	@Test(expected = NullPointerException.class)
	public void callAction_missingOperation() {
		actions.callAction(new RequestStub(null));
	}

	@Test(expected = NullPointerException.class)
	public void callAction_missingTargetId() {
		RequestStub request = new RequestStub("actionName");
		request.setTargetId(null);
		actions.callAction(request);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void callAction_noActionImplementation() {
		RequestStub request = new RequestStub("notSupportedAction");
		request.setUserOperation("doSomething");
		request.setTargetId("emf:instance");
		actions.callAction(request);
	}

	@Test(expected = EmfRuntimeException.class)
	public void callAction_actionShouldBeLockedWithTransaction() throws SystemException {
		RequestStub request = new RequestStub("actionName");
		request.setUserOperation("doSomething");
		request.setTargetId("emf:instance");
		when(action.shouldLockInstanceBeforeAction(request)).thenReturn(true);
		Transaction transaction = mock(Transaction.class);
		when(transactionManager.getTransaction()).thenReturn(transaction);
		actions.callAction(request);
	}

	@Test
	public void callAction_transactionManagerError() throws SystemException {
		RequestStub request = new RequestStub("actionName");
		request.setUserOperation("doSomething");
		request.setTargetId("emf:instance");
		when(transactionManager.getTransaction()).thenThrow(new SystemException());
		try {
			actions.callAction(request);
		} catch (Exception e) {
			assertTrue(e instanceof EmfRuntimeException);
			assertTrue(e.getCause() instanceof SystemException);
		} finally {
			verify(action, never()).perform(request);
		}
	}

	@Test(expected = RuntimeException.class)
	public void callSlowAction_actionValidationFailed() {
		ActionRequest request = new RequestStub("actionName");
		try {
			request.setUserOperation("doSomething");
			request.setTargetId("emf:instance");
			doThrow(new RuntimeException()).when(action).validate(request);

			actions.callSlowAction(request);
		} finally {
			verify(nonTransactionalActionExecutor, never()).execute(action, request);
		}
	}

	@Test
	public void callSlowAction_dontRequireLocking() {
		RequestStub request = new RequestStub("actionName");
		request.setUserOperation("doSomething");
		request.setTargetId("emf:instance");

		when(action.shouldLockInstanceBeforeAction(request)).thenReturn(false);

		actions.callSlowAction(request);

		verify(lockService, never()).tryLock(any(), anyString());
		verify(nonTransactionalActionExecutor).execute(action, request);
		verify(lockService, never()).tryUnlock(any());
	}

	@Test
	public void callSlowAction_shouldLockInstanceWhenActionAllows() {
		RequestStub request = new RequestStub("actionName");
		request.setUserOperation("doSomething");
		request.setTargetId("emf:instance");

		when(action.shouldLockInstanceBeforeAction(request)).thenReturn(true);
		String type = "doSomething-" + REQUEST_UUID;
		LockInfo successfulLock = new LockInfo(null, "emf:user", new Date(), type, lockBy -> true);
		when(lockService.tryLock(any(), eq(type))).thenReturn(successfulLock);
		LockInfo notLocked = new LockInfo(null, null, null, null, lockBy -> false);
		when(lockService.tryUnlock(any())).thenReturn(notLocked);

		actions.callSlowAction(request);

		verify(lockService).tryLock(any(), anyString());
		verify(nonTransactionalActionExecutor).execute(action, request);
		verify(lockService).tryUnlock(any());
	}

	@Test
	public void callAction_shouldLockInstanceEventIfAlreadyLockedButUnlockedInTime() {
		ActionRequest request = new RequestStub("actionName");
		request.setUserOperation("doSomething");
		request.setTargetId("emf:instance");

		when(action.shouldLockInstanceBeforeAction(request)).thenReturn(true);
		String type = "doSomething-" + REQUEST_UUID;
		LockInfo successfulLock = new LockInfo(null, "emf:user", new Date(), type, lockBy -> true);
		LockInfo unsuccessfulLock = new LockInfo(null, "emf:user2", new Date(), "somethingElse", lockBy -> false);
		when(lockService.tryLock(any(), eq(type))).thenReturn(unsuccessfulLock, unsuccessfulLock, successfulLock);
		LockInfo notLocked = new LockInfo(null, null, null, null, lockBy -> false);
		when(lockService.tryUnlock(any())).thenReturn(notLocked);

		actions.callAction(request);

		verify(lockService, times(3)).tryLock(any(), anyString());
		verify(transactionalActionExecutor).execute(action, request);
		verify(lockService).tryUnlock(any());
	}

	@Test(expected = RollbackedRuntimeException.class)
	public void callAction_shouldFailIfFailToAcquireLockInTime_withReference() {
		ActionRequest request = new RequestStub("actionName");
		request.setUserOperation("doSomething");
		request.setTargetId("emf:instance");
		request.setTargetReference(buildInstance(request.getTargetId().toString()).toReference());
		when(action.shouldLockInstanceBeforeAction(request)).thenReturn(true);
		String type = "doSomething-" + REQUEST_UUID;
		LockInfo unsuccessfulLock = new LockInfo(null, "emf:user2", new Date(), "somethingElse", lockBy -> false);
		when(lockService.tryLock(any(), eq(type))).thenReturn(unsuccessfulLock);

		try {
			actions.callAction(request);
		} finally {
			verify(lockService, times(3)).tryLock(any(), anyString());
			verify(transactionalActionExecutor, never()).execute(action, request);
			verify(lockService, never()).tryUnlock(any());
		}
	}

	private static class RequestStub extends ActionRequest {
		private static final long serialVersionUID = -7835216083328537817L;
		private final String operation;

		RequestStub(String operation) {
			this.operation = operation;
		}

		@Override
		public String getOperation() {
			return operation;
		}
	}
}