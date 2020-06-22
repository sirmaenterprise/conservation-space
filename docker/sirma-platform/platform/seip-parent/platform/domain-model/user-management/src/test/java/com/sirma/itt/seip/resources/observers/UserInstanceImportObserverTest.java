package com.sirma.itt.seip.resources.observers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.instance.event.BeforeInstanceImportEvent;
import com.sirma.itt.seip.resources.ResourceProperties;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Tests for {@link UserInstanceImportObserver}.
 *
 * @author smustafov
 */
public class UserInstanceImportObserverTest {

	private static final String TENANT_ID = "tenant.com";

	@InjectMocks
	private UserInstanceImportObserver observer;

	@Mock
	private SecurityContext securityContext;

	@Before
	public void beforeEach() {
		initMocks(this);

		when(securityContext.getCurrentTenantId()).thenReturn(TENANT_ID);
	}

	@Test
	public void beforeImport_Should_DoNothing_When_InstanceNull() {
		observer.onBeforeInstanceImport(new BeforeInstanceImportEvent(null));
	}

	@Test
	public void beforeImport_Should_DoNothing_When_InstanceIsNotUser() {
		EmfInstance instance = createInstance("emf:instance-id", false);

		observer.onBeforeInstanceImport(new BeforeInstanceImportEvent(instance));

		assertNull(instance.get(ResourceProperties.USER_ID));
	}

	@Test
	public void beforeImport_Should_DoNothing_When_DbIdHasTenantId() {
		EmfInstance instance = createInstance("emf:user-" + TENANT_ID, true);

		observer.onBeforeInstanceImport(new BeforeInstanceImportEvent(instance));

		assertNull(instance.get(ResourceProperties.USER_ID));
	}

	@Test
	public void beforeImport_Should_AppendTenantId_When_ImportingNewUser() {
		EmfInstance instance = createInstance("emf:instanceId", true);
		instance.add(ResourceProperties.USER_ID, "john");

		observer.onBeforeInstanceImport(new BeforeInstanceImportEvent(instance));

		assertEquals("john@" + TENANT_ID, instance.get(ResourceProperties.USER_ID));
	}

	@Test(expected = IllegalArgumentException.class)
	public void beforeImport_Should_ThrowException_When_NewUserHasDifferentTenant() {
		EmfInstance instance = createInstance("emf:instanceId", true);
		instance.add(ResourceProperties.USER_ID, "john@abc.com");

		observer.onBeforeInstanceImport(new BeforeInstanceImportEvent(instance));
	}

	private static EmfInstance createInstance(String id, boolean isUserType) {
		InstanceType instanceType = mock(InstanceType.class);
		when(instanceType.is(ObjectTypes.USER)).thenReturn(isUserType);

		EmfInstance instance = new EmfInstance(id);
		instance.setType(instanceType);

		return instance;
	}

}
