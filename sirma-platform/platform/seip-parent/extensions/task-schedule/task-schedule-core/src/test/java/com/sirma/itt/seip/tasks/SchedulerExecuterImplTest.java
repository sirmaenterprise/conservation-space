package com.sirma.itt.seip.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.lang.annotation.Annotation;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.concurrent.event.BaseCallableEvent;
import com.sirma.itt.seip.event.EmfEvent;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.security.UserStore;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Test for {@link SchedulerExecuterImpl}
 *
 * @author BBonev
 */
public class SchedulerExecuterImplTest {
	@InjectMocks
	private SchedulerExecuterImpl executer;

	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();
	@Mock
	private EventService eventService;
	@Mock
	private SchedulerService schedulerService;
	@Spy
	private InstanceProxyMock<SchedulerService> schedulerServiceInstance = new InstanceProxyMock<>();
	@Mock
	private SchedulerAction action;
	@Spy
	private SecurityContextManager securityContextManager = new SecurityContextManagerFake();
	@Mock
	private UserStore userStore;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		schedulerServiceInstance.set(schedulerService);
		doAnswer(a -> {
			EmfEvent event = a.getArgumentAt(0, EmfEvent.class);
			if (event instanceof BaseCallableEvent) {
				((BaseCallableEvent) event).call();
			}
			return null;
		}).when(eventService).fire(any(), any());

