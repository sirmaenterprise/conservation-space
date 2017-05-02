package com.sirma.cmf.web.rest;

import java.lang.invoke.MethodHandles;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.emf.web.rest.util.SearchResultTransformer;
import com.sirma.itt.seip.Uri;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.rest.RestServiceException;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.facet.Facet;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.search.SearchConfiguration;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.search.facet.FacetService;
import com.sirma.itt.seip.time.TimeTracker;

/**
 * REST service for performing basic search operations.
 *
 * @author yasko
 */
@Path("/search")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class SearchRestService extends EmfRestService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String TOO_MUCH_RESULTS_LABEL_ID = "search.facet.toomuchresults";

	private static final String SPECIFY_MORE_CRITERIA_LABEL_ID = "search.facet.specifymorecriteria";

	private static final String NO_RESULTS_LABEL_ID = "search.facet.noresults";

	@Inject
	private SearchService searchService;

	@Inject
	private SearchResultTransformer searchResultTransformer;

	@Inject
	private FacetService facetService;

	@Inject
	private SearchConfiguration searchConfiguration;

	/**
	 * Performs basic object search.
	 *
	 * @param uriInfo
	 *            Contains search params.
	 * @return JSON object as string containing the results.
	 */
	@GET
	@Path("/basic")
	public Response performBasicSearch(@Context UriInfo uriInfo) {
		TimeTracker tracker = TimeTracker.createAndStart();

		try {
			SearchArguments<Instance> searchArgs = performSearchInternal(uriInfo, false);
			JSONObject result = new JSONObject();
			int size = 0;
			List<Instance> instances = searchArgs.getResult();
			if (CollectionUtils.isNotEmpty(instances)) {
				// XXX: Implement search by fields
				List<String> selectedFields = uriInfo.getQueryParameters().get("fields[]");

				size = instances.size();

				searchResultTransformer.transformResult(searchArgs.getTotalItems(), instances, selectedFields, result,
						searchArgs.getFacets());
			}

			LOGGER.debug("SearchRestService.performBasicSearch response results : {} took {} ms", size,
					tracker.stopInSeconds());
			return buildOkResponse(result.toString());
		} catch (RestServiceException e) {
			LOGGER.debug(e.getMessage(), e);
			return buildResponse(e.getStatus(), e.getMessage());
		}
	}

	/**
	 * Get the available facets for the given search criterias. Returns only the facets and their configurations but no
	 * actual values.
	 *
	 * @param uriInfo
	 *            the provided parameters
	 * @return JSON object containing the facets
	 */
	@GET
	@Path("/facets")
	public Response getAvailableFacets(@Context UriInfo uriInfo) {
		try {
			addUserURI(uriInfo);
		} catch (RestServiceException e) {
			LOGGER.debug(e.getMessage(), e);
			return buildResponse(e.getStatus(), e.getMessage());
		}
		SearchArguments<Instance> searchArgs = facetService
				.getAvailableFacets(new SearchRequest(uriInfo.getQueryParameters()));
		if (searchArgs.getTotalItems() >= searchConfiguration.getSearchResultMaxSize().intValue()) {
			return buildResponse(Status.FORBIDDEN,
					MessageFormat.format(
							labelProvider.getValue(TOO_MUCH_RESULTS_LABEL_ID)
									+ labelProvider.getValue(SPECIFY_MORE_CRITERIA_LABEL_ID),
							searchConfiguration.getSearchResultMaxSize()));
		} else if (searchArgs.getTotalItems() == 0) {
			return buildResponse(Status.FORBIDDEN, labelProvider.getValue(NO_RESULTS_LABEL_ID));
		}
		return buildOkResponse(searchResultTransformer.getFacets(searchArgs.getFacets().values()).toString());

	}

	/**
	 * Based on the provided parameters, performs a search that will calculate facets.
	 *
	 * @param uriInfo
	 *            - the provided parameters
	 * @return JSON object as string containing the facets
	 */
	@GET
	@Path("/faceted")
	public Response performFacetedSearch(@Context UriInfo uriInfo) {
		try {
			SearchArguments<Instance> searchArgs = performSearchInternal(uriInfo, true);
			return buildOkResponse(getFacetsJson(searchArgs).toString());
		} catch (RestServiceException e) {
			LOGGER.debug(e.getMessage(), e);
			return buildResponse(e.getStatus(), e.getMessage());
		}
	}

	/**
	 * Create a {@link JSONArray} from the facets list using the {@link SearchResultTransformer}.
	 *
	 * @param searchArgs
	 *            the search arguments
	 * @return a json array containing the facet
	 */
	private JSONObject getFacetsJson(SearchArguments<Instance> searchArgs) {
		JSONObject result = new JSONObject();
		Map<String, Facet> facets = searchArgs.getFacets();
		JSONArray transformedFacets = searchResultTransformer.transformFacets(facets);

		JsonUtil.addToJson(result, "facets", transformedFacets);
		JsonUtil.addToJson(result, "resultSize", searchArgs.getTotalItems());
		return result;
	}

	/**
	 * Perform search according to the query params.
	 *
	 * @param uriInfo
	 *            - params info
	 * @param isFaceted
	 *            indicates whether a faceted search should be performed
	 * @return the arguments after the search (search arguments)
	 */
	private SearchArguments<Instance> performSearchInternal(UriInfo uriInfo, boolean isFaceted) {
		addUserURI(uriInfo);
		SearchArguments<Instance> searchArgs = searchService
				.parseRequest(new SearchRequest(uriInfo.getQueryParameters()));
		if (searchArgs == null) {
			LOG.warn("Invalid or not supported search request!");
			throw new RestServiceException("Invalid or not supported search request!", Status.BAD_REQUEST);
		}
		searchArgs.requestAllFoundInstanceIds(isFaceted);
		searchArgs.setFaceted(isFaceted);
		searchService.searchAndLoad(Instance.class, searchArgs);
		return searchArgs;
	}

	/**
	 * Add the curent user's uri to the query parameters so permissions can be applied. If the user is not found, throw
	 * a {@link RestServiceException} .
	 *
	 * @param uriInfo
	 *            the provided parameters
	 */
	/**
	 * Add the curent user's uri to the query parameters so permissions can be applied. If the user is not found, throw
	 * a {@link RestServiceException} .
	 *
	 * @param uriInfo
	 *            the provided parameters
	 */
	private void addUserURI(UriInfo uriInfo) {
		User currentUser = (User) getCurrentUser();
		if (currentUser == null) {
			throw new RestServiceException("User is not logged in!", Status.UNAUTHORIZED);
		}

		Uri userURI = typeConverter.convert(Uri.class, currentUser.getId());
		uriInfo.getQueryParameters().add("userURI", userURI.toString());
	}

}