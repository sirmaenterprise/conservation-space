package com.sirma.itt.seip.search.rest;

import java.lang.invoke.MethodHandles;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.rest.BadRequestException;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.time.TimeTracker;

/**
 * REST service providing instance searching.
 *
 * @author yasko
 * @author Mihail Radkov
 */
@ApplicationScoped
@Path(SearchRest.PATH)
@Produces(Versions.V2_JSON)
public class SearchRest {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	public static final String PATH = "/search";

	@Inject
	private SearchService searchService;

	/**
	 * Performs search for instances by query parameters.
	 *
	 * @param uriInfo
	 *            - object containing the used query parameters
	 * @return {@link SearchArguments Found results} with additional information e.g total number of results.
	 */
	@GET
	@Consumes(Versions.V2_JSON)
	@Deprecated
	public SearchArguments<Instance> search(@Context UriInfo uriInfo) {
		LOGGER.debug("Received search request on uri '{}' with params {} ", uriInfo.getPath(),
				uriInfo.getQueryParameters());
		SearchRequest request = new SearchRequest(uriInfo.getQueryParameters());
		SearchArguments<Instance> searchArgs = searchService.parseRequest(request);
		if (searchArgs == null) {
			throw new BadRequestException("Search request is not supported.");
		}
		return performSearch(searchArgs);
	}

	/**
	 * Search for instances by provided search arguments.
	 *
	 * @param searchArgs
	 *            {@link SearchArguments Search arguments}.
	 * @return {@link SearchArguments Found results} with additional information e.g total number of results.
	 */
	@POST
	@Consumes(Versions.V2_JSON)
	public SearchArguments<Instance> search(SearchArguments<Instance> searchArgs) {
		return performSearch(searchArgs);
	}

	private SearchArguments<Instance> performSearch(SearchArguments<Instance> searchArgs) {
		try {
			TimeTracker tracker = TimeTracker.createAndStart();
			searchService.searchAndLoad(Instance.class, searchArgs);
			LOGGER.debug("Search results: {} took {} ms", Integer.valueOf(searchArgs.getResult().size()),
					Double.valueOf(tracker.stopInSeconds()));
		} catch (Exception e) {
			LOGGER.error("Error during search request!", e);
			searchArgs.setSearchError(e);
		}
		return searchArgs;
	}
}
