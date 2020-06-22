package com.sirmaenterprise.sep.activities;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.audit.processor.RecentActivity;
import com.sirma.itt.emf.audit.processor.StoredAuditActivitiesWrapper;
import com.sirma.itt.emf.audit.solr.service.RecentActivitiesRetriever;
import com.sirma.itt.emf.audit.solr.service.RecentActivitiesSentenceGenerator;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchArguments.QueryResultPermissionFilter;
import com.sirma.itt.seip.rest.annotations.search.Search;
import com.sirma.itt.seip.rest.models.SearchResponseWrapper;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;
import com.sirma.itt.seip.rest.utils.request.params.RequestParams;
import com.sirma.itt.seip.search.SearchService;

/**
 * Provides access to instance audit activity records.
 *
 * @author yasko
 * @author A. Kunchev
 */
@Transactional
@Path("/instances")
@ApplicationScoped
@Produces(Versions.V2_JSON)
public class InstanceHistoryRestService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private RecentActivitiesRetriever retriever;

	@Inject
	private RecentActivitiesSentenceGenerator generator;

	@Inject
	private SearchService searchService;

	/**
	 * Retrieves the audit activities for an instance. Results can be paginated using the limit and offset query params.
	 * For unlimited results use limit=all.
	 *
	 * @param info
	 *            {@link RequestInfo} bean param used for limit and offset param retrieval
	 * @return {@link SearchResponseWrapper} containing the matched results and pagination info=
	 */
	@GET
	@Search
	@Path("/{id}/history")
	public SearchResponseWrapper<RecentActivity> loadHistory(@BeanParam RequestInfo info) {
		return loadActivities(RecentActivitiesRequest.buildRequestFromInfo(info, RequestParams.PATH_ID));
	}

	/**
	 * Search for instances by provided search arguments.
	 *
	 * @param searchArgs
	 *            {@link SearchArguments Search arguments}
	 * @param request
	 *            {@link RecentActivitiesRequest} contains the request information like limit, offset, etc.
	 * @return {@link SearchArguments Found results} with additional information e.g total number of results
	 */
	@POST
	@Search
	@Path("/history")
	@Consumes(Versions.V2_JSON)
	public SearchResponseWrapper<RecentActivity> loadHistoryForSearchedInstances(SearchArguments<Instance> searchArgs,
			RecentActivitiesRequest request) {
		List<Serializable> foundIds = performSearch(searchArgs);
		request.getIds().addAll(foundIds);
		return loadActivities(request);
	}

	private List<Serializable> performSearch(SearchArguments<Instance> searchArgs) {
		try {
			searchArgs.setMaxSize(-1);
			searchArgs.setPageNumber(1);
			searchArgs.setPermissionsType(QueryResultPermissionFilter.READ);
			searchService.search(Instance.class, searchArgs);

			return searchArgs
					.getResult()
						.stream()
						.map(Instance::getId)
						.collect(Collectors.toCollection(LinkedList::new));
		} catch (Exception e) {
			LOGGER.error("Error during search request!", e);
		}

		return Collections.emptyList();
	}

	/**
	 * Returns activities for the given list of instance ids.
	 *
	 * @param request
	 *            {@link RecentActivitiesRequest} contains the request information like limit, offset, etc.
	 * @return {@link SearchArguments Found results} with additional information e.g total number of results
	 */
	@POST
	@Search
	@Path("/history/batch")
	@Consumes(Versions.V2_JSON)
	public SearchResponseWrapper<RecentActivity> batchLoadHistory(
			RecentActivitiesRequest request) {
		return loadActivities(request);
	}

	private SearchResponseWrapper<RecentActivity> loadActivities(RecentActivitiesRequest request) {
		int limit = request.getLimit();
		int offset = request.getOffset();
		StoredAuditActivitiesWrapper activities;
		if (limit == -1 && offset == 0) {
			// When we want to retrieve all activities.
			activities = retriever.getActivities(request.getIds(), request.getDateRange());
		} else {
			activities = retriever.getActivities(request.getIds(), offset, limit, request.getDateRange());
		}

		List<RecentActivity> generated = generator.generateSentences(activities.getActivities());
		return new SearchResponseWrapper<RecentActivity>()
				.setResults(generated)
					.setLimit(limit)
					.setOffset(offset)
					.setTotal(activities.getTotal());
	}

}
