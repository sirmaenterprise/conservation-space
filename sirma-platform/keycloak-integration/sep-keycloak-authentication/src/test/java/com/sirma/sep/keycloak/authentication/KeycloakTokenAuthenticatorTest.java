package com.sirma.sep.keycloak.authentication;

import static com.sirma.sep.keycloak.authentication.KeycloakAuthTestUtil.TENANT_ID;
import static com.sirma.sep.keycloak.authentication.KeycloakAuthTestUtil.generateToken;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;

import org.junit.Before;
import org.junit.Test;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.common.util.Time;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.security.UserStore;
import com.sirma.itt.seip.security.authentication.AuthenticationContext;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.security.exception.AuthenticationException;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;
import com.sirma.sep.keycloak.tenant.KeycloakDeploymentRetriever;

/**
 * Tests for {@link KeycloakTokenAuthenticator}.
 *
 * @author smustafov
 */
public class KeycloakTokenAuthenticatorTest {

	@InjectMocks
	private KeycloakTokenAuthenticator authenticator;

	@Mock
	private KeycloakDeploymentRetriever deploymentRetriever;

	@Spy
	private SecurityContextManager securityContextManager = new SecurityContextManagerFake();

	@Mock
	private UserStore userStore;

	private EmfUser user;

	@Before
	public void beforeEach() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void should_SetUserTicket_When_TokenValidationSuccessful() throws Exception {
		Map<String, String> authRequest = mockAuthRequest(
				KeycloakTokenAuthenticator.AUTHORIZATION_METHOD + " " + generateToken("regularuser"), "regularuser");

		authenticator.authenticate(AuthenticationContext.create(authRequest));

		verify(userStore).setUserTicket(any(), any());
	}

	@Test
	public void should_SetUserTicket_ForSystemTenant() throws Exception {
		String token = generateToken("master", "systemadmin");
		Map<String, String> authRequest = mockAuthRequest(KeycloakTokenAuthenticator.AUTHORIZATION_METHOD + " " + token,
				"systemadmin", SecurityContext.SYSTEM_TENANT, "master");

		user = new EmfUser();
		when(userStore.loadByIdentityId("systemadmin", SecurityContext.SYSTEM_TENANT)).thenReturn(user);

		authenticator.authenticate(AuthenticationContext.create(authRequest));

		verify(userStore).setUserTicket(any(), any());
	}

	@Test(expected = AuthenticationException.class)
	public void should_ThrowException_When_TokenValidationFailed() throws Exception {
		Map<String, String> authRequest = mockAuthRequest(
				KeycloakTokenAuthenticator.AUTHORIZATION_METHOD + " " + generateToken("regularuser",
						Time.currentTime() - 1000), "regularuser");

		authenticator.authenticate(AuthenticationContext.create(authRequest));
	}

	@Test(expected = AuthenticationException.class)
	public void should_ThrowException_When_UserNotFound() throws Exception {
		Map<String, String> authRequest = mockAuthRequest(
				KeycloakTokenAuthenticator.AUTHORIZATION_METHOD + " " + generateToken("regularuser"), null);
		when(userStore.loadByIdentityId(anyString(), anyString())).thenReturn(null);

		authenticator.authenticate(AuthenticationContext.create(authRequest));
	}

	@Test
	public void should_DoNothing_When_AuthMethodNotKeycloak() throws Exception {
		Map<String, String> authRequest = mockAuthRequest("Jwt jwt", "regularuser");

		authenticator.authenticate(AuthenticationContext.create(authRequest));

		verify(userStore, never()).setUserTicket(any(), any());
	}

	@Test(expected = AuthenticationException.class)
	public void should_ThrowException_When_NoTenantPropertyInHeader() throws Exception {
		String token = generateToken(null, "regularuser", Time.currentTime());
		Map<String, String> authRequest = mockAuthRequest(KeycloakTokenAuthenticator.AUTHORIZATION_METHOD + " " + token,
				"regularuser");

		authenticator.authenticate(AuthenticationContext.create(authRequest));
	}

	@Test
	public void should_DoNothing_When_NoAuthorizationHeader() throws Exception {
		Map<String, String> authRequest = mockAuthRequest(null, "regularuser");

		authenticator.authenticate(AuthenticationContext.create(authRequest));

		verify(userStore, never()).setUserTicket(any(), any());
	}

	@Test
	public void user_Should_ReturnNull() {
		assertNull(authenticator.authenticate(user));
	}

	private Map<String, String> mockAuthRequest(String authorization, String username) {
		return mockAuthRequest(authorization, username, TENANT_ID, TENANT_ID);
	}

	private Map<String, String> mockAuthRequest(String authorization, String username, String tenant, String realm) {
		Map<String, String> properties = new HashMap<>();
		properties.put(HttpHeaders.AUTHORIZATION, authorization);

		KeycloakDeployment deployment = KeycloakAuthTestUtil.getMockedDeployment(realm);
		when(deploymentRetriever.getDeployment(realm)).thenReturn(deployment);

		user = new EmfUser();
		when(userStore.loadByIdentityId(username + "@" + tenant, tenant)).thenReturn(user);

		return properties;
	}

}
