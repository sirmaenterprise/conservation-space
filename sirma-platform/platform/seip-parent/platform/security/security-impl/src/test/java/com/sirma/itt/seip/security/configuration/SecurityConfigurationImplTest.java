package com.sirma.itt.seip.security.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.configuration.convert.GroupConverterContext;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.UserStore;
import com.sirma.itt.seip.security.authentication.Authenticator;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * @author BBonev
 */
public class SecurityConfigurationImplTest {

	private static final String TENANT_ID = "test";

	@Mock
	private GroupConverterContext context;
	@Mock
	private UserStore userStore;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private Authenticator authenticator;

	@InjectMocks
	private SecurityConfigurationImpl configuration;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(userStore.wrap(any())).then((a) -> a.getArgumentAt(0, User.class));
		when(securityContext.getCurrentTenantId()).thenReturn(TENANT_ID);
		when(authenticator.authenticate(any(User.class))).thenReturn("ticket");
		when(context.get(SecurityConfigurationImpl.ADMIN_PWD)).thenReturn("pass");
	}

	@Test
	public void testBuildAdminUser() {
		when(context.get(SecurityConfigurationImpl.ADMIN_NAME)).thenReturn("admin");
		verifyAdminUser();
		// test with full id
		when(context.get(SecurityConfigurationImpl.ADMIN_NAME)).thenReturn("admin@test");
		verifyAdminUser();
	}

	private void verifyAdminUser() {
		User user = SecurityConfigurationImpl.buildAdminUser(context, userStore, securityContext, authenticator);
		assertNotNull(user);
		assertEquals("ticket", user.getTicket());
		assertEquals("admin@test", user.getIdentityId());
		assertEquals(TENANT_ID, user.getTenantId());
	}

	@Test
	public void should_CorrectlyBuildSystemUser() {
		when(context.get(SecurityConfigurationImpl.SYSTEM_USER_NAME)).thenReturn("system");

		User systemUser = SecurityConfigurationImpl.buildSystemUser(context, userStore, securityContext, authenticator);
		assertNotNull(systemUser);
		assertEquals(TENANT_ID, systemUser.getTenantId());
		assertEquals(SecurityContext.SYSTEM_USER_NAME + "@" + TENANT_ID, systemUser.getIdentityId());
	}

}
