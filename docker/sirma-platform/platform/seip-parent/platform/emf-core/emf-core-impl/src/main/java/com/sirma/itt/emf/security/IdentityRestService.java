/**
 * Copyright (c) 2014 26.05.2014 , Sirma ITT. /* /**
 */

package com.sirma.itt.emf.security;

import java.util.Map;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.json.JSONObject;

import com.sirma.itt.emf.security.event.UserAuthenticatedEvent;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.rest.annotations.security.PublicResource;
import com.sirma.itt.seip.security.authentication.AuthenticationContext;
import com.sirma.itt.seip.security.authentication.Authenticator;

/**
 * Restful service for identity operations.
 *
 * @author Adrian Mitev
 */
@Path("/identity")
public class IdentityRestService {

	@Inject
	private Event<UserAuthenticatedEvent> authenticatedEvent;

	@Inject
	private Authenticator authenticator;

	/**
	 * Authenticates an user within the system. If the login is successful a cookie is sent to the browser with the
	 * browser session id.
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
	@PublicResource
	public String login(@QueryParam("username") String username, @QueryParam("password") String password) {

		Map<String, String> properties = CollectionUtils.createHashMap(2);
		properties.put(Authenticator.USERNAME, username);
		properties.put(Authenticator.CREDENTIAL, password);

		com.sirma.itt.seip.security.User identity = authenticator
				.authenticate(AuthenticationContext.create(properties));

		// copy all properties to the user info map and notify that the user has
		// authenticated
		authenticatedEvent.fire(new UserAuthenticatedEvent((User) identity));

		JSONObject response = new JSONObject();
		JsonUtil.addToJson(response, "message", "success");
		return response.toString();
	}
}
