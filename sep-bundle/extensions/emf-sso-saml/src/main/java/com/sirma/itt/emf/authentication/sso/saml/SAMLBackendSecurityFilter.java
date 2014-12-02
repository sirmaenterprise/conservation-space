/**
 *
 */
package com.sirma.itt.emf.authentication.sso.saml;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.SecurityTokenService;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.security.event.UserAuthenticatedEvent;
import com.sirma.itt.emf.security.event.UserLogoutEvent;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.security.model.UserWithCredentials;

/**
 * Filter used to authenticate a request when trying to access specific entity in emf. This could be
 * case, document, section, etc. One has to provide username and password custom headers in order to
 * successfully authenticate. The idp is called useing the provided username and pass and an emf
 * login event is thrown.
 * 
 * @author Ivo Rusev
 */
@ApplicationScoped
@WebFilter(filterName = "Backend saml login", urlPatterns = "/entity/*")
public class SAMLBackendSecurityFilter implements Filter {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(SAMLBackendSecurityFilter.class);
	/** The Constant USERNAME. */
	private static final String USERNAME = "username";
	/** The Constant PASSWORD. */
	private static final String PASSWORD = "password";

	/** The constant token. */
	private static final String TOKEN = "ssoToken";
	/** The Constant LINE_SEPARATOR. */
	private static final Pattern LINE_SEPARATOR = Pattern.compile("lineSeparator");
	/** The security token service. */
	@Inject
	private SecurityTokenService securityTokenService;
	/** The message processor. */
	@Inject
	private SAMLMessageProcessor messageProcessor;
	/** The resource service. */
	@Inject
	private ResourceService resourceService;
	/** The authenticated event. */
	@Inject
	private Event<UserAuthenticatedEvent> authenticatedEvent;
	/** The logout event. */
	@Inject
	protected Event<UserLogoutEvent> logoutEvent;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		final HttpServletRequest httpRequest = (HttpServletRequest) request;

		final String username = httpRequest.getHeader(USERNAME);
		final String password = httpRequest.getHeader(PASSWORD);
		final String headerToken = httpRequest.getHeader(TOKEN);

		if (StringUtils.isNotNullOrEmpty(headerToken)) {
			// restore new lines.
			// changed to use pre-compiled pattern
			final String encryptedToken = LINE_SEPARATOR.matcher(headerToken).replaceAll(
					System.lineSeparator());
			final String decryptedToken = SecurityContextManager.decrypt(encryptedToken);
			executeAuthenticated(request, response, chain, encryptedToken, decryptedToken);
			return;
		}

		if (StringUtils.isNullOrEmpty(username) || StringUtils.isNullOrEmpty(password)) {
			chain.doFilter(request, response);
			return;
		}
		// Get the token.
		String decryptedToken = null;
		try {
			decryptedToken = securityTokenService.requestToken(username, password);
		} catch (Exception e) {
			LOGGER.error("Security token request failed", e);
		}
		if (StringUtils.isNullOrEmpty(decryptedToken)) {
			chain.doFilter(request, response);
			return;
		}

		executeAuthenticated(request, response, chain,
				SecurityContextManager.encrypt(decryptedToken), decryptedToken);
	}

	/**
	 * Execute filter chain in authenticated context. The method will create authentication context
	 * before filter chaining and will clear it at the end.
	 * 
	 * @param request
	 *            the request
	 * @param response
	 *            the response
	 * @param chain
	 *            the chain
	 * @param encryptedToken
	 *            the encrypted token
	 * @param decryptedToken
	 *            the decrypted token
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws ServletException
	 *             the servlet exception
	 */
	private void executeAuthenticated(ServletRequest request, ServletResponse response,
			FilterChain chain, final String encryptedToken, String decryptedToken)
			throws IOException, ServletException {
		// try to authenticate if the user is found
		Resource resource = authenticateWithToken(encryptedToken, decryptedToken);
		if (resource != null) {
			try {
				chain.doFilter(request, response);
			} finally {
				// clear the authentication data after execution
				// XXX: this is disabled due to the fact it breaks the IDOC export because after
				// current filter execution there 2 more JSF requests that fail to authenticate
				// clearAuthentication(resource);
			}
		}
	}

	/**
	 * Clear the current authentication.
	 * 
	 * @param resource
	 *            the resource
	 */
	private void clearAuthentication(Resource resource) {
		logoutEvent.fire(new UserLogoutEvent((User) resource));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void destroy() {
	}

	/**
	 * Authenticates user with a given IDP token.
	 * 
	 * @param encryptedToken
	 *            the base64 encrypted token
	 * @param decryptedToken
	 *            the decrypted token.
	 * @return the resource
	 */
	private Resource authenticateWithToken(final String encryptedToken, final String decryptedToken) {
		SecurityContextManager.authenticateAsAdmin();
		// get the processed token so we can extract useriD
		Map<String, String> processedToken = messageProcessor.processSAMLResponse(decryptedToken);
		String userId = processedToken.get("Subject");

		UserWithCredentials user = null;
		Resource resource = resourceService.getResource(userId, ResourceType.USER);
		if (resource instanceof UserWithCredentials) {
			user = (UserWithCredentials) resource;
		}
		if (user == null) {
			LOGGER.error("No user with username {} is found.", userId);
			return null;
			// for some reason the user does not exist in the
		}
		user.setTicket(encryptedToken);
		user.getProperties().putAll(processedToken);
		authenticatedEvent.fire(new UserAuthenticatedEvent(user));
		return user;
	}

}
