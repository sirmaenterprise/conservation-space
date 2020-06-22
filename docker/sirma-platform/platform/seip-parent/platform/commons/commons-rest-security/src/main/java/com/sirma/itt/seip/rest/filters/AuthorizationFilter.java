package com.sirma.itt.seip.rest.filters;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.security.exception.AuthenticationException;

/**
 * JAX-RS Request filter with priority {@link Priorities#AUTHORIZATION}.
 * Responsible for asserting that there is a authorized user for the request.
 *
 * @author yasko
 */
@Singleton
@Priority(Priorities.AUTHORIZATION)
public class AuthorizationFilter implements ContainerRequestFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	protected final SecurityContextManager securityContextManager;

	/**
	 * Instantiate new authorization filter using the given security context manager
	 *
	 * @param securityContextManager
	 *            the security context manager to use
	 */
	@Inject
	public AuthorizationFilter(SecurityContextManager securityContextManager) {
		this.securityContextManager = securityContextManager;
	}

	@Override
	public void filter(ContainerRequestContext request) throws IOException {
		if (!securityContextManager.getCurrentContext().isActive()) {
			LOGGER.warn("Unauthorized access to: \n\t{} {}\n\tParams: {}\n\tCookies: {}\n\tHeaders: {}",
					request.getMethod(), request.getUriInfo().getPath(), request.getUriInfo().getQueryParameters(),
					request.getCookies().values(), request.getHeaders());
			throw new AuthenticationException(
					"Unauthorized access to: " + request.getMethod() + " " + request.getUriInfo().getPath());
		}
	}
}
