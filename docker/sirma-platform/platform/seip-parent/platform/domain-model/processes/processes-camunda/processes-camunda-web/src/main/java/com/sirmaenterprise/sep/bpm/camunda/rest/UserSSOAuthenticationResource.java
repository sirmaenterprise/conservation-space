/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.sirmaenterprise.sep.bpm.camunda.rest;

import static org.camunda.bpm.engine.authorization.Permissions.ACCESS;
import static org.camunda.bpm.engine.authorization.Resources.APPLICATION;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.webapp.impl.security.auth.Authentication;
import org.camunda.bpm.webapp.impl.security.auth.AuthenticationDto;
import org.camunda.bpm.webapp.impl.security.auth.Authentications;
import org.camunda.bpm.webapp.impl.security.auth.UserAuthenticationResource;

import com.sirmaenterprise.sep.bpm.camunda.security.SSOAuthenticator;

/**
 * Jax-Rs resource allowing users to authenticate with username and password
 * </p>
 * . <br>
 * Extend base code by integration of {@link SSOAuthenticator}
 * 
 * @author Daniel Meyer
 * @author bbanchev
 */
@Path(UserAuthenticationResource.PATH)
public class UserSSOAuthenticationResource {

	@Context
	protected HttpServletRequest request;
	@Inject
	private SSOAuthenticator ssoAuthenticator;

	/**
	 * Gets the authenticated user.
	 *
	 * @param engineName
	 *            the engine name
	 * @return the authenticated user
	 */
	@GET
	@Path("/{processEngineName}")
	public Response getAuthenticatedUser(@PathParam("processEngineName") String engineName) {

		Authentications allAuthentications = Authentications.getCurrent();

		// force login
		if (allAuthentications == null) {
			return doLogin(engineName, null, null, null);
		}

		Authentication engineAuth = allAuthentications.getAuthenticationForProcessEngine(engineName);

		if (engineAuth == null) {
			return doLogin(engineName, null, null, null);
		}
		return Response.ok(AuthenticationDto.fromAuthentication(engineAuth)).build();
	}

	/**
	 * Do login.
	 *
	 * @param engineName
	 *            the engine name
	 * @param appName
	 *            the app name
	 * @param username
	 *            the username
	 * @param password
	 *            the password
	 * @return the login dto
	 */
	@SuppressWarnings("unused")
	@POST
	@Path("/{processEngineName}/login/{appName}")
	public Response doLogin(@PathParam("processEngineName") String engineName, @PathParam("appName") String appName,
			@FormParam("username") String username, @FormParam("password") String password) {

		Authentication login = ssoAuthenticator.doLogin(engineName);
		if (login == null) {
			return Response.status(401).build();
		}
		final Authentications authentications = Authentications.getCurrent();
		authentications.addAuthentication(login);
		return Response.ok(AuthenticationDto.fromAuthentication(login)).build();

	}

	/**
	 * Gets the groups of user.
	 *
	 * @param engine
	 *            the engine
	 * @param userId
	 *            the user id
	 * @return the groups of user
	 */
	protected static List<String> getGroupsOfUser(ProcessEngine engine, String userId) {
		List<Group> groups = engine.getIdentityService().createGroupQuery().groupMember(userId).list();

		List<String> groupIds = new ArrayList<>(groups.size());
		for (Group group : groups) {
			groupIds.add(group.getId());
		}
		return groupIds;
	}

	/**
	 * Do logout.
	 *
	 * @param engineName
	 *            the engine name
	 * @return the response
	 */
	@POST
	@Path("/{processEngineName}/logout")
	public Response doLogout(@PathParam("processEngineName") String engineName) {
		final Authentications authentications = Authentications.getCurrent();
		if (authentications != null) {
			// remove authentication for process engine
			authentications.removeAuthenticationForProcessEngine(engineName);
			Authentications.clearCurrent();
		}
		return Response.status(401).build();

	}

	/**
	 * Checks if is authorized for app.
	 *
	 * @param authorizationService
	 *            the authorization service
	 * @param username
	 *            the username
	 * @param groupIds
	 *            the group ids
	 * @param application
	 *            the application
	 * @return true, if is authorized for app
	 */
	protected static boolean isAuthorizedForApp(AuthorizationService authorizationService, String username,
			List<String> groupIds, String application) {
		return authorizationService.isUserAuthorized(username, groupIds, ACCESS, APPLICATION, application);
	}

}
