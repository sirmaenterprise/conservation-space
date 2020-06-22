package com.sirma.itt.seip.permissions.role;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Collection;

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.permissions.PermissionService;
import com.sirma.itt.seip.permissions.role.PermissionsChange.PermissionsChangeBuilder;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * Tests for {@link TransactionalPermissionChangesImpl}.
 *
 * @author smustafov
 */
public class TransactionalPermissionChangesImplTest {

	@Mock
	private PermissionService permissionService;

	@Mock
	private TransactionManager transactionManager;
	@Mock
	private Transaction transaction;

	@InjectMocks
	private TransactionalPermissionChangesImpl changes;

	@Before
	public void beforeEach() throws SystemException {
		initMocks(this);
		when(transactionManager.getTransaction()).thenReturn(transaction);
	}

	@Test
	public void shouldRegisterForAutomaticFlushOnCreation() throws RollbackException, SystemException {
		changes.register();

		verify(transaction).registerSynchronization(changes);
	}

	@Test(expected = IllegalStateException.class)
	public void shouldFailToRegisterForAutomaticFlushOnCreation_ifOutsideTxContext() throws RollbackException, SystemException {
		when(transactionManager.getTransaction()).thenReturn(null);

		changes.register();

		// should not reach this
		verify(transaction, never()).registerSynchronization(changes);
	}

	@Test(expected = IllegalStateException.class)
	public void shouldFailToRegisterForAutomaticFlushOnCreation_ifTxNotActive() throws RollbackException, SystemException {
		when(transaction.getStatus()).thenReturn(Status.STATUS_COMMITTING);

		changes.register();

		// should not reach this
		verify(transaction, never()).registerSynchronization(changes);
	}

	@Test
	public void should_ReturnNonNullBuilder() {
		PermissionsChangeBuilder builder = changes.builder(InstanceReferenceMock.createGeneric("emf:id"));
		assertNotNull(builder);
	}

	@Test
	public void should_SavePermissionsForInstanceWithChanges() throws Exception {
		PermissionsChangeBuilder builder = changes.builder(InstanceReferenceMock.createGeneric("emf:instance-id"));
		builder.addRoleAssignmentChange("regularuser", "consumer");

		changes.beforeCompletion();

		ArgumentCaptor<Collection<PermissionsChange>> argCaptor = ArgumentCaptor.forClass(Collection.class);

		verify(permissionService).setPermissions(any(InstanceReference.class), argCaptor.capture());

		Collection<PermissionsChange> permissionChanges = argCaptor.getValue();
		assertEquals(1, permissionChanges.size());

		changes.afterCompletion(Status.STATUS_COMMITTED);
	}

	@Test(expected = IllegalStateException.class)
	public void shouldNotAllow_Modifications_afterTxFlush() throws Exception {
		PermissionsChangeBuilder builder = changes.builder(InstanceReferenceMock.createGeneric("emf:instance-id"));
		builder.addRoleAssignmentChange("regularuser", "consumer");

		changes.beforeCompletion();
		changes.afterCompletion(Status.STATUS_COMMITTED);

		builder = changes.builder(InstanceReferenceMock.createGeneric("emf:instance-id"));
	}

}
