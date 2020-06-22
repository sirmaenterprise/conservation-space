package com.sirma.sep.keycloak.util;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import javax.json.JsonObject;
import javax.ws.rs.core.Response;

import org.keycloak.admin.client.resource.GroupsResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.security.util.SecurityUtil;
import com.sirma.sep.keycloak.exception.KeycloakClientException;

/**
 * Contains utility methods for working with Keycloak IdP REST API.
 *
 * @author smustafov
 */
public class KeycloakApiUtil {

	public static final String ERROR_MESSAGE_KEY = "errorMessage";
	public static final String ERROR_DESCRIPTION_KEY = "error_description";

	private KeycloakApiUtil() {
		// utility class
	}

	/**
	 * Gets the created Keycloak object id by extracting it from the given {@link Response}'s location path.
	 *
	 * @param response the api {@link Response} from which to extract id
	 * @return id of the created object in Keycloak
	 */
	public static String getCreatedId(Response response) {
		if (!response.getStatusInfo().equals(Response.Status.CREATED)) {
			Response.StatusType statusInfo = response.getStatusInfo();
			throw new EmfRuntimeException(
					"Create method returned status " + statusInfo.getReasonPhrase() + " (Code: " + statusInfo
							.getStatusCode() + "); expected status: Created (201)");
		}

		URI location = response.getLocation();
		if (location == null) {
			return null;
		}
		String path = location.getPath();
		return path.substring(path.lastIndexOf('/') + 1);
	}

	/**
	 * Retrieves keycloak user id by username. This can be useful when its needed to modify or fetch user's data.
	 *
	 * @param usersResource the users resource
	 * @param username      of the user for which to find keycloak user id. Usernames are checked case insensitively.
	 * @return keycloak id of the user
	 * @throws KeycloakClientException if user cannot be found
	 */
	public static String retrieveUserId(UsersResource usersResource, String username) {
		return getRemoteUser(usersResource, SecurityUtil.getUserWithoutTenant(username))
				.orElseThrow(() -> new KeycloakClientException("User not found: " + username))
				.getId();
	}

	/**
	 * Retrieves keycloak user representation by username.
	 *
	 * @param usersResource the users resource
	 * @param username      of the user for which to find keycloak user id. Usernames are checked case insensitively
	 * @return UserRepresentation of the user in Keycloak
	 */
	public static Optional<UserRepresentation> getRemoteUser(UsersResource usersResource, String username) {
		String userWithoutTenant = SecurityUtil.getUserWithoutTenant(username);

		List<UserRepresentation> foundUsers = usersResource.search(userWithoutTenant);
		for (UserRepresentation foundUser : foundUsers) {
			if (foundUser.getUsername().equalsIgnoreCase(userWithoutTenant)) {
				return Optional.of(foundUser);
			}
		}
		return Optional.empty();
	}

	/**
	 * Retrieves keycloak group id by group name.
	 *
	 * @param groupsResource the groups resource
	 * @param groupName      the name of the group
	 * @return keycloak id of the group
	 * @throws KeycloakClientException if group cannot be found
	 */
	public static String retrieveGroupId(GroupsResource groupsResource, String groupName) {
		return getRemoteGroup(groupsResource, groupName)
				.orElseThrow(() -> new KeycloakClientException("Group not found: " + groupName))
				.getId();
	}

	/**
	 * Retrieves keycloak group representation by group name.
	 *
	 * @param groupsResource the groups resource
	 * @param groupName      the name of the group
	 * @return GroupRepresentation of the group in Keycloak
	 */
	public static Optional<GroupRepresentation> getRemoteGroup(GroupsResource groupsResource, String groupName) {
		List<GroupRepresentation> foundGroups = groupsResource.groups(groupName, null, null);
		for (GroupRepresentation foundGroup : foundGroups) {
			if (foundGroup.getName().equals(groupName)) {
				return Optional.of(foundGroup);
			}
		}
		return Optional.empty();
	}

	/**
	 * Reads error message from the given response. The message is retrieved from the json response under error_description
	 * and errorMessage keys. If any of the keys are not found default value is returned.
	 *
	 * @param response the response from keycloak api
	 * @return error message contained in the given response or a default message
	 */
	public static String readErrorMessage(Response response) {
		JsonObject json = readJson(response);
		return json.getString(ERROR_DESCRIPTION_KEY, json.getString(ERROR_MESSAGE_KEY, "No response message found"));
	}

	/**
	 * Reads json from the given response and returns it as plain string.
	 *
	 * @param response the response from keycloak api
	 * @return response json as string
	 */
	public static String readJsonAsString(Response response) {
		return readJson(response).toString();
	}

	/**
	 * Reads json from the given response and returns it as {@link JsonObject}.
	 *
	 * @param response the response from keycloak api
	 * @return response json as JsonObject
	 */
	public static JsonObject readJson(Response response) {
		return JSON.readObject(response.readEntity(String.class), json -> json);
	}

}
