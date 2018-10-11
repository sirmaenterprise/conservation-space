package com.sirma.itt.emf.authentication.sso.saml;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
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

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.security.event.UserAuthenticatedEvent;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.rest.session.SessionManager;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.authentication.AuthenticationContext;
import com.sirma.itt.seip.security.authentication.AuthenticationResponseDecorator;
import com.sirma.itt.seip.security.authentication.Authenticator;
import com.sirma.itt.seip.security.configuration.SecurityExclusion;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.security.exception.AccountDisabledException;
import com.sirma.itt.seip.security.exception.AuthenticationException;
import com.sirma.itt.seip.security.exception.SecurityException;
import com.sirma.itt.seip.security.util.SecurityUtil;

/**
 * Handles Single Sign-On requests. Called by the IdP.
 */
@WebServlet(value = SAMLServiceLogin.SERVICE_LOGIN, urlPatterns = SAMLServiceLogin.SERVICE_LOGIN,
		name = "SAML2ServiceLogin", description = "SAML2ServiceLogin")
public class SAMLServiceLogin extends HttpServlet {

	static final String RELAY_STATE = "RelayState";
	private static final long serialVersionUID = -3656004764256057270L;
	static final String SERVICE_LOGIN = "/ServiceLogin";
	static final String SAML_RESPONSE = "SAMLResponse";
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

	@Inject
	private SystemConfiguration systemConfiguration;

	/**
	 * Called by the IdP when the user has successfully authenticated. Parses the SAML response and authenticated the
	 * user in the system.
	 *
	 * @param request the request
	 * @param response the response
	 * @throws ServletException the servlet exception
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		String responseMessage = request.getParameter(SAML_RESPONSE); /* response from the identity provider */
		String relayState = request.getParameter(RELAY_STATE);

		if (responseMessage == null) {
			sendError(response, HttpStatus.SC_BAD_REQUEST, SAML_RESPONSE + " parameter is missing from request");
			return;
		}
		if (relayState == null) {
			sendError(response, HttpStatus.SC_BAD_REQUEST, RELAY_STATE + " parameter is missing from request");
			return;
		}

		LOGGER.trace("SAMLResponse received from IDP");
		try {
			AuthenticationContext authenticationContext = buildAuthenticationContext(responseMessage);
			securityContextManager.initializeExecution(authenticationContext);
		} catch (AccountDisabledException e) {
			LOGGER.warn(e.getMessage());
			LOGGER.trace("", e);
			onDisabledUser(request, response, e.getIdentity());
			return;
		} catch (AuthenticationException e) {
			LOGGER.warn("Could not authenticate user {} due to: ", e.getFailedIdentity(), e.getMessage());
			LOGGER.trace("", e);
			onFailedAuthentication(request, response, e);
			return;
		} catch (SecurityException e) {
			LOGGER.warn("Could not finish authentication ", e);
			invalidateSession(request.getSession());
			sendError(response, HttpStatus.SC_INTERNAL_SERVER_ERROR, "Could not finish authentication.");
			return;
		}

