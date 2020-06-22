package com.sirma.itt.emf.authentication.sso.saml;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Supplier;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.SessionIndex;
import org.opensaml.xml.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.security.event.BeginLogoutEvent;
import com.sirma.itt.emf.security.event.UserLogoutEvent;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.rest.session.SessionManager;
import com.sirma.itt.seip.rest.utils.JwtUtil;
import com.sirma.itt.seip.security.UserStore;
import com.sirma.itt.seip.security.configuration.SecurityExclusion;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.security.exception.AuthenticationException;
import com.sirma.itt.seip.security.util.SecurityUtil;

/**
 * Handles SAML Logout requests including single-logout.
 */
@WebServlet(name = "SAML2ServiceLogout", urlPatterns = SAMLServiceLogout.SERVICE_LOGOUT, loadOnStartup = 1)
public class SAMLServiceLogout extends HttpServlet {

	private static final long serialVersionUID = -4749929835873786286L;

	static final String SERVICE_LOGOUT = "/ServiceLogout";
	static final String SAML_RESPONSE = "SAMLResponse";
	static final String SAML_REQUEST = "SAMLRequest";

	private static final Logger LOGGER = LoggerFactory.getLogger(SAMLServiceLogout.class);

	@Inject
	private SAMLMessageProcessor messageProcessor;

	@Inject
	private Event<UserLogoutEvent> logoutEvent;

	@Inject
	private Event<BeginLogoutEvent> beginLogoutEvent;

	@Inject
	private SessionManager sessionManager;

	@Inject
	private UserStore userStore;

	@Inject
	private SecurityContextManager securityContextManager;

	@Inject
	private JwtUtil jwtUtil;

	@Inject
	private SystemConfiguration systemConfiguration;

	@Override
	public void init() {
		String logoutContextPath = getServletContext().getContextPath() + SERVICE_LOGOUT;
		getServletContext().setAttribute("logoutContextPath", logoutContextPath);
	}

	/**
	 * Called by the browser when a logout should be performed. Sends a single logout request to the IdP.
	 *
	 * @param request
	 *            the request
	 * @param response
	 *            the response
	 * @throws ServletException
	 *             the servlet exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		try {

			// its a logout request from UI2, try loading user from jwt token
			User currentUser = loadUserFromJwt(request);

			String userName = currentUser == null ? " null" : currentUser.getName();
			LOGGER.debug("Sending SAML logout request: {} with session: {}", userName, session.getId());
			if (currentUser == null) {
				invalidateSession(session);
				sendRedirect(response, getRelayAddress(request));
				return;
			}
			// in multitab scenario, wait for single request to complete
			String id = sessionManager.getClientId(request);
			if (sessionManager.isProcessing(id)) {
				Thread.sleep(2000);
				sendRedirect(response, getRelayAddress(request));
				return;
			}

			logoutInSecurityContext(request, response, currentUser, id);
		} catch (Exception e) {
			LOGGER.warn("Logout error: ", e);
			invalidateSession(session);
			sendRedirect(response, getRelayAddress(request));
		}
	}

	private String getRelayAddress(HttpServletRequest request) {
		return systemConfiguration.getUi2Url().computeIfNotSet(request::getContextPath);
	}

	private static String getJwtFromRequest(HttpServletRequest request) {
		return request.getParameter("jwt");
	}

	private User loadUserFromJwt(HttpServletRequest request) {
		String jwtToken = getJwtFromRequest(request);
		if (StringUtils.isBlank(jwtToken)) {
			return null;
		}

		EmfUser user = new EmfUser(jwtUtil.readUser(securityContextManager, userStore, jwtToken));
		// set session index in the http request for successful logout in idp
		String sessionIndex = jwtUtil.extractSessionIndex(jwtToken);
		request.getSession().setAttribute(SAMLMessageProcessor.SAML_KEY_SESSION, sessionIndex);
		return user;
	}

	private void logoutInSecurityContext(HttpServletRequest request, HttpServletResponse response, User currentUser,
			String id) {
		if (securityContextManager.getCurrentContext().isActive()) {
			doLogout(request, response, currentUser, id);
		} else {
			// by default the logout is performed in no security context so operations that require context like
			// configuration access per tenant need to be executed in such
			try {
				String tenantId = getTenantId(currentUser);
				securityContextManager.initializeTenantContext(tenantId);
				// execute as the user that performs the logout so the correct audit information is logged in
				securityContextManager.beginContextExecution(currentUser);
				// do the actual logout procedure
				doLogout(request, response, currentUser, id);
			} finally {
				// it's called twice for the double context activations
				securityContextManager.endContextExecution();
				securityContextManager.endContextExecution();
			}
		}
	}

	private String getTenantId(User currentUser) {
		String tenantId = currentUser.getTenantId();
		if (tenantId == null) {
			if (nullSafeEquals(securityContextManager.getSuperAdminUser().getIdentityId(),
					currentUser.getIdentityId())) {
				tenantId = SecurityContext.SYSTEM_TENANT;
			} else {
				tenantId = SecurityContext.getDefaultTenantId();
			}
		}
		return tenantId;
	}

	private void doLogout(HttpServletRequest request, HttpServletResponse response, User currentUser, String id) {
		sessionManager.beginLogout(id);
		sessionManager.removeLoggedUser(getJwtFromRequest(request));

		beginLogoutEvent.fire(new BeginLogoutEvent(currentUser));

		// if the UI sets a relay (for example the originating page) we will pass it to doPost method
		// that will later pass it to the login so the flow will be
		// user is on page A -> hits logout -> redirected to login -> do login -> arrives back at page A
		// if there is no relay the doPost method will redirect to home page
		String redirectTo = request.getParameter(SAMLServiceLogin.RELAY_STATE);

		sendRedirect(response, messageProcessor.buildLogoutMessage(currentUser.getName(), redirectTo, request));
	}

	private String getRelayState(HttpServletRequest request) {
		String relayTo = request.getParameter("RelayState");
		if (StringUtils.isBlank(relayTo)) {
			relayTo = getRelayAddress(request);
		}
		return relayTo;
	}

	/**
	 * Called by the IdP when the logout request is processed. Sends a logout event and invalidates the session.
	 *
	 * @param request
	 *            the request
	 * @param response
	 *            the response
	 * @throws ServletException
	 *             the servlet exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// here is the right place to invalidate session, but since redirect to
		// idp might be
		// blocked(FF) the session is invalidated at first place
		String responseMessage = request.getParameter(SAML_RESPONSE);
		if (responseMessage != null) {
			Map<String, String> samlResponse = messageProcessor.processSAMLResponse(Base64.decode(responseMessage));
			String userId = SAMLMessageProcessor.getSubject(samlResponse);
			executeLogout(userId, () -> new Pair<>(null, request.getSession()));

			String redirectTo = request.getParameter(SAMLServiceLogin.RELAY_STATE);

			if (StringUtils.isBlank(redirectTo)) {
				try {
					// if there is no requested relay we redirect to login
					// after the IdP processed the logout, redirect to AuthenticationServlet with the ui2 url
					// so after login can go in ui2
					StringBuilder uiPath = new StringBuilder(request.getContextPath());
					String relayTo = getRelayState(request);
					uiPath.append("/auth?url=").append(URLEncoder.encode(relayTo, StandardCharsets.UTF_8.name()));
					redirectTo = uiPath.toString();
				} catch (UnsupportedEncodingException e) {
					LOGGER.warn("Cannot encode redirect", e);
					redirectTo = getRelayState(request);
				}
			}
			sendRedirect(response, redirectTo);
		} else {
			String encodedRequestMessage = request.getParameter(SAML_REQUEST);
			LOGGER.debug("IDP SAML request has been received! {}", encodedRequestMessage);
			if (encodedRequestMessage != null) {
				logoutUsingSAMLRequest(encodedRequestMessage);
			} else {
				LOGGER.error("Did not receive any valid logout SAML request!");
			}
		}
	}

	/**
	 * Logs out the current user (and invalidates session) using an information provided by a SAML request. Such logout
	 * may also be initiated by the IdP when a single sign-out is performed.
	 *
	 * @param encodedRequestMessage
	 *            SAML logout request as XML.
	 */
	private void logoutUsingSAMLRequest(String encodedRequestMessage) {
		LogoutRequest processSAMLRequest = messageProcessor
				.processSAMLRequest(new String(Base64.decode(encodedRequestMessage), StandardCharsets.UTF_8));
		if (processSAMLRequest != null && processSAMLRequest.getSessionIndexes() != null) {
			String userId = processSAMLRequest.getNameID().getValue();
			final String tenantId = SecurityUtil.getUserAndTenant(userId).getSecond();
			Pair<String, HttpSession> indexAndSession = securityContextManager
					.executeAsTenant(tenantId)
					.supplier(() -> processSAMLRequest.getSessionIndexes()
							.stream()
							.map(SessionIndex::getSessionIndex)
							.map(sessionIndex -> new Pair<>(sessionIndex, sessionManager.getSession(sessionIndex)))
							.findFirst()
							.orElse(null));
			executeLogout(userId, () -> indexAndSession);

		}
	}

