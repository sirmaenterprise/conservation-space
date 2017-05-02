package com.sirma.itt.emf.authentication.sso.saml;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Event;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
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

import com.sirma.itt.emf.authentication.session.SessionManager;
import com.sirma.itt.emf.authentication.sso.saml.SAMLServiceLogout.SAMLLogoutSecurityExclusion;
import com.sirma.itt.emf.security.event.BeginLogoutEvent;
import com.sirma.itt.emf.security.event.UserLogoutEvent;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.security.AuthenticationService;
import com.sirma.itt.seip.rest.secirity.SecurityTokensHolder;
import com.sirma.itt.seip.rest.utils.JwtUtil;
import com.sirma.itt.seip.security.UserStore;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;

/**
 * Tests for {@link SAMLServiceLogout}
 *
 * @author Adrian Mitev
 */
public class SAMLServiceLogoutTest {

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
	private BeanManager beanManager;

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

	private ConfigurationProperty<String> configProperty;
	private HttpServletRequest request;
	private HttpServletResponse response;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);

		request = mock(HttpServletRequest.class);
		response = mock(HttpServletResponse.class);
		when(request.getParameter(anyString())).thenReturn("responseMessage");
		when(request.getContextPath()).thenReturn("/emf");
		when(request.getSession()).thenReturn(Mockito.mock(HttpSession.class));

		configProperty = mock(ConfigurationProperty.class);
		when(systemConfiguration.getUi2Url()).thenReturn(configProperty);
		when(configProperty.requireConfigured()).thenReturn(configProperty);
		when(configProperty.get()).thenReturn("http://ui2.net");

		Set<Bean<?>> beans = new HashSet<>();
		beans.add(mock(Bean.class));
		when(beanManager.getBeans(any(), any())).thenReturn(beans);
		when(beanManager.resolve(Mockito.anySet())).thenReturn(Mockito.mock(Bean.class));
		when(beanManager.createCreationalContext(Mockito.any())).thenReturn(Mockito.mock(CreationalContext.class));

		AuthenticationService authenticationService = Mockito.mock(AuthenticationService.class);
		when(authenticationService.getCurrentUser()).thenReturn(new EmfUser());

		when(beanManager.getReference(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(authenticationService);

		when(sessionManager.getClientId(Mockito.any(), Mockito.any())).thenReturn("1");
		when(sessionManager.isProcessing(Mockito.anyString())).thenReturn(Boolean.FALSE);

		when(messageProcessor.buildLogoutMessage(Mockito.any(), Mockito.anyString(), Mockito.any()))
				.thenAnswer(invocation -> invocation.getArgumentAt(1, String.class));
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

		verify(response).sendRedirect("/emf/auth?url=123");
	}

	@Test
	public void testDoGet_verifyUi2RedirectAfterLogout() throws ServletException, IOException {
		when(request.getParameter("RelayState")).thenReturn("");
		logoutService.doGet(request, response);

		verify(response).sendRedirect("http://ui2.net");
		verify(systemConfiguration).getUi2Url();
	}

	@Test
	public void testDoGet_verifyRelayStateRedirectAfterLogout() throws ServletException, IOException {
		when(request.getParameter("RelayState")).thenReturn("123");
		logoutService.doGet(request, response);

		verify(response).sendRedirect("123");
	}

	@Test
	public void testDoGet_shouldSetSessionIndexAttributeInRequest() throws ServletException, IOException {
		AuthenticationService authenticationService = Mockito.mock(AuthenticationService.class);
		when(authenticationService.getCurrentUser()).thenReturn(null);

		when(beanManager.getReference(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(authenticationService);
		when(request.getParameter("jwt")).thenReturn("token");
		when(jwtUtil.readUser(securityContextManager, userStore, "token")).thenReturn(new EmfUser());
		when(jwtUtil.extractSessionIndex("token")).thenReturn("index");

		logoutService.doGet(request, response);

		verify(request.getSession()).setAttribute(SAMLMessageProcessor.SAML_KEY_SESSION, "index");
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
	public void testDoPost_verifyEventNotFired() throws ServletException, IOException {
		Map<String, String> samlResponse = new HashMap<>();
		samlResponse.put("Subject", "username@domain.com");

		when(messageProcessor.processSAMLResponse(any(byte[].class))).thenReturn(samlResponse);

		logoutService.doPost(request, response);

		verify(logoutEvent, times(0)).fire(any(UserLogoutEvent.class));
	}

}
