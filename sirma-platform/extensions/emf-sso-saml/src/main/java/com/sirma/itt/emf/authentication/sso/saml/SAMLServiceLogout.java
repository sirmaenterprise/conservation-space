package com.sirma.itt.emf.authentication.sso.saml;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Supplier;

import javax.enterprise.event.Event;
import javax.enterprise.inject.spi.BeanManager;
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

import com.sirma.itt.emf.authentication.session.SessionManager;
import com.sirma.itt.emf.security.event.BeginLogoutEvent;
import com.sirma.itt.emf.security.event.UserLogoutEvent;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.resources.security.AuthenticationService;
import com.sirma.itt.seip.rest.secirity.SecurityTokensHolder;
import com.sirma.itt.seip.rest.utils.JwtUtil;
import com.sirma.itt.seip.security.UserStore;
import com.sirma.itt.seip.security.configuration.SecurityExclusion;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.security.exception.AuthenticationException;
import com.sirma.itt.seip.security.util.SecurityUtil;
import com.sirma.itt.seip.util.CDI;

/**
 * Handles SAML Logout requests including single-logout.
 */
@WebServlet(name = "SAML2ServiceLogout", urlPatterns = SAMLServiceLogout.SERVICE_LOGOUT, loadOnStartup = 1)
public class SAMLServiceLogout extends HttpServlet {

	private static final long serialVersionUID = -4749929835873786286L;

	static final String SERVICE_LOGOUT = "/ServiceLogout";

	private static final String SAML_RESPONSE = "SAMLResponse";

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
	private BeanManager beanManager;

	@Inject
	private UserStore userStore;
	@Inject
	private SecurityContextManager securityContextManager;

	@Inject
	private JwtUtil jwtUtil;

	@Inject
	private SecurityTokensHolder tokens;

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
		User currentUser = null;
		HttpSession session = request.getSession();
		try {
			AuthenticationService authenticationService = CDI.instantiateDefaultBean(AuthenticationService.class,
					beanManager);
			currentUser = authenticationService.getCurrentUser();

			if (currentUser == null) {
				// perhaps its a logout request from UI2, try loading user from jwt token
				currentUser = loadUserFromJwt(request);
			}

			LOGGER.debug("Sending SAML logout request: {} with session: {}",
					currentUser == null ? " null" : currentUser.getName(), session.getId());
			if (currentUser == null) {
				invalidateSession(session);
				response.sendRedirect(request.getContextPath());
				return;
			}
			// in multitab scenario, wait for single request to complete
			String id = sessionManager.getClientId(currentUser, request);
			if (sessionManager.isProcessing(id)) {
				Thread.sleep(2000);
				response.sendRedirect(request.getContextPath());
				return;
			}

			logoutInSecurityContext(request, response, currentUser, id);
		} catch (Exception e) {
			LOGGER.warn("Logout error: ", e);
			invalidateSession(session);
			response.sendRedirect(request.getContextPath());
			return;
		}
	}

	private User loadUserFromJwt(HttpServletRequest request) {
		String jwtToken = request.getParameter("jwt");
		if (jwtToken.isEmpty()) {
			return null;
		}

		EmfUser user = new EmfUser(jwtUtil.readUser(securityContextManager, userStore, jwtToken));
		userStore.setUserTicket(user, tokens.getSamlToken(jwtToken));
		// set session index in the http request for successful logout in idp
		String sessionIndex = jwtUtil.extractSessionIndex(jwtToken);
		request.getSession().setAttribute(SAMLMessageProcessor.SAML_KEY_SESSION, sessionIndex);
		return user;
	}

	private void logoutInSecurityContext(HttpServletRequest request, HttpServletResponse response, User currentUser,
			String id) throws IOException {
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

	private void doLogout(HttpServletRequest request, HttpServletResponse response, User currentUser, String id)
			throws IOException {
		sessionManager.beginLogout(id);

		beginLogoutEvent.fire(new BeginLogoutEvent(currentUser));

		String relayTo = request.getParameter("RelayState");
		if (StringUtils.isBlank(relayTo)) {
			relayTo = systemConfiguration.getUi2Url().requireConfigured().get();
		}
		response.sendRedirect(messageProcessor.buildLogoutMessage(currentUser.getName(), relayTo, request));
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

			// after the IdP processed the logout, redirect to AuthenticationServlet with the ui2 url
			// so after login can go in ui2
			StringBuilder uiPath = new StringBuilder(request.getContextPath());
			String relayTo = request.getParameter("RelayState");
			if (StringUtils.isBlank(relayTo)) {
				relayTo = systemConfiguration.getUi2Url().requireConfigured().get();
			}
			uiPath.append("/auth?url=").append(URLEncoder.encode(relayTo, StandardCharsets.UTF_8.name()));
			response.sendRedirect(uiPath.toString());
		} else {
			String encodedRequestMessage = request.getParameter("SAMLRequest");
			LOGGER.debug("IDP SAML request has been received! {}", encodedRequestMessage);
			if (encodedRequestMessage != null) {
				logoutUsingSAMLRequest(encodedRequestMessage);
			} else {
				LOGGER.error("Valid logout SAML response has not been received!");
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
		LogoutRequest processSAMLRequest = SAMLMessageProcessor
				.processSAMLRequest(new String(Base64.decode(encodedRequestMessage), StandardCharsets.UTF_8));
		if (processSAMLRequest != null && processSAMLRequest.getSessionIndexes() != null) {
			String userId = processSAMLRequest.getNameID().getValue();
			final String tenantId = SecurityUtil.getUserAndTenant(userId).getSecond();
			Pair<String, HttpSession> indexAndSession = securityContextManager
					.executeAsTenant(tenantId)
						.supplier(() -> {
				Pair<String, HttpSession> result = null;
				for (SessionIndex nextSession : processSAMLRequest.getSessionIndexes()) {
					String sessionIndex = nextSession.getSessionIndex();
					result = new Pair<>(sessionIndex, sessionManager.getSession(sessionIndex));
					break;
				}
				return result;
			});
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

	private static boolean invalidateSession(HttpSession session) {
		try {
			if (session != null) {
				session.invalidate();
				return true;
			}
		} catch (Exception e) {
			LOGGER.warn("Error during session closing", e);
		}
		return false;
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
