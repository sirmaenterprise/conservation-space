package com.sirma.itt.emf.authentication.sso.saml;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Map;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.sirma.itt.emf.authentication.session.SessionManager;
import com.sirma.itt.emf.plugin.ExtensionPoint;
import com.sirma.itt.emf.resources.ResourceProviderExtension;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.security.event.UserAuthenticatedEvent;
import com.sirma.itt.emf.security.model.UserWithCredentials;

/**
 * Handles Single Sign-On requests. Called by the IdP.
 */
@WebServlet(value = SAMLServiceLogin.SERVICE_LOGIN, urlPatterns = SAMLServiceLogin.SERVICE_LOGIN, name = "SAML2ServiceLogin", description = "SAML2ServiceLogin")
public class SAMLServiceLogin extends HttpServlet {

	private static final long serialVersionUID = -3656004764256057270L;

	static final String SERVICE_LOGIN = "/ServiceLogin";

	static final String RETURN_URL = "return_url";

	private static final String SAML_RESPONSE = "SAMLResponse";

	private static final Logger LOGGER = Logger.getLogger(SAMLServiceLogin.class);

	private static final boolean TARCE_ENABLED = LOGGER.isTraceEnabled();

	@Inject
	private SAMLMessageProcessor messageProcessor;

	@Inject
	private ResourceService resourceService;

	@Inject
	private Event<UserAuthenticatedEvent> authenticatedEvent;

	@Inject
	@ExtensionPoint(value = ResourceProviderExtension.TARGET_NAME)
	private Iterable<ResourceProviderExtension> resourceProviders;

	@Inject
	private SessionManager sessionManager;

	/**
	 * Called by the IdP when the user has successfully authenticated. Parses the SAML response and
	 * authenticated the user in the system.
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
		String relayState = request.getParameter("RelayState");

		if (responseMessage != null) { /* response from the identity provider */
			// if no relay state is provided, probably there is an logout attempt, forward to
			// ServiceLogout
			if (relayState == null) {
				LOGGER.warn("relayState is not provided");
				request.getRequestDispatcher("/ServiceLogout?" + SAML_RESPONSE + "="
						+ responseMessage);
				return;
			}

			if (TARCE_ENABLED) {
				LOGGER.trace("SAMLResponse received from IDP");
			}

			Map<String, String> result = null;
			try {
				result = messageProcessor.processSAMLResponse(responseMessage);
			} catch (SAMLMessageValidationException e) {
				response.sendError(400, "Invalid security token provided");
				return;
			}

			// Fetch user profile from bean manager and notify the
			// application that the user has successfully authenticated
			UserWithCredentials user = null;
			try {
				// authenticate as admin to fetch the user data
				SecurityContextManager.authenticateAsAdmin();
				// REVIEW: change people retrieval to be from LDAP directly
				// not via DMS
				String userId = result.get("Subject");
				Resource resource = resourceService.getResource(userId, ResourceType.USER);
				if (resource instanceof UserWithCredentials) {
					user = (UserWithCredentials) resource;
				}
				if (user == null) {
					// try to search the resource from some of the providers and if found - add it
					// to emf
					for (ResourceProviderExtension nextProvider : resourceProviders) {
						if (nextProvider.isApplicable(ResourceType.USER)) {
							Resource synchedUser = nextProvider.getResource(userId, true);
							if (synchedUser != null) {
								Resource createdResource = resourceService
										.getOrCreateResource(synchedUser);
								if (createdResource instanceof UserWithCredentials) {
									user = (UserWithCredentials) createdResource;
								}
							}
						}
					}
					if (user == null) {
						response.sendError(500, "User '" + userId + "' not found in the system");
						return;
					}
					// for some reason the user does not exists
				}
				HttpSession session = request.getSession();
				String sessionIndex = result.get("SessionIndex");
				if (sessionIndex != null) {
					session.setAttribute("SessionIndex", sessionIndex);
					sessionManager.registerSession(sessionIndex, session);
				}
				LOGGER.debug("Logged-in user: " + userId + " with session: " + sessionIndex);
				user.setTicket(SecurityContextManager.encrypt(responseMessage));
				sessionManager.trackUser(session, user.getIdentifier(),
						request.getHeader("User-Agent"));
			} finally {
				// restore the security context for the current thread
				SecurityContextManager.clearCurrentSecurityContext();
			}

			// copy all properties to the user info map and notify that the user has authenticated
			user.getProperties().putAll(result);

			authenticatedEvent.fire(new UserAuthenticatedEvent(user));
			// get return URL from the RelayState parameter
			if ((relayState != null) && !relayState.equals("null")) {
				response.sendRedirect(URLDecoder.decode(relayState, "UTF-8"));
			} else {
				// redirect to home page if no return url is provided
				response.sendRedirect(request.getContextPath());
			}
		}
	}
}
