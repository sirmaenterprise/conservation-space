package com.sirma.itt.emf.authentication.sso.saml;

import java.util.Map;
import java.util.concurrent.Callable;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.BackendAuthenticator;
import com.sirma.itt.emf.security.SecurityTokenService;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.security.event.UserAuthenticatedEvent;
import com.sirma.itt.emf.security.event.UserLogoutEvent;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.security.model.UserWithCredentials;

/**
 * Wrapper class for authentication mechanisms related to rest services or servlets.
 * 
 * @author bbanchev
 * @author BBonev
 */
@ApplicationScoped
public class SAMLBackendAuthenticator implements BackendAuthenticator {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(SAMLBackendAuthenticator.class);

	/** The Constant AUTHENTICATED_USER. */
	private static final ThreadLocal<User> AUTHENTICATED_USER = new ThreadLocal<User>();
	/** The security token service. */
	@Inject
	protected SecurityTokenService securityTokenService;
	/** The message processor. */
	@Inject
	protected SAMLMessageProcessor messageProcessor;
	/** The resource service. */
	@Inject
	protected ResourceService resourceService;
	/** The authenticated event. */
	@Inject
	protected Event<UserAuthenticatedEvent> authenticatedEvent;
	/** The logout event. */
	@Inject
	protected Event<UserLogoutEvent> logoutEvent;

	/**
	 * Do an authentication in the system. If some data is not valid, false is returned, otherwise
	 * true
	 *
	 * @param request
	 *            is the http request
	 * @param response
	 *            is the http response
	 * @param username
	 *            is the user to authenticate. Should be provided only if pass is provided as well
	 * @param password
	 *            is the password
	 * @param encryptedToken
	 *            is the saml2 authenication response of an user.
	 * @return true if authentication event is fired
	 */
	@Override
	public boolean doAuthenticate(HttpServletRequest request, HttpServletResponse response,
			final String username, final String password, final String encryptedToken) {
		if (StringUtils.isNotNullOrEmpty(encryptedToken)) {
			return authenticateWithToken(encryptedToken,
					SecurityContextManager.decrypt(encryptedToken));
		}

		if (StringUtils.isNullOrEmpty(username) || StringUtils.isNullOrEmpty(password)) {
			return false;
		}
		// Get the token.
		String decryptedToken = null;
		try {
			decryptedToken = securityTokenService.requestToken(username, password);
		} catch (Exception e) {
			LOGGER.error("Security token request failed", e);
		}
		if (StringUtils.isNullOrEmpty(decryptedToken)) {
			return false;
		}

		return authenticateWithToken(SecurityContextManager.encrypt(decryptedToken), decryptedToken);
	}

	/**
	 * Authenticates user with a given IDP token.
	 * 
	 * @param encryptedToken
	 *            the base64 encrypted token
	 * @param decryptedToken
	 *            the decrypted token.
	 * @return true, if successfully authenticated the user
	 */
	private boolean authenticateWithToken(final String encryptedToken, final String decryptedToken) {
		SecurityContextManager.authenticateAsAdmin();
		// get the processed token so we can extract useriD
		Map<String, String> processedToken = messageProcessor.processSAMLResponse(decryptedToken);
		String userId = processedToken.get("Subject");
		// TODO check tocken validity
		UserWithCredentials user = null;
		Resource resource = resourceService.getResource(userId, ResourceType.USER);
		if (resource instanceof UserWithCredentials) {
			user = (UserWithCredentials) resource;
		}
		if (user == null) {
			LOGGER.error("No user with username {} is found.", userId);
			return false;
			// for some reason the user does not exist in the
		}
		user.setTicket(encryptedToken);
		user.getProperties().putAll(processedToken);
		authenticatedEvent.fire(new UserAuthenticatedEvent(user));

		// store the authenticated user in the current thread until the clearAuthentication is
		// called to reset it and to logout the user
		AUTHENTICATED_USER.set(user);
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearAuthentication() {
		User authenticatedUser = AUTHENTICATED_USER.get();
		if (authenticatedUser != null) {
			logoutEvent.fire(new UserLogoutEvent(authenticatedUser));
			AUTHENTICATED_USER.set(null);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <E> E executeAuthenticated(Callable<E> callable, String username, String password,
			String encryptedToken) {
		if (doAuthenticate(null, null, username, password, encryptedToken)) {
			try {
				return callable.call();
			} catch (Exception e) {
				throw new EmfRuntimeException(e);
			} finally {
				clearAuthentication();
			}
		}
		return null;
	}
}
