package com.sirma.itt.seip.configuration.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.security.exception.SecurityException;

/**
 * Test for {@link ConfigurationManagementSecurityDecorator}
 *
 * @author BBonev
 */
public class ConfigurationManagementSecurityDecoratorTest {

	@InjectMocks
	private ConfigurationManagementSecurityDecoratorImpl decorator;

	@Mock
	private ConfigurationManagement delegate;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private SecurityContextManager contextManager;

	@Before
	@SuppressWarnings("boxing")
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		Configuration c1 = mock(Configuration.class);
		when(c1.isSensitive()).thenReturn(Boolean.TRUE);
		Configuration c2 = mock(Configuration.class);
		when(c2.isSensitive()).thenReturn(Boolean.FALSE);

		when(delegate.getAllConfigurations()).thenReturn(Arrays.asList(c1, c2));
		when(delegate.getCurrentTenantConfigurations()).thenReturn(Arrays.asList(c1, c2));
		when(delegate.getSystemConfigurations()).thenReturn(Arrays.asList(c1, c2));
	}

	@Test
	public void testGetAllConfigurations() {
		setAuthenticatedAsSuperAdmin();
		Collection<Configuration> collection = decorator.getAllConfigurations();
		assertNotNull(collection);
		assertEquals(2, collection.size());
	}

	@Test(expected = SecurityException.class)
	public void testGetAllConfigurations_notSuperAdmin() {
		setAuthenticatedAsAdmin();
		decorator.getAllConfigurations();
	}

	@Test
	public void testGetSystemConfigurations() {
		setAuthenticatedAsSuperAdmin();
		Collection<Configuration> collection = decorator.getSystemConfigurations();
		assertNotNull(collection);
		assertEquals(2, collection.size());
	}

	@Test(expected = SecurityException.class)
	public void testGetSystemConfigurations_notSuperAdmin() {
		setAuthenticatedAsAdmin();
		decorator.getSystemConfigurations();
	}

	@Test
	public void testGetCurrentTenantConfigurations_asAdmin() {
		setAuthenticatedAsAdmin();
		Collection<Configuration> collection = decorator.getCurrentTenantConfigurations();
		assertNotNull(collection);
		assertEquals(2, collection.size());
	}

	@Test
	public void testGetCurrentTenantConfigurations_asNonAdmin() {
		Collection<Configuration> collection = decorator.getCurrentTenantConfigurations();
		assertNotNull(collection);
		assertEquals(1, collection.size());
	}

	@Test
	public void testUpdateSystemConfiguration() {
		setAuthenticatedAsSuperAdmin();
		decorator.updateSystemConfiguration(mock(Configuration.class));
		verify(delegate).updateSystemConfiguration(any());
	}

	@Test(expected = SecurityException.class)
	public void testUpdateSystemConfiguration_noSystemAdmin() {
		setAuthenticatedAsAdmin();
		decorator.updateSystemConfiguration(mock(Configuration.class));
	}

	@Test
	public void testUpdateSystemConfigurations() {
		setAuthenticatedAsSuperAdmin();
		decorator.updateSystemConfigurations(Arrays.asList(mock(Configuration.class)));
		verify(delegate).updateSystemConfigurations(anyCollection());
	}

	@Test(expected = SecurityException.class)
	public void testUpdateSystemConfigurations_notSystemAdmin() {
		decorator.updateSystemConfigurations(Arrays.asList(mock(Configuration.class)));
	}

	@Test
	public void testUpdateConfiguration() {
		setAuthenticatedAsAdmin();
		decorator.updateConfiguration(mock(Configuration.class));
		verify(delegate).updateConfiguration(any());
	}

	@Test(expected = SecurityException.class)
	public void testUpdateConfiguration_notAdmin() {
		decorator.updateConfiguration(mock(Configuration.class));
		verify(delegate).updateConfiguration(any());
	}

	@Test
	public void testUpdateConfigurations() {
		setAuthenticatedAsAdmin();
		decorator.updateConfigurations(Arrays.asList(mock(Configuration.class)));
		verify(delegate).updateConfigurations(anyCollection());
	}

	@Test(expected = SecurityException.class)
	public void testUpdateConfigurations_noAdmin() {
		decorator.updateConfigurations(Arrays.asList(mock(Configuration.class)));
		verify(delegate).updateConfigurations(anyCollection());
	}

	@Test
	public void testRemoveSystemConfiguration() {
		setAuthenticatedAsSuperAdmin();
		decorator.removeSystemConfiguration("configKey");
		verify(delegate).removeSystemConfiguration(anyString());
	}

	@Test(expected = SecurityException.class)
	public void testRemoveSystemConfiguration_notSystemAdmin() {
		decorator.removeSystemConfiguration("configKey");
	}

	@Test
	public void testRemoveConfiguration_asSuperAdmin() {
		setAuthenticatedAsSuperAdmin();
		decorator.removeConfiguration("configKey");
		verify(delegate).removeConfiguration(anyString());
	}

	@Test
	public void testRemoveConfiguration_asAdmin() {
		setAuthenticatedAsAdmin();
		decorator.removeConfiguration("configKey");
		verify(delegate).removeConfiguration(anyString());
	}

	@Test(expected = SecurityException.class)
	public void testRemoveConfiguration_notAdmin() {
		decorator.removeConfiguration("configKey");
	}

	@Test
	public void testAddConfigurations_asSystem() {
		setAuthenticatedAsSystem();
		decorator.addConfigurations(Arrays.asList(mock(Configuration.class)));
		verify(delegate).addConfigurations(anyCollection());
	}

	@Test
	public void testAddConfigurations_asSuperAdmin() {
		setAuthenticatedAsSuperAdmin();
		decorator.addConfigurations(Arrays.asList(mock(Configuration.class)));
		verify(delegate).addConfigurations(anyCollection());
	}

	@Test
	public void testAddConfigurations_asAdmin() {
		setAuthenticatedAsAdmin();
		decorator.addConfigurations(Arrays.asList(mock(Configuration.class)));
		verify(delegate).addConfigurations(anyCollection());
	}

	@Test(expected = SecurityException.class)
	public void testAddConfigurations_notAdmin() {
		decorator.addConfigurations(Arrays.asList(mock(Configuration.class)));
	}

	private void setAuthenticatedAsSystem() {
		when(contextManager.isCurrentUserSystem()).thenReturn(Boolean.TRUE);
	}

	private void setAuthenticatedAsSuperAdmin() {
		when(securityContext.isSystemTenant()).thenReturn(Boolean.TRUE);
		when(contextManager.isCurrentUserSuperAdmin()).thenReturn(Boolean.TRUE);
	}

	private void setAuthenticatedAsAdmin() {
		when(contextManager.isAuthenticatedAsAdmin()).thenReturn(Boolean.TRUE);
	}

	/**
	 * Dummy implementation of the decorator to be able to test it
	 *
	 * @author BBonev
	 */
	static class ConfigurationManagementSecurityDecoratorImpl extends ConfigurationManagementSecurityDecorator {

		@Override
		public void removeAllConfigurations() {
			// nothing to do
		}
		
	}
}