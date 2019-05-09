package com.sirma.sep.keycloak.authentication;

import static com.sirma.sep.keycloak.authentication.KeycloakAuthTestUtil.TENANT_ID;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.junit.Before;
import org.junit.Test;
import org.keycloak.adapters.KeycloakDeployment;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.UserStore;
import com.sirma.itt.seip.security.authentication.AuthenticationContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.security.exception.AuthenticationException;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.sep.keycloak.config.KeycloakConfiguration;
import com.sirma.sep.keycloak.tenant.KeycloakDeploymentRetriever;

/**
 * Tests for {@link KeycloakBasicAuthenticator}.
 *
 * @author smustafov
 */
public class KeycloakBasicAuthenticatorTest {

	@InjectMocks
	private KeycloakBasicAuthenticator authenticator;

	@Mock
	private KeycloakConfiguration keycloakConfiguration;

	@Spy
	private SecurityContextManager securityContextManager = new SecurityContextManagerFake();

	@Mock
	private UserStore userStore;

	@Mock
	private KeycloakDeploymentRetriever deploymentRetriever;

	@Before
	public void beforeEach() {
		MockitoAnnotations.initMocks(this);

		when(keycloakConfiguration.getKeycloakAddress()).thenReturn(new ConfigurationPropertyMock<>("http://localhost:8090/auth"));
	}

	@Test
	public void should_DoNothing_When_NoAuthHeader() {
		authenticator.authenticate(AuthenticationContext.createEmpty());

		verify(userStore, never()).loadByIdentityId(anyString(), anyString());
	}

	@Test
	public void should_DoNothing_When_NotBasicAuthentication() {
		Map<String, String> properties = new HashMap<>();
		properties.put(HttpHeaders.AUTHORIZATION, "Bearer jwt");

		authenticator.authenticate(AuthenticationContext.create(properties));

		verify(userStore, never()).loadByIdentityId(anyString(), anyString());
	}

	@Test
	public void should_DoNothing_When_WrongCredentialsProdived() throws Exception {
		Map<String, String> authRequest = mockAuthRequest();
		mockDeploymentResponse(401);

		authenticator.authenticate(AuthenticationContext.create(authRequest));

		verify(userStore, never()).loadByIdentityId(any(), any());
	}

	@Test(expected = AuthenticationException.class)
	public void should_ThrowException_When_UserNotFound() throws Exception {
		Map<String, String> authRequest = mockAuthRequest();
		mockDeploymentResponse(200);

		authenticator.authenticate(AuthenticationContext.create(authRequest));
	}

	@Test(expected = AuthenticationException.class)
	public void should_ThrowException_When_IOExceptionOccurred() throws Exception {
		Map<String, String> authRequest = mockAuthRequest();
		KeycloakDeployment deployment = mockDeploymentResponse(200);
		when(deployment.getClient()).thenThrow(IOException.class);

		authenticator.authenticate(AuthenticationContext.create(authRequest));
	}

	@Test
	public void should_AuthenticateUser_When_UserFound() throws Exception {
		Map<String, String> authRequest = mockAuthRequest();
		mockDeploymentResponse(200);

		EmfUser user = new EmfUser("regularuser@sep.test");
		when(userStore.loadByIdentityId("regularuser@sep.test", TENANT_ID)).thenReturn(user);
		when(userStore.setUserTicket(eq(user), anyString())).thenReturn(user);

		User authenticated = authenticator.authenticate(AuthenticationContext.create(authRequest));

		assertEquals(user, authenticated);
	}

	private static Map<String, String> mockAuthRequest() {
		Map<String, String> properties = new HashMap<>();
		properties.put(HttpHeaders.AUTHORIZATION, buildAuthHeader("regularuser@sep.test", "qwerty"));
		return properties;
	}

	private KeycloakDeployment mockDeploymentResponse(int status) throws Exception {
		KeycloakDeployment deployment = KeycloakAuthTestUtil.getMockedDeployment();

		HttpClient httpClient = mock(HttpClient.class);

		HttpResponse httpResponse = mock(HttpResponse.class);

		HttpEntity entity = mock(HttpEntity.class);

		String token = KeycloakAuthTestUtil.generateToken("regularuser");
		String json = "{\"access_token\": \"" + token + "\"}";
		when(entity.getContent()).thenReturn(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));

		when(httpResponse.getEntity()).thenReturn(entity);

		StatusLine statusLine = mock(StatusLine.class);
		when(statusLine.getStatusCode()).thenReturn(status);

		when(httpResponse.getStatusLine()).thenReturn(statusLine);

		when(httpClient.execute(any())).thenReturn(httpResponse);

		when(deployment.getClient()).thenReturn(httpClient);

		when(deploymentRetriever.getDeployment(TENANT_ID)).thenReturn(deployment);

		return deployment;
	}

	private static String buildAuthHeader(String username, String password) {
		String credentials = username + ":" + password;
		return "Basic " + new String(encodeBase64(credentials), StandardCharsets.UTF_8);
	}

	private static byte[] encodeBase64(String credentials) {
		return Base64.getEncoder().encode(credentials.getBytes(StandardCharsets.UTF_8));
	}

}
