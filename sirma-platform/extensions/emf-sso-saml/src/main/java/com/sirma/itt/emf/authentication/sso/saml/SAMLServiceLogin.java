package com.sirma.itt.emf.authentication.sso.saml;

import java.io.IOException;
import java.io.Serializable;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.authentication.session.SessionManager;
import com.sirma.itt.emf.security.event.UserAuthenticatedEvent;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.authentication.AuthenticationContext;
import com.sirma.itt.seip.security.authentication.AuthenticationResponseDecorator;
import com.sirma.itt.seip.security.authentication.Authenticator;
import com.sirma.itt.seip.security.configuration.SecurityExclusion;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.security.exception.AuthenticationException;

/**
 * Handles Single Sign-On requests. Called by the IdP.
 */
@WebServlet(value = SAMLServiceLogin.SERVICE_LOGIN, urlPatterns = SAMLServiceLogin.SERVICE_LOGIN, name = "SAML2ServiceLogin", description = "SAML2ServiceLogin")
public class SAMLServiceLogin extends HttpServlet {

	private static final String RELAY_STATE = "RelayState";
	private static final long serialVersionUID = -3656004764256057270L;
	static final String SERVICE_LOGIN = "/ServiceLogin";
	static final String RETURN_URL = "return_url";
	private static final String SAML_RESPONSE = "SAMLResponse";
	private static final Logger LOGGER = LoggerFactory.getLogger(SAMLServiceLogin.class);

	@Inject
	private SessionManager sessionManager;

	@Inject
	private SecurityContextManager securityContextManager;

	@Inject
	private SAMLMessageProcessor messageProcessor;

	@Inject
	private Instance<AuthenticationResponseDecorator> decorators;

	@Inject
	private Event<UserAuthenticatedEvent> authenticatedEvent;

	/**
	 * Called by the IdP when the user has successfully authenticated. Parses the SAML response and authenticated the
	 * user in the system.
	 *
	 * @param request
	 *            the request
	 * @param response
	 *            the response
	 * @throws ServletException
	 *             the servlet exception
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String responseMessage = request.getParameter(SAML_RESPONSE);
		String relayState = request.getParameter(RELAY_STATE);

		if (responseMessage != null) { /* response from the identity provider */
			// if no relay state is provided, probably there is an logout
			// attempt, forward to
			// ServiceLogout
			if (relayState == null) {
				LOGGER.warn("relayState is not provided");
				request.getRequestDispatcher("/ServiceLogout?" + SAML_RESPONSE + "=" + responseMessage);
				return;
			}

			LOGGER.trace("SAMLResponse received from IDP");
			Map<String, String> properties = CollectionUtils.createHashMap(2);
			properties.put(Authenticator.TOKEN, responseMessage);
			properties.put(Authenticator.FORCE_AUTHENTICATION, "true");
			AuthenticationContext authenticationContext = AuthenticationContext.create(properties);
			try {
				securityContextManager.initializeExecution(authenticationContext);
			} catch (AuthenticationException e) {
				LOGGER.warn("Could not authenticate user {}", e.getFailedIdentity(), e);
				if (e.getFailedIdentity() != null || e.getSsoSessionId() != null) {
					response.sendRedirect(buildLogoutMessage(e.getFailedIdentity(), e.getSsoSessionId(), request));
				}
				invalidateSession(request.getSession());
				return;
			} catch (SecurityException e) {
				LOGGER.warn("Could not finish authentication ", e);
				invalidateSession(request.getSession());
				return;
			}
			try {
				onSuccessfulAuthentication(request, response, relayState);
			} finally {
				securityContextManager.endExecution();
			}
		}
	}

	private void onSuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, String relayState)
			throws IOException {
		User authenticated = securityContextManager.getCurrentContext().getAuthenticated();
		// Fetch user profile from bean manager and notify the

		HttpSession session = request.getSession();
		Serializable sessionIndex = SAMLMessageProcessor.getSessionIndex(authenticated.getProperties());
		if (sessionIndex != null) {
			session.setAttribute(SAMLMessageProcessor.SAML_KEY_SESSION, sessionIndex.toString());
			sessionManager.registerSession(sessionIndex.toString(), session);
		}
		LOGGER.debug("Logged-in user: {} with session: {}", authenticated.getIdentityId(), sessionIndex);

		sessionManager.trackUser(session, authenticated.getIdentityId(), request.getHeader("User-Agent"));

		com.sirma.itt.seip.resources.User user = null;
		if (authenticated instanceof com.sirma.itt.seip.resources.User) {
			user = (com.sirma.itt.seip.resources.User) authenticated;
		}

		authenticatedEvent.fire(new UserAuthenticatedEvent(authenticated, true));

		String redirect = new String(Base64.getDecoder().decode(relayState), StandardCharsets.UTF_8);

		// get return URL from the RelayState parameter
		if (!"null".equals(redirect)) {
			if (!decorators.isUnsatisfied()) {
				redirect = decorate(redirect, user);
			}
			response.sendRedirect(URLDecoder.decode(redirect, StandardCharsets.UTF_8.name()));
		} else {
			// redirect to home page if no return url is provided
			response.sendRedirect(request.getContextPath());
		}
	}

	private String decorate(String relayState, com.sirma.itt.seip.resources.User user) {
		Map<String, Object> properties = new HashMap<>(5);
		properties.put(RELAY_STATE, relayState);
		properties.put("user", user);

		decorators.forEach(decorator -> decorator.decorate(properties));

		return (String) properties.get(RELAY_STATE);
	}

	/**
	 * Build a logout message for idp saml2 processor using the arguments.
	 *
	 * @param userId
	 *            is user that would be logged out
	 * @param ssoSessionId
	 *            the sso session id
	 * @param request
	 *            is the http request
	 * @return the builder url for the request.
	 */
	private String buildLogoutMessage(String userId, String ssoSessionId, HttpServletRequest request) {
		return messageProcessor.buildLogoutMessage(userId, request.getContextPath(), request, ssoSessionId);
	}

	private static void invalidateSession(HttpSession session) {
		try {
			if (session != null) {
				session.invalidate();
			}
		} catch (Exception e) {
			LOGGER.warn("Error during session invalidation", e);
		}
	}

	/**
	 * Security exclusion for the SAML Login servlet.
	 *
	 * @author Adrian Mitev
	 */
	@Extension(target = SecurityExclusion.TARGET_NAME, order = 0.2)
	public static class SAMLLoginSecurityExclusion implements SecurityExclusion {

		@Override
		public boolean isForExclusion(String path) {
			return path.startsWith(SERVICE_LOGIN);
		}
	}
}
