package com.sirma.itt.emf.authentication.sso.saml;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.event.Event;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.SessionIndex;

import com.sirma.itt.emf.authentication.sso.saml.SAMLServiceLogout.SAMLLogoutSecurityExclusion;
import com.sirma.itt.emf.security.event.BeginLogoutEvent;
import com.sirma.itt.emf.security.event.UserLogoutEvent;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.rest.secirity.SecurityTokensHolder;
import com.sirma.itt.seip.rest.session.SessionManager;
import com.sirma.itt.seip.rest.utils.JwtUtil;
import com.sirma.itt.seip.security.UserStore;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;

/**
 * Tests for {@link SAMLServiceLogout}
 *
 * @author Adrian Mitev
 */
public class SAMLServiceLogoutTest {

	private static final String UI2_ADDRESS = "http://ui2.net";
	@InjectMocks
	private SAMLServiceLogout logoutService;

	@Mock
	private SAMLMessageProcessor messageProcessor;

	@Mock
	private Event<UserLogoutEvent> logoutEvent;

	@Mock
	private Event<BeginLogoutEvent> beginLogoutEvent;

	@Mock
	private SessionManager sessionManager;

	@Mock
	private UserStore userStore;

	@Spy
	private SecurityContextManager securityContextManager = new SecurityContextManagerFake();

	@Mock
	private JwtUtil jwtUtil;

	@Mock
	private SecurityTokensHolder tokens;

	@Mock
	private SystemConfiguration systemConfiguration;

