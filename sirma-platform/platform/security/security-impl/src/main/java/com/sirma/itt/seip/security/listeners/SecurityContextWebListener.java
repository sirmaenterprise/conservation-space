package com.sirma.itt.seip.security.listeners;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.security.authentication.AuthenticationContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.security.exception.AuthenticationException;

/**
 * A request listener responsible for creating and destroying security context
 * for a request.
 *
 * @author yasko
 */
@Singleton
@WebListener
public class SecurityContextWebListener implements ServletRequestListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityContextWebListener.class);
	@Inject
	private SecurityContextManager securityContextManager;

	/**
	 * Initialize a security context for the current request (if possible).
	 */
	@Override
	public void requestInitialized(ServletRequestEvent event) {
		HttpServletRequest request = (HttpServletRequest) event.getServletRequest();

		// TODO: fast check if context *can* be created
		// TODO: cache the initialized context with a very short TTL to speed up successive requests?
		try {
			securityContextManager.initializeExecution(AuthenticationContext.create(request));
		} catch (AuthenticationException e) {
			LOGGER.trace("User not authenticated or invalid token for {}", e.getFailedIdentity(), e);
		}
	}

	/**
	 * Destroy current (active) security context for current request.
	 */
	@Override
	public void requestDestroyed(ServletRequestEvent event) {
		if (securityContextManager.getCurrentContext().isActive()) {
			securityContextManager.endExecution();
		}
	}
}