package com.sirma.itt.seip.rest.filters;

import java.io.IOException;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.security.UserStore;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Notify the authenticated user for incoming header properties and languages. The filter ensures the
 * {@link UserStore#setRequestProperties(com.sirma.itt.seip.security.User, com.sirma.itt.seip.security.UserStore.RequestInfo)}
 * is called
 *
 * @author BBonev
 */
@Singleton
@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class CustomHeaderInterceptorFilter implements ContainerRequestFilter {

	@Inject
	private UserStore userStore;

	@Inject
	private SecurityContext securityContext;

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		if (securityContext.isActive()) {
			userStore.setRequestProperties(securityContext.getAuthenticated(),
					UserStore.RequestInfo.create(requestContext.getAcceptableLanguages(), requestContext.getHeaders()));
		}
	}
}
