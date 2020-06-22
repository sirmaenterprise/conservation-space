package com.sirma.itt.emf.authentication.sso.saml;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.event.Event;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.emf.authentication.sso.saml.SAMLServiceLogin.SAMLLoginSecurityExclusion;
import com.sirma.itt.emf.security.event.UserAuthenticatedEvent;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.rest.session.SessionManager;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.authentication.AuthenticationContext;
import com.sirma.itt.seip.security.authentication.AuthenticationResponseDecorator;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.security.exception.AccountDisabledException;
import com.sirma.itt.seip.security.exception.AuthenticationException;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;

/**
 * Tests for {@link SAMLServiceLogin}
 *
 * @author Adrian Mitev
 */
public class SAMLServiceLoginTest {

	public static final String UI2_ADDRESS = "http://localhost:5454";
	@InjectMocks
	private SAMLServiceLogin servlet;

	@Mock
	private SessionManager sessionManager;

	@Spy
	private SecurityContextManager securityContextManager = new SecurityContextManagerFake();

	@Mock
	private SAMLMessageProcessor messageProcessor;

	@Mock
	private AuthenticationResponseDecorator responseDecorator;
	@Spy
	private InstanceProxyMock<AuthenticationResponseDecorator> decorators = new InstanceProxyMock<>();

	@Mock
	private Event<UserAuthenticatedEvent> authenticatedEvent;

	@Mock
	private SystemConfiguration systemConfiguration;