		try {
			onSuccessfulAuthentication(request, response, relayState);
		} finally {
			securityContextManager.endContextExecution();
		}
	}

	private static AuthenticationContext buildAuthenticationContext(String responseMessage) {
		Map<String, String> properties = CollectionUtils.createHashMap(2);
		properties.put(Authenticator.TOKEN, responseMessage);
		properties.put(Authenticator.FORCE_AUTHENTICATION, "true");
		return AuthenticationContext.create(properties);
	}

	/**
	 * Redirect to logout and relay to the disabled user page
	 */
	private void onDisabledUser(HttpServletRequest request, HttpServletResponse response, User identity) {
		if (identity != null) {
			Serializable sessionIndex = SAMLMessageProcessor.getSessionIndex(identity.getProperties());
			String baseAddress = securityContextManager.executeAsTenant(identity.getTenantId())
					.function(this::getRedirectAddress, request);
			String relayTo = null;
			try {
				if (!baseAddress.endsWith("/")) {
					baseAddress += "/";
				}
				relayTo = baseAddress + "#/public/disabledUser?id=" + URLEncoder.encode(
						identity.getIdentityId(), StandardCharsets.UTF_8.name());
			} catch (UnsupportedEncodingException e) {
				LOGGER.warn(e.getMessage(), e);
			}
			if (sessionIndex != null) {
				// if the method is called with a null for session index an exception will be thrown
				relayTo = messageProcessor.buildLogoutMessage(identity.getIdentityId(), relayTo, request,
						sessionIndex.toString());
			} else {
				LOGGER.warn(
						"No session index is found in the identity. Will not perform logout, but will redirect directly to {}",
						relayTo);
			}
			sendRedirect(response, relayTo);
		}
		invalidateSession(request.getSession());
	}

	private void onFailedAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException e) {
		if (e.getFailedIdentity() != null && e.getSsoSessionId() != null) {
			String tenantId = SecurityUtil.getUserAndTenant(e.getFailedIdentity()).getSecond();
			String logoutRedirect = securityContextManager.executeAsTenant(tenantId)
					.supplier(() -> buildLogoutMessage(e.getFailedIdentity(), e.getSsoSessionId(), request));
			sendRedirect(response, logoutRedirect);
		}
		invalidateSession(request.getSession());
	}

	private void onSuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			String relayState) {
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

		sendRedirect(response, getRedirectOnSuccessAuthentication(relayState, user, request));
	}

	private String getRedirectOnSuccessAuthentication(String relayState, com.sirma.itt.seip.resources.User user,
			HttpServletRequest request) {
		String redirect;
		try {
			redirect = new String(Base64.getDecoder().decode(relayState), StandardCharsets.UTF_8);
		} catch (IllegalArgumentException e) {
			LOGGER.warn("Could not parse the requested relay state: {}. Redirecting to home", relayState);
			LOGGER.trace("Could not parse the requested relay state: {}. Redirecting to home", relayState, e);
			redirect = getRedirectAddress(request);
		}

		if ("null".equals(redirect)) {
			// redirect to home page if no return url is provided
			redirect = getRedirectAddress(request);
		}
		redirect = decorate(redirect, user);
		try {
			return URLDecoder.decode(redirect, StandardCharsets.UTF_8.name());
		} catch (Exception e) {
			LOGGER.warn("Invalid relay state {}. Redirecting to home", redirect, e);
		}
		return redirect;
	}

	private String decorate(String relayState, com.sirma.itt.seip.resources.User user) {
		if (!decorators.isUnsatisfied()) {
			Map<String, Object> properties = new HashMap<>(5);
			properties.put(RELAY_STATE, relayState);
			properties.put("user", user);

			decorators.forEach(decorator -> decorator.decorate(properties));

			return (String) properties.get(RELAY_STATE);
		}
		return relayState;
	}

	/**
	 * Build a logout message for idp saml2 processor using the arguments.
	 */
	private String buildLogoutMessage(String userId, String ssoSessionId, HttpServletRequest request) {
		return messageProcessor.buildLogoutMessage(userId, getRedirectAddress(request), request, ssoSessionId);
	}

	private String getRedirectAddress(HttpServletRequest request) {
		// redirect to the UI2 if configured or to the backend if not
		return systemConfiguration.getUi2Url().computeIfNotSet(request::getContextPath);
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

	private static void sendRedirect(HttpServletResponse response, String redirectDestination) {
		try {
			response.sendRedirect(redirectDestination);
		} catch (IOException e) {
			LOGGER.warn("Failed to redirect to {}", redirectDestination, e);
		}
	}

	private static void sendError(HttpServletResponse response, int statusCode, String redirectDestination) {
		try {
			response.sendError(statusCode, redirectDestination);
		} catch (IOException e) {
			LOGGER.warn("Failed to redirect to {}", redirectDestination, e);
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
