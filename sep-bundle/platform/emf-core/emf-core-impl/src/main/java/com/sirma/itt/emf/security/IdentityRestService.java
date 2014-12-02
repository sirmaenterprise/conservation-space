/**
 * Copyright (c) 2014 26.05.2014 , Sirma ITT. /* /**
 */

package com.sirma.itt.emf.security;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.rest.EmfApplicationException;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.security.event.UserAuthenticatedEvent;
import com.sirma.itt.emf.security.model.UserWithCredentials;

/**
 * Restful service for identity operations.
 * 
 * @author Adrian Mitev
 */
@Path("/identity")
public class IdentityRestService {

	@Inject
	private SecurityTokenService securityTokenService;

	@Inject
	private ResourceService resourceService;

	@Inject
	private Event<UserAuthenticatedEvent> authenticatedEvent;

	/**
	 * Authenticates an user within the system. If the login is successful a cookie is sent to the
	 * browser with the browser session id.
	 * 
	 * @param username
	 *            username
	 * @param password
	 *            password
	 * @return status message on success.
	 */
	@GET
	@Path("/login")
	@Produces(MediaType.APPLICATION_JSON)
	public String login(@QueryParam("username") String username,
			@QueryParam("password") String password) {
		String token = null;
		try {
			token = securityTokenService.requestToken(username, password);
		} catch (Exception e) {
			throw new EmfApplicationException("Security token request failed", e);
		}
		if (token == null) {
			throw new EmfApplicationException("Invalid username/password or SSO configuration");
		}

		UserWithCredentials user = null;
		try {
			// authenticate as admin to fetch the user data
			SecurityContextManager.authenticateAsAdmin();
			Resource resource = resourceService.getResource(username, ResourceType.USER);
			if (resource instanceof UserWithCredentials) {
				user = (UserWithCredentials) resource;
			} else {
				throw new EmfApplicationException("User not found in the system");
			}

			user.setTicket(SecurityContextManager.encrypt(token));
		} finally {
			// restore the security context for the current thread
			SecurityContextManager.clearCurrentSecurityContext();
		}

		// copy all properties to the user info map and notify that the user has
		// authenticated
		authenticatedEvent.fire(new UserAuthenticatedEvent(user));

		return "{ message: \"success\"}";
	}
}
