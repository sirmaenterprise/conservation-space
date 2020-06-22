package com.sirmaenterprise.sep.jms.security;

import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.UserStore;
import com.sirma.itt.seip.security.authentication.AuthenticationContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

/**
 * Test for {@link SecurityContextManager}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 22/05/2017
 */
public class JmsSecurityAuthenticatorTest {

	@InjectMocks
	private JmsSecurityAuthenticator authenticator;
	@Spy
	private SecurityContextManager securityContextManager = new SecurityContextManagerFake();
	@Mock
	private UserStore userStore;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(userStore.loadBySystemId(eq("emf:user-tenant.com"))).thenReturn(mock(User.class));
	}

	@Test
	public void authenticate() throws Exception {
		JmsAuthenticationContext context = new JmsAuthenticationContext();
		context.setAuthenticatedUser("emf:user-tenant.com");
		context.setTenantId("tenant.com");
		context.setRequestId("requestId");

		User user = authenticator.authenticate(context);
		Assert.assertNotNull(user);
	}

	@Test
	public void authenticate_shouldReturnNull_onMissingTenant() throws Exception {
		JmsAuthenticationContext context = new JmsAuthenticationContext();
		context.setAuthenticatedUser("emf:user-tenant.com");

		User user = authenticator.authenticate(context);
		assertNull(user);
	}

	@Test
	public void authenticate_shouldReturnNull_onMissingUser() throws Exception {
		JmsAuthenticationContext context = new JmsAuthenticationContext();
		context.setTenantId("tenant.com");
		context.setRequestId("requestId");

		User user = authenticator.authenticate(context);
		assertNull(user);
	}

	@Test
	public void authenticate_shouldReturnNull_onMissingProperties() throws Exception {
		JmsAuthenticationContext context = new JmsAuthenticationContext();

		User user = authenticator.authenticate(context);
		assertNull(user);
	}

	@Test
	public void authenticateShouldIgnoreInvalidContexts() throws Exception {
		User user = authenticator.authenticate(AuthenticationContext.create(new HashMap<>()));
		assertNull(user);
	}
}
