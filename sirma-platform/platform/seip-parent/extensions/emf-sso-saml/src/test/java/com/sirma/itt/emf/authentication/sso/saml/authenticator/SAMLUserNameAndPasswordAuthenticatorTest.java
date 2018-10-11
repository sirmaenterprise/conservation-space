package com.sirma.itt.emf.authentication.sso.saml.authenticator;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.enterprise.event.Event;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.emf.authentication.sso.saml.SAMLMessageProcessor;
import com.sirma.itt.emf.security.event.UserAuthenticatedEvent;
import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.resources.security.SecurityTokenService;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.UserStore;
import com.sirma.itt.seip.security.authentication.AuthenticationContext;
import com.sirma.itt.seip.security.authentication.Authenticator;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.security.exception.AuthenticationException;
import com.sirma.itt.seip.security.util.SecurityUtil;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;

/**
 * Tests for {@link SAMLUserNameAndPasswordAuthenticator}
 *
 * @author BBonev
 */
public class SAMLUserNameAndPasswordAuthenticatorTest {

	private static final String TOKEN = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPHNhbWwycDpSZXNwb25zZSBEZXN0aW5hdGlvbj0iaHR0cHM6Ly8xMC4xMzEuMi4xOTE6ODQ0NC9lbWYvU2VydmljZUxvZ2luIiBJRD0iZmdhY2JkaGNubWljb29nZ2xuamdtbG5pYnBqZGRvaGxsbGZhYWtpZyIgSXNzdWVJbnN0YW50PSIyMDE1LTEwLTI4VDEwOjIzOjA1LjE4NFoiIFZlcnNpb249IjIuMCIgeG1sbnM6c2FtbDJwPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6cHJvdG9jb2wiPjxzYW1sMjpJc3N1ZXIgRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6bmFtZWlkLWZvcm1hdDplbnRpdHkiIHhtbG5zOnNhbWwyPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXNzZXJ0aW9uIj5sb2NhbGhvc3Q8L3NhbWwyOklzc3Vlcj48c2FtbDJwOlN0YXR1cz48c2FtbDJwOlN0YXR1c0NvZGUgVmFsdWU9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpzdGF0dXM6U3VjY2VzcyIvPjwvc2FtbDJwOlN0YXR1cz48c2FtbDI6QXNzZXJ0aW9uIElEPSJmbG5iY3BhYWlpZGhhYWlmbGRqZ2ZkbWRra3BhamNtamtnaGVocGFvIiBJc3N1ZUluc3RhbnQ9IjIwMTUtMTAtMjhUMTA6MjM6MDUuMTg0WiIgVmVyc2lvbj0iMi4wIiB4bWxuczpzYW1sMj0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiI+PHNhbWwyOklzc3VlciBGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpuYW1laWQtZm9ybWF0OmVudGl0eSI+bG9jYWxob3N0PC9zYW1sMjpJc3N1ZXI+PHNhbWwyOlN1YmplY3Q+PHNhbWwyOk5hbWVJRCBGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpuYW1laWQtZm9ybWF0OmVudGl0eSI+YWRtaW5AYmFuY2hldi5iZzwvc2FtbDI6TmFtZUlEPjxzYW1sMjpTdWJqZWN0Q29uZmlybWF0aW9uIE1ldGhvZD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmNtOmJlYXJlciI+PHNhbWwyOlN1YmplY3RDb25maXJtYXRpb25EYXRhIE5vdE9uT3JBZnRlcj0iMjAxNS0xMC0yOFQxMDoyODowNS4xODRaIiBSZWNpcGllbnQ9Imh0dHBzOi8vMTAuMTMxLjIuMTkxOjg0NDQvZW1mL1NlcnZpY2VMb2dpbiIvPjwvc2FtbDI6U3ViamVjdENvbmZpcm1hdGlvbj48L3NhbWwyOlN1YmplY3Q+PHNhbWwyOkNvbmRpdGlvbnMgTm90QmVmb3JlPSIyMDE1LTEwLTI4VDEwOjIzOjA1LjE4NFoiIE5vdE9uT3JBZnRlcj0iMjAxNS0xMC0yOFQxMDoyODowNS4xODRaIj48c2FtbDI6QXVkaWVuY2VSZXN0cmljdGlvbj48c2FtbDI6QXVkaWVuY2U+MTAuMTMxLjIuMTkxXzg0NDQ8L3NhbWwyOkF1ZGllbmNlPjwvc2FtbDI6QXVkaWVuY2VSZXN0cmljdGlvbj48L3NhbWwyOkNvbmRpdGlvbnM+PHNhbWwyOkF1dGhuU3RhdGVtZW50IEF1dGhuSW5zdGFudD0iMjAxNS0xMC0yOFQxMDoyMzowNS4xODVaIj48c2FtbDI6QXV0aG5Db250ZXh0PjxzYW1sMjpBdXRobkNvbnRleHRDbGFzc1JlZj51cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YWM6Y2xhc3NlczpQYXNzd29yZDwvc2FtbDI6QXV0aG5Db250ZXh0Q2xhc3NSZWY+PC9zYW1sMjpBdXRobkNvbnRleHQ+PC9zYW1sMjpBdXRoblN0YXRlbWVudD48L3NhbWwyOkFzc2VydGlvbj48L3NhbWwycDpSZXNwb25zZT4=";

	@InjectMocks
	private SAMLUserNameAndPasswordAuthenticator authenticator;

	@Mock
	private SAMLMessageProcessor messageProcessor;
	@Spy
	private SecurityContextManager securityContextManager = new SecurityContextManagerFake();
	@Mock
	private Event<UserAuthenticatedEvent> authenticatedEvent;
	@Mock
	private SecurityConfiguration securityConfiguration;
	@Mock
	private UserStore userStore;

