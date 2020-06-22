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
import com.sirma.itt.seip.security.exception.NoPermissionsException;

/**
 * JAX-RS filter responsible for asserting that the currently authenticated user has administrator permissions to access
 * REST endpoints annotated with {@link com.sirma.itt.seip.rest.annotations.security.AdminResource}. Otherwise rejects
 * access.
 *
 * @author smustafov
 */
@Singleton
@Priority(Priorities.AUTHORIZATION + 10)
public class AdminFilter implements ContainerRequestFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private SecurityContextManager securityContextManager;

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		if (!securityContextManager.isAuthenticatedAsAdmin()) {
			LOGGER.warn(
					"Non-admin user tried to access admin resource: \n\t{} {}\n\tParams: {}\n\tCookies: {}\n\tHeaders: {}",
					requestContext.getMethod(), requestContext.getUriInfo().getPath(),
					requestContext.getUriInfo().getQueryParameters(), requestContext.getCookies().values(),
					requestContext.getHeaders());
			throw new NoPermissionsException(requestContext.getUriInfo().getPath(),
					"Unauthorized access to: " + requestContext.getMethod() + " "
							+ requestContext.getUriInfo().getPath());
		}
	}

}