	@Mock
	private HttpServletRequest request;
	@Mock
	private HttpServletResponse response;
	@Mock
	private HttpSession session;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		decorators.set(responseDecorator);
		when(request.getSession()).thenReturn(session);
		when(systemConfiguration.getUi2Url()).thenReturn(new ConfigurationPropertyMock<>(UI2_ADDRESS));
	}

	/**
	 * Tests {@link SAMLLoginSecurityExclusion}.
	 */
	@Test
	public void verifySecurityExclusionShouldExcludeTheServletPath() {
		SAMLLoginSecurityExclusion exclusion = new SAMLLoginSecurityExclusion();
		Assert.assertTrue(exclusion.isForExclusion(SAMLServiceLogin.SERVICE_LOGIN));
	}

	@Test
	public void shouldFailOnMissingResponse() throws Exception {
		servlet.doPost(request, response);
		verify(response).sendError(eq(HttpStatus.SC_BAD_REQUEST), anyString());
	}

	@Test
	public void shouldFailOnMissingRelayState() throws Exception {
		when(request.getParameter(SAMLServiceLogin.SAML_RESPONSE)).thenReturn("samlResponse");
		servlet.doPost(request, response);
		verify(response).sendError(eq(HttpStatus.SC_BAD_REQUEST), anyString());
	}

	@Test
	public void shouldAuthenticate() throws Exception {
		String relay = "localhost";
		String encodedRelay = Base64.getEncoder().encodeToString(relay.getBytes(StandardCharsets.UTF_8));
		when(request.getParameter(SAMLServiceLogin.SAML_RESPONSE)).thenReturn("samlResponse");
		when(request.getParameter(SAMLServiceLogin.RELAY_STATE)).thenReturn(encodedRelay);

		User user = mock(User.class);
		when(securityContextManager.getCurrentContext().getAuthenticated()).thenReturn(user);
		servlet.doPost(request, response);

		verify(securityContextManager).initializeExecution(any(AuthenticationContext.class));
		verify(securityContextManager).endContextExecution();
		verify(response).sendRedirect(relay);
		verify(authenticatedEvent).fire(any());
		verify(responseDecorator).decorate(anyMap());
	}

	@Test
	public void shouldRedirectToHomePageIfInvalidRelay() throws Exception {
		String relay = "localhost";
		String encodedRelay = Base64.getEncoder().encodeToString(relay.getBytes(StandardCharsets.UTF_8));
		encodedRelay = new StringBuilder(encodedRelay).replace(2,4,"\n\n").toString();
		when(request.getParameter(SAMLServiceLogin.SAML_RESPONSE)).thenReturn("samlResponse");
		when(request.getParameter(SAMLServiceLogin.RELAY_STATE)).thenReturn(encodedRelay);

		User user = mock(User.class);
		when(securityContextManager.getCurrentContext().getAuthenticated()).thenReturn(user);
		servlet.doPost(request, response);

		verify(securityContextManager).initializeExecution(any(AuthenticationContext.class));
		verify(securityContextManager).endContextExecution();
		verify(response).sendRedirect(UI2_ADDRESS);
		verify(authenticatedEvent).fire(any());
		verify(responseDecorator).decorate(anyMap());
	}

	@Test
	public void shouldNotAllowDisabledUser() throws Exception {
		String relay = "localhost";
		String encodedRelay = Base64.getEncoder().encodeToString(relay.getBytes(StandardCharsets.UTF_8));
		when(request.getParameter(SAMLServiceLogin.SAML_RESPONSE)).thenReturn("samlResponse");
		when(request.getParameter(SAMLServiceLogin.RELAY_STATE)).thenReturn(encodedRelay);

		User user = mock(User.class);
		when(user.getIdentityId()).thenReturn("name@tenant.com");
		Map<String, Serializable> properties = new HashMap<>();
		properties.put(SAMLMessageProcessor.SAML_KEY_SESSION, "sessionIndex");
		when(user.getProperties()).thenReturn(properties);
		when(securityContextManager.initializeExecution(any(AuthenticationContext.class))).thenThrow(
				new AccountDisabledException(user, ""));
		String redirect = "http://localhost:5454/#/public/disabledUser?id=" + URLEncoder.encode("name@tenant.com", "utf-8");

		when(messageProcessor.buildLogoutMessage(eq("name@tenant.com"), eq(redirect), any(), any())).then(
				a -> a.getArgumentAt(1, String.class).replace("localhost", "127.0.0.1"));

		servlet.doPost(request, response);

		verify(response).sendRedirect(redirect.replace("localhost", "127.0.0.1"));
		verify(authenticatedEvent, never()).fire(any());
		verify(responseDecorator, never()).decorate(anyMap());
		verify(session).invalidate();
	}

	@Test
	public void shouldNotAllowDisabledUser_shouldRedirectToDisablePageOnMissingSessionIndex() throws Exception {
		String relay = "localhost";
		String encodedRelay = Base64.getEncoder().encodeToString(relay.getBytes(StandardCharsets.UTF_8));
		when(request.getParameter(SAMLServiceLogin.SAML_RESPONSE)).thenReturn("samlResponse");
		when(request.getParameter(SAMLServiceLogin.RELAY_STATE)).thenReturn(encodedRelay);

		User user = mock(User.class);
		when(user.getIdentityId()).thenReturn("name@tenant.com");
		when(securityContextManager.initializeExecution(any(AuthenticationContext.class))).thenThrow(
				new AccountDisabledException(user, ""));
		String redirect = "http://localhost:5454/#/public/disabledUser?id=" + URLEncoder.encode("name@tenant.com", "utf-8");

		when(messageProcessor.buildLogoutMessage(eq("name@tenant.com"), eq(redirect), any(), any())).then(
				a -> a.getArgumentAt(1, String.class));

		servlet.doPost(request, response);

		verify(response).sendRedirect(redirect);
		verify(authenticatedEvent, never()).fire(any());
		verify(responseDecorator, never()).decorate(anyMap());
		verify(session).invalidate();
	}

	@Test
	public void shouldNotAllowInvalidUser() throws Exception {
		String relay = "localhost";
		String encodedRelay = Base64.getEncoder().encodeToString(relay.getBytes(StandardCharsets.UTF_8));
		when(request.getParameter(SAMLServiceLogin.SAML_RESPONSE)).thenReturn("samlResponse");
		when(request.getParameter(SAMLServiceLogin.RELAY_STATE)).thenReturn(encodedRelay);

		User user = mock(User.class);
		when(user.getIdentityId()).thenReturn("name@tenant.com");
		when(securityContextManager.initializeExecution(any(AuthenticationContext.class))).thenThrow(
				new AuthenticationException("name@tenant.com", "sessionIndex","message"));

		when(messageProcessor.buildLogoutMessage(eq("name@tenant.com"),
				eq("http://localhost:5454/#/public/disabledUser?id=" + URLEncoder.encode("name@tenant.com", "utf-8")), any(),
				any())).then(a -> a.getArgumentAt(1, String.class));

		servlet.doPost(request, response);

		verify(authenticatedEvent, never()).fire(any());
		verify(responseDecorator, never()).decorate(anyMap());
		verify(session).invalidate();
	}

}
