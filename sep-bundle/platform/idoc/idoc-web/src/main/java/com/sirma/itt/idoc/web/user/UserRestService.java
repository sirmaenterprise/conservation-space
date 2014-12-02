package com.sirma.itt.idoc.web.user;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.security.model.User;

/**
 * Rest service for operating with users.
 * <p>
 * REVIEW: remove this class and use the ResourceRestService
 * 
 * @author SKostadinov
 */
@Path("/user")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class UserRestService {

	@Inject
	private ResourceService resourceService;

	/**
	 * Loads a list of users for a given object.
	 * 
	 * @return fetched users.
	 * @throws JSONException
	 *             when JSON conversion fails.
	 */
	@GET
	@Path("/")
	public String load() throws JSONException {
		List<User> users = resourceService.getAllResources(ResourceType.USER, null);
		JSONArray result = new JSONArray();
		for (User user : users) {
			result.put(convert(user));
		}
		return result.toString();
	}

	/**
	 * Converts a User into JSON object.
	 * 
	 * @param user
	 *            user to convert
	 * @return produces JSON object
	 */
	private JSONObject convert(User user) {
		try {
			JSONObject object = new JSONObject();
			object.put("id", user.getId());
			object.put("name", user.getName());
			object.put("displayName", user.getDisplayName());
			return object;
		} catch (JSONException e) {
			throw new IllegalStateException(e);
		}
	}
}