	private void executeLogout(String userId, Supplier<Pair<String, HttpSession>> session) {
		final String tenantId = SecurityUtil.getUserAndTenant(userId).getSecond();
		securityContextManager.executeAsTenant(tenantId).executable(() -> {
			try {
				com.sirma.itt.seip.security.User loggedInUser = userStore.loadByIdentityId(userId, tenantId);
				// fire event for backend logout
				if (loggedInUser != null) {
					User user = (User) userStore.wrap(loggedInUser);
					LOGGER.debug("Firing UserLogoutEvent for {}", user);
					logoutEvent.fire(new UserLogoutEvent(user));
				}
			} catch (AuthenticationException e) {
				// this could happen when user with valid sso account tries to enter the application
				// but the user is not allowed to enter probably not found or for invalid tenant we
				// will try just to log them out.
				LOGGER.trace("", e);
			} finally {
				cleanUpSession(session);
			}
		});
	}

	private void cleanUpSession(Supplier<Pair<String, HttpSession>> session) {
		if (session != null) {
			if (session.get().getSecond() != null) {
				invalidateSession(session.get().getSecond());
			}
			if (session.get().getFirst() != null) {
				sessionManager.unregisterSession(session.get().getFirst());
			}
		}
	}

	private static void sendRedirect(HttpServletResponse servletResponse, String target) {
		try {
			servletResponse.sendRedirect(target);
		} catch (IOException e) {
			LOGGER.warn("Failed to send redirect to {}", target, e);
		}
	}

	private static void invalidateSession(HttpSession session) {
		try {
			if (session != null) {
				session.invalidate();
			}
		} catch (Exception e) {
			LOGGER.warn("Error during session closing", e);
		}
	}

	/**
	 * Security exclusion for the SAML Logout servlet.
	 *
	 * @author Adrian Mitev
	 */
	@Extension(target = SecurityExclusion.TARGET_NAME, order = 0.3)
	public static class SAMLLogoutSecurityExclusion implements SecurityExclusion {

		@Override
		public boolean isForExclusion(String path) {
			return path.startsWith(SERVICE_LOGOUT);
		}
	}
}
