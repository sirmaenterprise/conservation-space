package com.sirma.cmf.web.rest;

import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sirma.itt.cmf.search.SearchPreprocessorEngine;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.instance.dao.BatchEntityLoader;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.ExtensionPoint;
import com.sirma.itt.emf.resources.PeopleService;
import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.emf.search.SearchService;
import com.sirma.itt.emf.search.event.SearchExecutedEvent;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.security.AuthorityService;
import com.sirma.itt.emf.security.SecurityModel;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.emf.util.JsonUtil;
import com.sirma.itt.emf.web.rest.util.SearchResultTransformer;

/**
 * REST service for performing basic search operations.
 * 
 * @author yasko
 */
@Path("/search")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class SearchRestService extends EmfRestService {

	/** The people service. */
	@Inject
	private PeopleService peopleService;

	/** The search service. */
	@Inject
	private SearchService searchService;

	@Inject
	private SearchResultTransformer searchResultTransformer;

	@Inject
	private AuthorityService authorityService;

	@Inject
	private EventService eventService;

	@Inject
	@ExtensionPoint(value = SearchPreprocessorEngine.NAME)
	private Iterable<SearchPreprocessorEngine> facades;

	/**
	 * Searches for users containing the provided search term in their names or usernames.
	 * 
	 * @deprecated Use UsersRestService
	 * @param searchTerm
	 *            Search term.
	 * @return List of users.
	 */
	@Deprecated
	@GET
	@Path("/users")
	public String searchUsers(@QueryParam("term") String searchTerm) {
		TimeTracker tracker = null;
		if (debug) {
			tracker = new TimeTracker().begin();
			log.debug("SearchRestService.searchForUsers term [" + searchTerm + "]");
		}
		List<User> users = peopleService.getFilteredUsers("*" + searchTerm + "*");
		JSONArray result = new JSONArray();

		for (User user : users) {
			JSONObject jsonObject = new JSONObject();
			JsonUtil.addToJson(jsonObject, "value", user.getId());
			JsonUtil.addToJson(jsonObject, "label", user.getDisplayName());
			result.put(jsonObject);
		}

		if (debug) {
			log.debug("SearchRestService.searchForUsers response: " + result.length() + " in "
					+ tracker.stopInSeconds() + " s");
			if (trace) {
				log.trace("SearchRestService.searchForUsers response:" + result);
			}
		}
		return result.toString();
	}

	/**
	 * Performs basic object search.
	 * 
	 * @param uriInfo
	 *            Contains search params.
	 * @param toLog
	 *            indicates whether the search should be logged in the audit log
	 * @return JSON object as string containing the results.
	 */
	@GET
	@Path("/basic")
	public String performBasicSearch(@Context UriInfo uriInfo,
			@DefaultValue("false") @QueryParam("log") boolean toLog) {
		TimeTracker tracker = null;
		if (debug) {
			tracker = new TimeTracker().begin();
		}
		JSONObject result = new JSONObject();
		MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
		SearchArguments<Instance> searchArgs = new SearchArguments<>();
		try {
			for (SearchPreprocessorEngine nextFacade : facades) {
				if (nextFacade.isApplicable(queryParams)) {
					nextFacade.prepareBasicQuery(queryParams, searchArgs);
					break;
				}
			}
			searchService.search(Instance.class, searchArgs);


			User currentUser = authenticationService.get().getCurrentUser();
			if ((searchArgs.getResult() != null) && (currentUser != null)) {
				// TODO Implement search by fields
				List<String> selectedFields = queryParams.get("fields[]");
				List<Instance> instances = searchArgs.getResult();
				// if we have have requested specific fields we are fetching the instances and
				// getting the fields from them. this should be realized via combined semantic and
				// solr query
				if (selectedFields != null && !selectedFields.isEmpty()) {
					instances = BatchEntityLoader.loadInstances(searchArgs.getResult(),
							serviceRegister, taskExecutor);
				}
				// TODO permission query
				Map<Instance, Boolean> resultsByPermission = authorityService.hasPermission(
						SecurityModel.PERMISSION_READ, instances, currentUser);

				searchResultTransformer.transformResult(searchArgs.getTotalItems(),
						resultsByPermission, selectedFields, result);
			}

			if (toLog) {
				String searchType = queryParams.getFirst("searchType") + "Search";
				eventService.fire(new SearchExecutedEvent(searchType));
			}
			if (debug) {
				log.debug("SearchRestService.performBasicSearch response results : "
						+ searchArgs.getResult().size() + " took " + tracker.stopInSeconds() + " s");
				if (trace) {
					log.trace("SearchRestService.performBasicSearch response:" + result);
				}
			}
		} catch (JSONException e) {
			log.error("Error during request parsing!", e);
		} catch (Exception e) {
			log.error("Error during search request!", e);
		}

		return result.toString();
	}

}