	private ConfigurationProperty<String> configProperty = new ConfigurationPropertyMock<>(UI2_ADDRESS);
	private HttpServletRequest request;
	private HttpServletResponse response;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);

		request = mock(HttpServletRequest.class);
		response = mock(HttpServletResponse.class);
		when(request.getParameter("jwt")).thenReturn("jwtToken");
		when(request.getParameter(SAMLServiceLogout.SAML_RESPONSE)).thenReturn("responseMessage");
		when(request.getContextPath()).thenReturn("/emf");
		when(request.getSession()).thenReturn(Mockito.mock(HttpSession.class));

		when(systemConfiguration.getUi2Url()).thenReturn(configProperty);

		when(sessionManager.getClientId(Mockito.any())).thenReturn("1");
		when(sessionManager.isProcessing(Mockito.anyString())).thenReturn(Boolean.FALSE);

		when(messageProcessor.buildLogoutMessage(Mockito.any(), Mockito.anyString(), Mockito.any()))
				.thenAnswer(a -> "?SAMLRequest=aRequest&RelayState=" + a.getArgumentAt(1, String.class));
	}

	/**
	 * Tests {@link SAMLLogoutSecurityExclusion}.
	 */
	@Test
	public void verifySecurityExlusionShouldExcludeTheServletPath() {
		SAMLLogoutSecurityExclusion exclusion = new SAMLLogoutSecurityExclusion();
		Assert.assertTrue(exclusion.isForExclusion(SAMLServiceLogout.SERVICE_LOGOUT));
	}

	@Test
	public void testDoPost_verifyUi2RedirectAfterLogout() throws ServletException, IOException {
		when(request.getParameter("RelayState")).thenReturn("");
		logoutService.doPost(request, response);

		verify(response).sendRedirect(anyString());
		verify(systemConfiguration).getUi2Url();
	}

	@Test
	public void testDoPost_verifyRelayStateRedirectAfterLogout() throws ServletException, IOException {
		when(request.getParameter("RelayState")).thenReturn("123");
		logoutService.doPost(request, response);

		verify(response).sendRedirect("123");
	}

	@Test
	public void testDoGet_verifyUi2RedirectAfterLogout() throws ServletException, IOException {
		when(request.getParameter("jwt")).thenReturn("token");
		when(jwtUtil.readUser(securityContextManager, userStore, "token")).thenReturn(new EmfUser());
		when(tokens.getSamlToken("token")).thenReturn(Optional.of("saml"));

		when(request.getParameter("RelayState")).thenReturn("");
		logoutService.doGet(request, response);

		verify(response).sendRedirect("?SAMLRequest=aRequest&RelayState=");
	}

	@Test
	public void testDoGet_verifyRelayStateRedirectAfterLogout() throws ServletException, IOException {
		when(request.getParameter("jwt")).thenReturn("token");
		when(jwtUtil.readUser(securityContextManager, userStore, "token")).thenReturn(new EmfUser());
		when(tokens.getSamlToken("token")).thenReturn(Optional.of("saml"));

		when(request.getParameter("RelayState")).thenReturn("123");
		logoutService.doGet(request, response);

		verify(response).sendRedirect("?SAMLRequest=aRequest&RelayState=123");
	}

	@Test
	public void testDoGet_shouldSetSessionIndexAttributeInRequest() throws ServletException, IOException {

		when(request.getParameter("jwt")).thenReturn("token");
		when(jwtUtil.readUser(securityContextManager, userStore, "token")).thenReturn(new EmfUser());
		when(jwtUtil.extractSessionIndex("token")).thenReturn("index");
		when(tokens.getSamlToken("token")).thenReturn(Optional.of("saml"));

		logoutService.doGet(request, response);

		verify(request.getSession()).setAttribute(SAMLMessageProcessor.SAML_KEY_SESSION, "index");
	}

	@Test
	public void testDoGet_ShouldRemoveJwtSession() throws ServletException, IOException {

		when(request.getParameter("jwt")).thenReturn("token");
		when(jwtUtil.readUser(securityContextManager, userStore, "token")).thenReturn(new EmfUser());
		when(jwtUtil.extractSessionIndex("token")).thenReturn("index");
		when(tokens.getSamlToken("token")).thenReturn(Optional.of("saml"));

		logoutService.doGet(request, response);

		verify(sessionManager).removeLoggedUser("token");
	}

	@Test
	public void should_InvalidateSession_When_ThereIsNoAuthenticatedUser() throws ServletException, IOException {

		when(request.getParameter("jwt")).thenReturn(null);

		HttpSession session = mock(HttpSession.class);
		when(request.getSession()).thenReturn(session);

		logoutService.doGet(request, response);

		verify(session).invalidate();
		verify(response).sendRedirect(UI2_ADDRESS);
	}

	@Test
	public void should_RedirectToContextPath_When_HttpSessionIsAlreadyInvalidated()
			throws ServletException, IOException {

		when(request.getParameter("jwt")).thenReturn(null);
		when(request.getSession()).thenReturn(null);
		logoutService.doGet(request, response);

		HttpSession session = mock(HttpSession.class);
		doAnswer(invocation -> {
			throw new IllegalStateException();
		}).when(session).invalidate();
		when(request.getSession()).thenReturn(session);

		logoutService.doGet(request, response);
		verify(response, times(2)).sendRedirect(UI2_ADDRESS);
	}

	@Test
	public void should_InvalidateSession_When_AnExceptionOccursDuringPreLogoutProcess()
			throws ServletException, IOException {

		HttpSession session = mock(HttpSession.class);
		when(request.getSession()).thenReturn(session);

		logoutService.doGet(request, response);

		verify(session).invalidate();
		verify(response).sendRedirect(UI2_ADDRESS);
	}

	@Test
	public void should_NotInitializeSecurityContext_When_ItsAlreadyInitialized() throws ServletException, IOException {
		SecurityContext currentContext = securityContextManager.getCurrentContext();
		when(currentContext.isActive()).thenReturn(Boolean.TRUE);

		logoutService.doGet(request, response);

		verify(securityContextManager, never()).initializeTenantContext(anyString());
	}

	@Test
	public void should_RedirectToContextPath_When_ThereIsLogoutOnAnotherBrowserTab()
			throws ServletException, IOException {
		when(sessionManager.isProcessing(anyString())).thenReturn(Boolean.TRUE);

		logoutService.doGet(request, response);

		verify(response).sendRedirect(UI2_ADDRESS);
	}

	@Test
	public void testDoPost_verifyEventFiredAfterLogout() throws ServletException, IOException {
		EmfUser user = new EmfUser();
		user.setId("username@domain.com");
		Map<String, String> samlResponse = new HashMap<>();
		samlResponse.put("Subject", "username@domain.com");

		when(messageProcessor.processSAMLResponse(any(byte[].class))).thenReturn(samlResponse);
		when(userStore.loadByIdentityId("username@domain.com", "domain.com")).thenReturn(user);

		logoutService.doPost(request, response);

		verify(logoutEvent).fire(any(UserLogoutEvent.class));
	}

	@Test
	public void testDoPost_shouldHandleSAMLRequest() throws ServletException, IOException {
		EmfUser user = new EmfUser();
		user.setId("username@domain.com");

		LogoutRequest logoutRequest = mock(LogoutRequest.class);
		SessionIndex sessionIndex = mock(SessionIndex.class);
		when(sessionIndex.getSessionIndex()).thenReturn("session-index");
		NameID nameID = mock(NameID.class);
		when(nameID.getValue()).thenReturn("username@domain.com");
		when(logoutRequest.getNameID()).thenReturn(nameID);
		when(logoutRequest.getSessionIndexes()).thenReturn(Collections.singletonList(sessionIndex));

		when(request.getParameter(SAMLServiceLogout.SAML_RESPONSE)).thenReturn(null);
		when(request.getParameter(SAMLServiceLogout.SAML_REQUEST)).thenReturn(Base64.getEncoder().encodeToString("requestMessage".getBytes(
				StandardCharsets.UTF_8)));

		when(messageProcessor.processSAMLRequest("requestMessage")).thenReturn(logoutRequest);
		when(userStore.loadByIdentityId("username@domain.com", "domain.com")).thenReturn(user);

		logoutService.doPost(request, response);

		verify(logoutEvent).fire(any(UserLogoutEvent.class));
	}

	@Test
	public void testDoPost_verifyEventNotFired() throws ServletException, IOException {
		Map<String, String> samlResponse = new HashMap<>();
		samlResponse.put("Subject", "username@domain.com");

		when(messageProcessor.processSAMLResponse(any(byte[].class))).thenReturn(samlResponse);

		logoutService.doPost(request, response);

		verify(logoutEvent, times(0)).fire(any(UserLogoutEvent.class));
	}

}
