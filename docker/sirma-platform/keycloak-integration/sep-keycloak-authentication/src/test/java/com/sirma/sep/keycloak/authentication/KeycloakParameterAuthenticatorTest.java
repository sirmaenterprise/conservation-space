package com.sirma.sep.keycloak.authentication;

import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.keycloak.adapters.KeycloakDeployment;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.rest.secirity.JwtParameterAuthenticator;
import com.sirma.itt.seip.rest.utils.JwtConfiguration;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.UserStore;
import com.sirma.itt.seip.security.authentication.AuthenticationContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.sep.keycloak.config.KeycloakConfiguration;
import com.sirma.sep.keycloak.tenant.KeycloakDeploymentRetriever;

/**
 * Tests for {@link KeycloakParameterAuthenticator}.
 *
 * @author smustafov
 */
public class KeycloakParameterAuthenticatorTest {

	private static final String PARAM_NAME = "APIKey";

	@InjectMocks
	private KeycloakParameterAuthenticator authenticator;

	@Mock
	private JwtConfiguration jwtConfiguration;

	@Mock
	private KeycloakConfiguration keycloakConfiguration;

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

		when(jwtConfiguration.getJwtParameterName()).thenReturn(PARAM_NAME);
		when(keycloakConfiguration.getKeycloakAddress()).thenReturn(new ConfigurationPropertyMock<>("http://idp/auth"));
	}

	@Test
	public void should_SetUserTicket_When_AuthRequestCorrect() {
		Map<String, String> authRequest = mockAuthRequest();

		authenticator.authenticate(AuthenticationContext.create(authRequest));

		verify(userStore).setUserTicket(any(), any());
	}

	@Test
	public void should_UseConfigParam_When_DefaultOneHaveNoValue() {
		Map<String, String> authRequest = mockAuthRequest(PARAM_NAME);

		authenticator.authenticate(AuthenticationContext.create(authRequest));

		verify(userStore).setUserTicket(any(), any());
	}

	@Test
	public void should_DoNothing_When_TokenParamMissing() {
		authenticator.authenticate(AuthenticationContext.createEmpty());

		verify(userStore, never()).setUserTicket(any(), any());
	}

	@Test
	public void should_ReturnNull_When_TenantNotFoundInTokenString() {
		Map<String, String> properties = new HashMap<>();
		properties
				.put(JwtParameterAuthenticator.PARAMETER_NAME, KeycloakAuthTestUtil.generateToken(null, "regularuser"));

		User authenticated = authenticator.authenticate(AuthenticationContext.create(properties));

		assertNull(authenticated);
		verify(userStore, never()).setUserTicket(any(), any());
	}

	private Map<String, String> mockAuthRequest() {
		return mockAuthRequest(JwtParameterAuthenticator.PARAMETER_NAME);
	}

	private Map<String, String> mockAuthRequest(String paramName) {
		Map<String, String> properties = new HashMap<>();
		properties.put(paramName, KeycloakAuthTestUtil.generateToken("regularuser"));

		KeycloakDeployment deployment = KeycloakAuthTestUtil.getMockedDeployment();
		when(deploymentRetriever.getDeployment("sep.test")).thenReturn(deployment);

		user = new EmfUser();
		when(userStore.loadByIdentityId("regularuser@sep.test", "sep.test")).thenReturn(user);

		return properties;
	}

}
