/**
 *
 */
package com.sirma.itt.seip.security.configuration;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.configuration.convert.GroupConverterContext;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.UserStore;
import com.sirma.itt.seip.security.authentication.Authenticator;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * @author BBonev
 */

public class SecurityConfigurationImplTest {
	@Mock
	GroupConverterContext context;
	@Mock
	UserStore userStore;
	@Mock
	SecurityContext securityContext;
	@Mock
	Authenticator authenticator;

	@InjectMocks
	SecurityConfigurationImpl configuration;

	@BeforeMethod
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(userStore.wrap(any())).then((a) -> a.getArgumentAt(0, User.class));
	}

	@Test
	public void testBuildAdminUser() {

		when(context.get(SecurityConfigurationImpl.ADMIN_NAME)).thenReturn("admin");
		testInternal();
		// test with full id
		when(context.get(SecurityConfigurationImpl.ADMIN_NAME)).thenReturn("admin@test");
	}

	private void testInternal() {
		when(context.get(SecurityConfigurationImpl.ADMIN_PWD)).thenReturn("pass");
		when(securityContext.getCurrentTenantId()).thenReturn("test");
		when(authenticator.authenticate(any(User.class))).thenReturn("ticket");

		User user = SecurityConfigurationImpl.buildAdminUser(context, userStore, securityContext, authenticator);
		assertNotNull(user);
		assertEquals(user.getTicket(), "ticket");
		assertEquals(user.getIdentityId(), "admin@test");
		assertEquals(user.getTenantId(), "test");
	}
}
