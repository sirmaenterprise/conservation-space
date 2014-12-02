package com.sirma.cmf.web.rest;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.resources.PeopleService;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.util.JsonUtil;

/**
 * Rest service for working with and searching for users.
 * 
 * @author yasko
 */
@Path("/users")
@ApplicationScoped
public class UsersRestService {

	@Inject
	private ResourceService resourceService;

	@Inject
	private PeopleService peopleService;

	/**
	 * Searches for a user with the specified username.
	 * 
	 * @param username
	 *            Query param specifying the username of the user we are searching for.
	 * @return The full user object as JSON.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Resource getUserByUsername(@QueryParam("username") String username) {
		User resource = resourceService.getResource(username, ResourceType.USER);
		return resource;
	}

	/**
	 * Searches for users containing the provided search term in their names or usernames.
	 * 
	 * @param searchTerm
	 *            Search term.
	 * @return List of users.
	 */
	@GET
	@Path("/search")
	public String searchUsers(@QueryParam("term") String searchTerm) {
		List<User> users;
		if (StringUtils.isNullOrEmpty(searchTerm) || "*".equals(searchTerm)) {
			users = resourceService.getAllResources(ResourceType.USER, null);
		} else {
			users = peopleService.getFilteredUsers("*" + searchTerm + "*");
		}
		JSONArray result = new JSONArray();

		for (User user : users) {
			JSONObject jsonObject = new JSONObject();
			JsonUtil.addToJson(jsonObject, "value", user.getId());
			JsonUtil.addToJson(jsonObject, "label", user.getDisplayName());
			result.put(jsonObject);
		}
		return result.toString();
	}
}