		when(userStore.loadBySystemId(anyString())).then(a -> {
			EmfUser user = new EmfUser();
			user.setId(a.getArgumentAt(0, String.class));
			return user;
		});
	}

	@Test(expected = EmfRuntimeException.class)
	public void execute_noConfiguration() throws Exception {
		executer.execute(createEntry(null));
	}

	@Test(expected = EmfRuntimeException.class)
	public void execute_noEntity() throws Exception {
		executer.execute(null);
	}

	@Test(expected = EmfRuntimeException.class)
	public void executeImmediate_noEntity() throws Exception {
		executer.executeImmediate(null);
	}

	@Test
	public void execute_noAction() throws Exception {
		SchedulerConfiguration configuration = new DefaultSchedulerConfiguration();
		configuration.setSynchronous(true).setPersistent(false).setTransactionMode(TransactionMode.NOT_SUPPORTED);

		SchedulerEntry entry = createEntry(configuration);
		entry.setAction(null);

		Future<SchedulerEntryStatus> future = executer.execute(entry);
		assertNotNull(future);
		assertEquals(SchedulerEntryStatus.FAILED, future.get());
		verify(schedulerService, never()).save(any());
	}

	@Test(expected = EmfRuntimeException.class)
	public void executeImmediate_noAction() throws Exception {
		SchedulerEntry entry = createEntry(null);
		entry.setAction(null);

		executer.executeImmediate(entry);
	}

	@Test
	public void executeSync_noTx_noSave() throws Exception {
		SchedulerConfiguration configuration = new DefaultSchedulerConfiguration();
		configuration.setSynchronous(true).setPersistent(false).setTransactionMode(TransactionMode.NOT_SUPPORTED);

		Future<SchedulerEntryStatus> future = executer.execute(createEntry(configuration));
		assertNotNull(future);
		assertEquals(SchedulerEntryStatus.COMPLETED, future.get());
		verify(action).execute(any());
		verify(schedulerService, never()).save(any());
	}

	@Test
	public void executeSync_Tx_Save() throws Exception {
		SchedulerConfiguration configuration = new DefaultSchedulerConfiguration();
		configuration.setSynchronous(true).setPersistent(true).setTransactionMode(TransactionMode.REQUIRED);

		SchedulerEntry schedulerEntry = createEntry(configuration);
		Future<SchedulerEntryStatus> future = executer.execute(schedulerEntry);
		assertNotNull(future);
		assertEquals(SchedulerEntryStatus.COMPLETED, future.get());
		verify(action).execute(any());
		verify(schedulerService).save(schedulerEntry);
	}

	@Test
	public void should_executeSync_SystemTenant() throws Exception {
		SchedulerConfiguration configuration = new DefaultSchedulerConfiguration();
		configuration.setSynchronous(true).setRunAs(RunAs.SYSTEM);

		SchedulerEntry schedulerEntry = createEntry(configuration);
		Future<SchedulerEntryStatus> future = executer.execute(schedulerEntry);
		assertNotNull(future);
		assertEquals(SchedulerEntryStatus.COMPLETED, future.get());
		verify(action).execute(any());
		verify(eventService, Mockito.times(1)).fire(Matchers.any(SchedulerExecuterEvent.class),
				Matchers.any(Annotation.class));
	}

	@Test
	public void should_executeSync_CustomUser() throws Exception {
		SchedulerConfiguration configuration = new DefaultSchedulerConfiguration();
		configuration.setSynchronous(true).setRunAs(RunAs.USER).setRunAs("emf:someUser");

		SchedulerEntry schedulerEntry = createEntry(configuration);
		Future<SchedulerEntryStatus> future = executer.execute(schedulerEntry);
		assertNotNull(future);
		assertEquals(SchedulerEntryStatus.COMPLETED, future.get());
		verify(action).execute(any());
		verify(eventService, Mockito.times(1)).fire(Matchers.any(SchedulerExecuterEvent.class),
				Matchers.any(Annotation.class));
		verify(securityContextManager).executeAsUser(any());
	}

	@Test
	public void should_executeSync_EvenIfTheCustomUserIsNotFound() throws Exception {
		reset(userStore);
		SchedulerConfiguration configuration = new DefaultSchedulerConfiguration();
		configuration.setSynchronous(true).setRunAs(RunAs.USER).setRunAs("emf:someUser");

		SchedulerEntry schedulerEntry = createEntry(configuration);
		Future<SchedulerEntryStatus> future = executer.execute(schedulerEntry);
		assertNotNull(future);
		assertEquals(SchedulerEntryStatus.COMPLETED, future.get());
		verify(action).execute(any());
		verify(eventService, Mockito.times(1)).fire(Matchers.any(SchedulerExecuterEvent.class),
				Matchers.any(Annotation.class));
		verifyZeroInteractions(securityContextManager);
	}

	@Test
	public void executeSync_NewTx_persistent_NoSave() throws Exception {
		SchedulerConfiguration configuration = new DefaultSchedulerConfiguration();
		configuration.setSynchronous(true).setPersistent(true).setTransactionMode(TransactionMode.REQUIRED);

		SchedulerEntry schedulerEntry = createEntry(configuration);
		Future<SchedulerEntryStatus> future = executer.execute(schedulerEntry, true, false);
		assertNotNull(future);
		assertEquals(SchedulerEntryStatus.COMPLETED, future.get());
		verify(action).execute(any());
		verify(schedulerService, never()).save(schedulerEntry);
	}

	@Test
	public void executeSync_NewTx_persistent_NoSave_fail() throws Exception {
		SchedulerConfiguration configuration = new DefaultSchedulerConfiguration();
		configuration.setSynchronous(true).setPersistent(true).setTransactionMode(TransactionMode.REQUIRES_NEW);

		SchedulerEntry schedulerEntry = createEntry(configuration);
		doThrow(Exception.class).when(action).execute(any());
		Future<SchedulerEntryStatus> future = executer.execute(schedulerEntry, true, false);
		assertNotNull(future);
		assertEquals(SchedulerEntryStatus.FAILED, future.get());
		verify(action).execute(any());
		verify(schedulerService, never()).save(schedulerEntry);
	}

	@Test
	public void executeAsync_Tx_persistent_NoSave() throws Exception {
		SchedulerConfiguration configuration = new DefaultSchedulerConfiguration();
		configuration.setSynchronous(false).setPersistent(true).setTransactionMode(TransactionMode.REQUIRED);

		SchedulerEntry schedulerEntry = createEntry(configuration);
		Future<SchedulerEntryStatus> future = executer.execute(schedulerEntry, true, false);
		assertNotNull(future);
		assertEquals(SchedulerEntryStatus.COMPLETED, future.get());
		verify(action).execute(any());
		verify(schedulerService, never()).save(schedulerEntry);
	}

	@Test
	public void executeAsync_NoTx_persistent_NoSave() throws Exception {
		SchedulerConfiguration configuration = new DefaultSchedulerConfiguration();
		configuration.setSynchronous(false).setPersistent(true).setTransactionMode(TransactionMode.NOT_SUPPORTED);

		SchedulerEntry schedulerEntry = createEntry(configuration);
		Future<SchedulerEntryStatus> future = executer.execute(schedulerEntry, true, false);
		assertNotNull(future);
		assertEquals(SchedulerEntryStatus.COMPLETED, future.get());
		verify(action).execute(any());
		verify(schedulerService, never()).save(schedulerEntry);
	}

	@Test
	public void executeAsync_noAsync_NoTx_persistent_NoSave() throws Exception {
		SchedulerConfiguration configuration = new DefaultSchedulerConfiguration();
		configuration.setSynchronous(false).setPersistent(true).setTransactionMode(TransactionMode.NOT_SUPPORTED);

		SchedulerEntry schedulerEntry = createEntry(configuration);
		Future<SchedulerEntryStatus> future = executer.execute(schedulerEntry, false, false);
		assertNotNull(future);
		assertEquals(SchedulerEntryStatus.COMPLETED, future.get());
		verify(action).execute(any());
		verify(schedulerService, never()).save(schedulerEntry);
	}

	@Test
	public void executeAsync_NoTx_persistent_Save() throws Exception {
		SchedulerConfiguration configuration = new DefaultSchedulerConfiguration();
		configuration.setSynchronous(false).setPersistent(true).setTransactionMode(TransactionMode.NOT_SUPPORTED);

		SchedulerEntry schedulerEntry = createEntry(configuration);
		Future<SchedulerEntryStatus> future = executer.execute(schedulerEntry);
		assertNotNull(future);
		assertEquals(SchedulerEntryStatus.COMPLETED, future.get());
		verify(action).execute(any());
		verify(schedulerService).save(schedulerEntry);
	}

	@Test
	public void executeAsync_NoTx_persistent_Save_fail() throws Exception {
		SchedulerConfiguration configuration = new DefaultSchedulerConfiguration();
		configuration.setSynchronous(false).setPersistent(true).setTransactionMode(TransactionMode.NOT_SUPPORTED);

		SchedulerEntry schedulerEntry = createEntry(configuration);
		doThrow(Exception.class).when(action).beforeExecute(any());
		Future<SchedulerEntryStatus> future = executer.execute(schedulerEntry);
		assertNotNull(future);
		assertEquals(SchedulerEntryStatus.FAILED, future.get());
		verify(action, never()).execute(any());
		verify(schedulerService).save(schedulerEntry);
	}

	private SchedulerEntry createEntry(SchedulerConfiguration configuration) {
		SchedulerEntry schedulerEntry = new SchedulerEntry();
		schedulerEntry.setConfiguration(configuration);
		schedulerEntry.setAction(action);
		schedulerEntry.setContext(new SchedulerContext());
		return schedulerEntry;
	}
}
