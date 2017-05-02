package com.sirma.itt.seip.resources.rest;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.resources.security.ChangePasswordException;
import com.sirma.itt.seip.resources.security.UserCredentialService;

/**
 * The UserCredentialRestService is responsible to handle the authentication/authorization request for a user. Requests
 * are processed by the underlying systems.
 */
@ApplicationScoped
@Path("/security/user")
@Produces(MediaType.APPLICATION_JSON)
public class UserCredentialRestService {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserCredentialRestService.class);

	/** The credential service. */
	@Inject
	private Instance<UserCredentialService> credentialService;

	/**
	 * Change password request. All the arguments should be valid and provided to process the request.
	 *
	 * @param username
	 *            the username to change for
	 * @param oldPassword
	 *            the old password to validate
	 * @param newPassword
	 *            the new password to set
	 * @return the username keyed to the result of true or false depending on the result
	 */
	@POST
	@Path("/changepassword")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public String changePassword(@FormParam("username") final String username,
			@FormParam("oldpass") final String oldPassword, @FormParam("newpass") final String newPassword) {
		if (credentialService.isUnsatisfied()) {
			LOGGER.warn("No credential service is installed! Cannot change password!");
			return "";
		}

		// REVIEW: are passwords passed plain text, what about encryption?
		boolean changedUserPassword = false;
		JSONObject result = new JSONObject();

		boolean oldPass = StringUtils.isNotBlank(oldPassword);
		boolean newPass = StringUtils.isNotBlank(newPassword);
		boolean uname = StringUtils.isNotBlank(username);
		if (oldPass && newPass && uname) {
			try {
				changedUserPassword = credentialService.get().changeUserPassword(username, oldPassword, newPassword);
			} catch (Exception e) {
				String localizedMessage = e.getLocalizedMessage();
				LOGGER.warn(localizedMessage, e);
				throw new ChangePasswordException(localizedMessage, new HashMap<>(0));
			}
		} else {
			Map<String, String> messages = new HashMap<>(4);
			if (!oldPass) {
				messages.put("oldPass", "Old password is required");
			}
			if (!newPass) {
				messages.put("newPass", "New password is required");
			}
			if (!uname) {
				messages.put("uname", "Username is required");
			}
			throw new ChangePasswordException("Missing required fields", messages);
		}
		JsonUtil.addToJson(result, "success", Boolean.valueOf(changedUserPassword));
		return result.toString();
	}
}