	@Mock
	private SecurityTokenService tokenService;
	@Spy
	private InstanceProxyMock<SecurityTokenService> securityTokenService = new InstanceProxyMock<>(null);
	@Mock
	private User user;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		securityTokenService.set(tokenService);

		SecretKey secretKey = SecurityUtil.createSecretKey("AlfrescoCMFLogin@Test");
		when(securityConfiguration.getCryptoKey()).thenReturn(new ConfigurationPropertyMock<>(secretKey));

		when(messageProcessor.processSAMLResponse(any()))
				.thenReturn(Collections.singletonMap("Subject", "admin@tenant.com"));

		when(user.getProperties()).thenReturn(new HashMap<>());

		when(userStore.loadByIdentityId("admin@tenant.com", "tenant.com")).thenReturn(user);
		when(userStore.setUserTicket(any(User.class), anyString())).thenAnswer(a -> a.getArgumentAt(0, User.class));
	}

	@Test
	public void authenticate() throws Exception {
		Map<String, String> properties = new HashMap<>();
		properties.put(Authenticator.USERNAME, "name");
		properties.put(Authenticator.CREDENTIAL, "pass");
		AuthenticationContext context = AuthenticationContext.create(properties);

		when(tokenService.requestToken("name", "pass")).thenReturn(TOKEN);

		assertNotNull(authenticator.authenticate(context));
	}

	@Test
	public void authenticate_noConfig() throws Exception {

		Map<String, String> properties = new HashMap<>();
		assertNull(authenticator.authenticate(AuthenticationContext.create(new HashMap<>())));

		properties.put(Authenticator.USERNAME, "name");
		assertNull(authenticator.authenticate(AuthenticationContext.create(properties)));

		properties.remove(Authenticator.USERNAME);
		properties.put(Authenticator.CREDENTIAL, "pass");
		assertNull(authenticator.authenticate(AuthenticationContext.create(properties)));
	}

	@Test(expected = AuthenticationException.class)
	public void authenticate_noSaml() throws Exception {
		Map<String, String> properties = new HashMap<>();
		properties.put(Authenticator.USERNAME, "name");
		properties.put(Authenticator.CREDENTIAL, "pass");
		AuthenticationContext context = AuthenticationContext.create(properties);

		when(tokenService.requestToken("name", "pass")).thenThrow(Exception.class);

		authenticator.authenticate(context);
	}

	@Test(expected = AuthenticationException.class)
	public void authenticate_noSaml_noToken() throws Exception {
		Map<String, String> properties = new HashMap<>();
		properties.put(Authenticator.USERNAME, "name");
		properties.put(Authenticator.CREDENTIAL, "pass");
		AuthenticationContext context = AuthenticationContext.create(properties);

		authenticator.authenticate(context);
	}

	@Test(expected = EmfApplicationException.class)
	public void authenticate_noTokenService() throws Exception {

		Map<String, String> properties = new HashMap<>();
		properties.put(Authenticator.USERNAME, "name");
		properties.put(Authenticator.CREDENTIAL, "pass");
		AuthenticationContext context = AuthenticationContext.create(properties);

		securityTokenService.set(null);

		authenticator.authenticate(context);
	}

	@Test
	public void authenticateForUser() throws Exception {
		User toAuthenticate = mock(User.class);
		when(toAuthenticate.getIdentityId()).thenReturn("admin@tenant.com");
		when(toAuthenticate.getCredentials()).thenReturn("pass");
		when(toAuthenticate.getTenantId()).thenReturn("tenant.com");

		when(tokenService.requestToken("admin@tenant.com", "pass")).thenReturn(TOKEN);

		authenticator.authenticate(toAuthenticate);
	}

	@Test(expected = AuthenticationException.class)
	public void authenticateForUser_noTenantMatch() throws Exception {
		User toAuthenticate = mock(User.class);
		when(toAuthenticate.getIdentityId()).thenReturn("admin@tenant.com");
		when(toAuthenticate.getCredentials()).thenReturn("pass");
		when(toAuthenticate.getTenantId()).thenReturn(null);

		when(tokenService.requestToken("admin@tenant.com", "pass")).thenReturn(TOKEN);

		authenticator.authenticate(toAuthenticate);
	}

	@Test(expected = AuthenticationException.class)
	public void authenticateForUser_noSaml() throws Exception {
		assertNull(authenticator.authenticate((User) null));

		User toAuthenticate = mock(User.class);
		assertNull(authenticator.authenticate(toAuthenticate));

		when(toAuthenticate.getIdentityId()).thenReturn("admin@tenant.com");
		assertNull(authenticator.authenticate(toAuthenticate));

		when(toAuthenticate.getCredentials()).thenReturn("pass");

		when(tokenService.requestToken("admin@tenant.com", "pass")).thenThrow(Exception.class);

		authenticator.authenticate(toAuthenticate);
	}

	@Test(expected = AuthenticationException.class)
	public void authenticateForUser_noSamlToken() throws Exception {

		User toAuthenticate = mock(User.class);

		when(toAuthenticate.getIdentityId()).thenReturn("admin@tenant.com");
		when(toAuthenticate.getCredentials()).thenReturn("pass");

		when(tokenService.requestToken("admin@tenant.com", "pass")).thenReturn(null);

		authenticator.authenticate(toAuthenticate);
	}

	@Test(expected = EmfApplicationException.class)
	public void authenticateForUser_noTokenService() throws Exception {

		User toAuthenticate = mock(User.class);
		when(toAuthenticate.getIdentityId()).thenReturn("admin@tenant.com");
		when(toAuthenticate.getCredentials()).thenReturn("pass");

		securityTokenService.set(null);

		authenticator.authenticate(toAuthenticate);
	}

}
