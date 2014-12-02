package com.sirma.itt.emf.web.rest;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.exceptions.ChangePasswordException;
import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.emf.security.UserCredentialService;
import com.sirma.itt.emf.security.event.UserPasswordChangeEvent;
import com.sirma.itt.emf.util.JsonUtil;

/**
 * The UserCredentialRestService is responsible to handle the authentication/authorization request
 * for a user. Requests are processed by the underlying systems.
 */
@ApplicationScoped
@Path("/security/user")
@Produces(MediaType.APPLICATION_JSON)
public class UserCredentialRestService extends EmfRestService {

	/** The credential service. */
	@Inject
	private UserCredentialService credentialService;

	@Inject
	private EventService eventService;

	/**
	 * Change password request. All the arguments should be valid and provided to process the
	 * request.
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
	public String changePassword(final @FormParam("username") String username,
			final @FormParam("oldpass") String oldPassword,
			final @FormParam("newpass") String newPassword) {
		// REVIEW: are passwords passed plain text, what about encryption?
		boolean changedUserPassword = false;
		JSONObject result = new JSONObject();

		boolean oldPass = StringUtils.isNotBlank(oldPassword);
		boolean newPass = StringUtils.isNotBlank(newPassword);
		boolean uname = StringUtils.isNotBlank(username);
		if (oldPass && newPass && uname) {
			try {
				changedUserPassword = credentialService.changeUserPassword(username, oldPassword,
						newPassword);
				eventService.fire(new UserPasswordChangeEvent());
			} catch (Exception e) {
				String localizedMessage = e.getLocalizedMessage();
				log.warn(localizedMessage);
				throw new ChangePasswordException(localizedMessage, new HashMap<String, String>());
			}
		} else {
			Map<String, String> messages = new HashMap<>();
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
