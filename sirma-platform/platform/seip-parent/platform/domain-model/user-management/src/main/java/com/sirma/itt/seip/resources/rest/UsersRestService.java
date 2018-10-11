package com.sirma.itt.seip.resources.rest;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.LANGUAGE;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.Query;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchArguments.QueryResultPermissionFilter;
import com.sirma.itt.seip.domain.search.SearchDialects;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.rest.RestUtil;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Rest service for working with users and groups.
 *
 * @author yasko
 */
@Transactional
@Path("/users")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class UsersRestService {

	private static final Logger LOGGER = LoggerFactory.getLogger(UsersRestService.class);

	@Inject
	private SearchService searchService;

	@Inject
	private ResourceService resourceService;

	@Inject
	private AuthorityService authorityService;

	@Inject
	private SecurityContext securityContext;

	private static final String STAR = "*";

	/**
	 * Retrieves a user or a group by username/groupname or id.
	 *
	 * @param id
	 *            Username/Groupname to search by.
	 * @return the found user or group, null otherwise.
	 */
	@GET
	@Path("/{id}")
	public String find(@PathParam("id") String id) {
		Resource resource = resourceService.getResource(id);
		if (resource == null) {
			resource = resourceService.getResource(id, ResourceType.UNKNOWN);
		}

		if (resource == null) {
			return null;
		}
		return toJsonObject(resource, true).toString();
	}

	/**
	 * Search for users and groups.
	 *
	 * @param searchTerm
	 *            Search term to look for in user and group names.
	 * @param includeGroups
	 *            Whether to include groups in search ({@code false} by default)
	 * @param includeUsers
	 *            Whether to include users in search ({@code true} by default)
	 * @param limit
	 *            Limit the number of returned results. 20 by default.
	 * @param offset
	 *            page number
	 * @return A json array containing the matched users and groups.
	 */
	@GET
	public String search(@QueryParam("term") @DefaultValue("*") final String searchTerm,
			@QueryParam("includeGroups") @DefaultValue("false") final boolean includeGroups,
			@QueryParam("includeUsers") @DefaultValue("true") final boolean includeUsers,
			@QueryParam("limit") @DefaultValue("25") final int limit,
			@QueryParam("offset") @DefaultValue("1") final int offset) {

		SearchArguments<Instance> args = new SearchArguments<>();
		args.setDialect(SearchDialects.SOLR);
		args.setQueryTimeout(TimeUnit.MILLISECONDS, 500);
		args.setMaxSize(limit);
		args.setPageSize(limit);
		args.setPageNumber(offset);

		List<String> types = new ArrayList<>();
		if (includeUsers) {
			types.add(EMF.USER.toString());
		}

		if (includeGroups) {
			types.add(EMF.GROUP.toString());
		}

		Query query = new Query("rdfType", (Serializable) types);
		if (StringUtils.isNotBlank(searchTerm) && !UsersRestService.STAR.equals(searchTerm)) {
			String searchable = UsersRestService.STAR + searchTerm + UsersRestService.STAR;
			query = query.and(new Query(DefaultProperties.URI, searchable)
					.or(DefaultProperties.DESCRIPTION, searchable)
					.or(DefaultProperties.TITLE, searchable));
		}

		// users are not under permission control
		args.setPermissionsType(QueryResultPermissionFilter.NONE);

		args.setQuery(query);
		searchService.search(Instance.class, args);

		List<Instance> searchResult = args.getResult();
		List<Serializable> resourceIds = searchResult.stream().map(Instance::getId).collect(Collectors.toList());
		int total = args.getTotalItems();

		JSONObject result = new JSONObject();
		JSONArray items = new JSONArray();

		List<Instance> resources = resourceService.loadByDbId(resourceIds);

		for (Instance item : resources) {
			items.put(toJsonObject((Resource) item, false));
		}

		JsonUtil.addToJson(result, "total", total);
		JsonUtil.addToJson(result, "items", items);
		return result.toString();
	}

	/**
	 * Retrieves resources (users and/or groups) by their ids or usernames.
	 *
	 * @param data
	 *            JSON array with user and/or group ids or usernames.
	 * @return found resources or empty array
	 */
	@POST
	@Path("/multi")
	public Response getResources(String data) {
		if (StringUtils.isEmpty(data)) {
			return RestUtil.buildResponse(Status.OK, "[]");
		}
		JSONArray items = new JSONArray();
		JSONArray resourceIds = JsonUtil.createArrayFromString(data);
		if (resourceIds == null) {
			return RestUtil.buildResponse(Status.BAD_REQUEST, "Invalid input parameters.");
		}
		for (int i = 0; i < resourceIds.length(); i++) {
			String id = JsonUtil.getStringFromArray(resourceIds, i);
			if (id == null) {
				LOGGER.warn("Expected resource identifier but found none!");
				continue;
			}
			Resource resource = resourceService.findResource(id);
			if (resource != null) {
				items.put(toJsonObject(resource, false));
			}
		}
		return RestUtil.buildResponse(Status.OK, items.toString());
	}

	/**
	 * Converts a resource to a json object.
	 *
	 * @param resource
	 *            Resource to convert
	 * @return Json object
	 */
	private JSONObject toJsonObject(Resource resource, boolean calculateRole) {
		JSONObject jsonObject = new JSONObject();
		JsonUtil.addToJson(jsonObject, "id", resource.getId());
		JsonUtil.addToJson(jsonObject, "value", resource.getName());
		JsonUtil.addToJson(jsonObject, "label", resource.getDisplayName());
		JsonUtil.addToJson(jsonObject, "type", resource.getType().getName());
		JsonUtil.addToJson(jsonObject, "language", resource.getString(LANGUAGE));
		JsonUtil.addToJson(jsonObject, "emailAddress", resource.getProperties().get("emailAddress"));
		if (calculateRole) {
			JsonUtil.addToJson(jsonObject, "tenantId", securityContext.getCurrentTenantId());
			JsonUtil.addToJson(jsonObject, "isAdmin", authorityService.isAdminOrSystemUser(resource));
		}
		return jsonObject;
	}

}
