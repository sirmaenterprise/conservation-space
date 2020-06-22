package com.sirma.itt.seip.tenant.management;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.runtime.Component;
import com.sirma.itt.seip.runtime.boot.StartupException;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.context.DefaultTenantManager;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.context.TenantManager;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Test for {@link TenantOperationObserver}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 04/01/2018
 */
public class TenantOperationObserverTest {
	@InjectMocks
	private TenantOperationObserver operationObserver;

	@Mock
	private TenantManager tenantManager = new DefaultTenantManager();
	@Mock
	private TenantOperationCollector collector;
	@Spy
	private TransactionSupport transactionSupport = new TransactionSupportFake();
	@Spy
	private SecurityContextManager securityContextManager = new SecurityContextManagerFake();

	@Mock
	private Component componentOnAdd1;
	@Mock
	private Component componentOnAdd2;
	@Mock
	private Component componentOnRemove1;
	@Mock
	private Component componentOnRemove2;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		when(collector.getTenantAddedComponents()).thenReturn(Arrays.asList(componentOnAdd1, componentOnAdd2));
		when(collector.getTenantRemovedComponets()).thenReturn(Arrays.asList(componentOnRemove1, componentOnRemove2));

		when(componentOnAdd1.getName()).thenReturn("Component.add.1");
		when(componentOnAdd2.getName()).thenReturn("Component.add.2");
		when(componentOnRemove1.getName()).thenReturn("Component.remove.1");
		when(componentOnRemove2.getName()).thenReturn("Component.remove.2");
	}

	@Test
	public void init_shouldRegisterAddComponents() throws Exception {
		operationObserver.init();

		ArgumentCaptor<Consumer<TenantInfo>> captor = ArgumentCaptor.forClass(Consumer.class);
		verify(tenantManager).addOnTenantAddedListener(captor.capture());

		captor.getValue().accept(new TenantInfo("tenant.id"));

		verify(componentOnAdd1).execute();
		verify(componentOnAdd2).execute();
	}

	@Test
	public void init_shouldRegisterRemoveComponents() throws Exception {
		operationObserver.init();

		ArgumentCaptor<Consumer<TenantInfo>> captor = ArgumentCaptor.forClass(Consumer.class);
		verify(tenantManager).addOnTenantRemoveListener(captor.capture());

		captor.getValue().accept(new TenantInfo("tenant.id"));

		verify(componentOnRemove1).execute();
		verify(componentOnRemove2).execute();
	}

	@Test(expected = RollbackedRuntimeException.class)
	public void componentExecutionShouldNotStopAtException() throws Exception {
		operationObserver.init();

		ArgumentCaptor<Consumer<TenantInfo>> captor = ArgumentCaptor.forClass(Consumer.class);
		verify(tenantManager).addOnTenantAddedListener(captor.capture());

		doThrow(new StartupException(new NullPointerException())).when(componentOnAdd1).execute();

		try {
			captor.getValue().accept(new TenantInfo("tenant.id"));
		} finally {
			verify(componentOnAdd1).execute();
			verify(componentOnAdd2).execute();
		}
	}

}
