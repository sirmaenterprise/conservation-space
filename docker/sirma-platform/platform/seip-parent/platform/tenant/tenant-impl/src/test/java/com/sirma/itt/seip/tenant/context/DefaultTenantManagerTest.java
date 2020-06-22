package com.sirma.itt.seip.tenant.context;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.tenant.exception.TenantOperationException;
import com.sirma.itt.seip.tenant.exception.TenantValidationException;

/**
 * Test for {@link DefaultTenantManager}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 04/01/2018
 */
public class DefaultTenantManagerTest {
	@InjectMocks
	private DefaultTenantManager tenantManager;

	@Mock
	private DbDao dbDao;
	@Mock
	private DatabaseIdManager idManager;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	private void mockTenantExists() {
		when(dbDao.fetchWithNamed(eq(TenantEntity.QUERY_CHECK_TENANT_EXISTS_BY_ID_KEY), anyList())).thenReturn(
				Collections.singletonList("tenantId"));
	}

	private void mockTenantDoesExists() {
		when(dbDao.fetchWithNamed(eq(TenantEntity.QUERY_CHECK_TENANT_EXISTS_BY_ID_KEY), anyList())).thenReturn(
				Collections.emptyList());
	}

	@Test(expected = TenantValidationException.class)
	public void finishTenantActivation_shouldFailIfTenantDoesNotExists() throws Exception {
		mockTenantDoesExists();

		tenantManager.finishTenantActivation("tenantId");
	}

	@Test(expected = TenantOperationException.class)
	public void finishTenantActivation_shouldNotifyAllObserversEventOnException() throws Exception {
		mockTenantExists();

		AtomicInteger integer = new AtomicInteger();
		tenantManager.addOnTenantAddedListener(t -> { throw new RuntimeException();});
		tenantManager.addOnTenantAddedListener(t -> integer.incrementAndGet());

		try {
			tenantManager.finishTenantActivation("tenantId");
		} finally {
			assertEquals(1, integer.get());
		}
	}

	@Test
	public void finishTenantActivation_shouldNotFailIfAllObserversBehaved() throws Exception {
		mockTenantExists();

		AtomicInteger integer = new AtomicInteger();
		tenantManager.addOnTenantAddedListener(t -> integer.incrementAndGet());
		tenantManager.addOnTenantAddedListener(t -> integer.incrementAndGet());

		tenantManager.finishTenantActivation("tenantId");
		assertEquals(2, integer.get());
	}

	@Test(expected = TenantOperationException.class)
	public void callTenantRemovedListeners_shouldNotifyAllObserversEventOnException() throws Exception {
		AtomicInteger integer = new AtomicInteger();
		tenantManager.addOnTenantRemoveListener(t -> { throw new RuntimeException();});
		tenantManager.addOnTenantRemoveListener(t -> integer.incrementAndGet());

		try {
			tenantManager.callTenantRemovedListeners("tenantId");
		} finally {
			assertEquals(1, integer.get());
		}
	}

	@Test
	public void callTenantRemovedListeners_shouldNotFailIfAllObserversBehaved() throws Exception {
		mockTenantExists();

		AtomicInteger integer = new AtomicInteger();
		tenantManager.addOnTenantRemoveListener(t -> integer.incrementAndGet());
		tenantManager.addOnTenantRemoveListener(t -> integer.incrementAndGet());

		tenantManager.callTenantRemovedListeners("tenantId");
		assertEquals(2, integer.get());
	}

}
