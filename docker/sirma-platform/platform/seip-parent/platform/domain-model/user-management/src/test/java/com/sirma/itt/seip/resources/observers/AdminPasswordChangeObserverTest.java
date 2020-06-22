package com.sirma.itt.seip.resources.observers;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.resources.event.UserPasswordChangeEvent;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;

/**
 * Test for {@link AdminPasswordChangeObserver}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 11/10/2017
 */
public class AdminPasswordChangeObserverTest {
	@InjectMocks
	private AdminPasswordChangeObserver passwordChangeObserver;
	@Mock
	private ConfigurationManagement configurationManagement;
	@Mock
	private SecurityConfiguration securityConfiguration;
	@Mock
	private SecurityContext securityContext;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		ConfigurationPropertyMock<String> passConfig = new ConfigurationPropertyMock<>();
		passConfig.setName("admin.pass");
		when(securityConfiguration.getAdminUserPassword()).thenReturn(passConfig);
		when(securityConfiguration.getAdminUserName()).thenReturn(new ConfigurationPropertyMock<>("admin@tenant.com"));
		when(securityContext.getCurrentTenantId()).thenReturn("tenant.com");
	}

	@Test
	public void onPasswordChange_ifNonAdminShouldDoNothing() throws Exception {
		passwordChangeObserver.onPasswordChange(new UserPasswordChangeEvent("someUser@tenant.com", "somePass"));
		verify(configurationManagement, never()).updateConfiguration(any());
	}

	@Test
	public void onPasswordChange_shouldUpdateAdminPassConfigIfAdminUser() throws Exception {
		passwordChangeObserver.onPasswordChange(new UserPasswordChangeEvent("admin@tenant.com", "adminPass"));
		verify(configurationManagement).updateConfiguration(argThat(CustomMatcher.ofPredicate(
				c -> "admin.pass".equals(c.getConfigurationKey())
						&& "tenant.com".equals(c.getTenantId())
						&& "adminPass".equals(c.getValue()))));
	}
}
