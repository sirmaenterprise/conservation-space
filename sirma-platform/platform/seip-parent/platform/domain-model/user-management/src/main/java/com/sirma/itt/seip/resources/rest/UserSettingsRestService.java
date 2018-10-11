package com.sirma.itt.seip.resources.rest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.sirma.itt.seip.resources.security.UserCredentialService;
import com.sirma.itt.seip.resources.security.UserPasswordChangeRequest;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * Provides managing logged user's settings like changing password and etc.
 *
 * @author smustafov
 */
@Path("/user")
@Produces(Versions.V2_JSON)
@ApplicationScoped
public class UserSettingsRestService {

	@Inject
	private UserCredentialService credentialService;

	/**
	 * Changes the currently logged in user's password.
	 * <p>
	 * Expected json:
	 *
	 * <pre>
	 * <code>
	 * {
	 *   "username": "john",
	 *   "oldPassword": "123456",
	 *   "newPassword": "password"
	 * }</code>
	 * </pre>
	 *
	 * @param passwordChangeRequest
	 *            the request for changing password
	 * @return 200 OK, if the password was successfully changed
	 */
	@POST
	@Path("change-password")
	public Response changePassword(UserPasswordChangeRequest passwordChangeRequest) {
		String username = passwordChangeRequest.getUsername();
		String oldPassword = passwordChangeRequest.getOldPassword();
		String newPassword = passwordChangeRequest.getNewPassword();

		boolean changeUserPassword = credentialService.changeUserPassword(username, oldPassword, newPassword);
		if (changeUserPassword) {
			return Response.ok().build();
		}
		return Response.notModified().build();
	}

}
