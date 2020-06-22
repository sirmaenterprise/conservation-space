package com.sirmaenterprise.sep.annotations;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.annotations.AnnotationSearchRequest;
import com.sirma.itt.seip.annotations.AnnotationService;
import com.sirma.itt.seip.annotations.model.Annotation;
import com.sirma.itt.seip.annotations.model.AnnotationProperties;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.time.DateRange;
import com.sirma.itt.seip.time.TimeTracker;

/**
 * Provides UI/client specific API for for accessing {@link Annotation} data
 *
 * @author Vilizar Tsonev
 */
@Transactional
@ApplicationScoped
@Path("/discussions")
@Produces(Versions.V2_JSON)
public class DiscussionsRest {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private AnnotationService annotationService;

	@Inject
	private InstanceService instanceService;

	@Inject
	private InstanceLoadDecorator instanceLoadDecorator;

	@Inject
	private SearchService searchService;

	/**
	 * Searches for annotations according to the passed {@link AnnotationsSearchCriteria}.
	 *
	 * @param searchCriteria
	 *            the search criteria for the annotations search
	 * @return the annotations
	 */
	@POST
	@Consumes(Versions.V2_JSON)
	public DiscussionsResponse searchAnnotations(AnnotationsSearchCriteria searchCriteria) {
		TimeTracker tracker = TimeTracker.createAndStart();
		List<String> instanceIds = searchAnnotations(searchCriteria, tracker);

		if (isEmpty(instanceIds)) {
			return DiscussionsResponse.EMPTY_RESPONSE;
		}

		DateRange dateRange = getDateRangeFromCriteria(searchCriteria);
		AnnotationSearchRequest searchRequest = new AnnotationSearchRequest()
				.setDateRange(dateRange)
					.setInstanceIds(instanceIds)
					.setUserIds(searchCriteria.getUserIds())
					.setLimit(searchCriteria.getLimit())
					.setOffset(searchCriteria.getOffset())
					.setStatus(searchCriteria.getStatus())
					.setText(searchCriteria.getText());

		Collection<Annotation> annotations = annotationService.searchAnnotations(searchRequest);
		LOGGER.trace("Annotations loading took {} ms", tracker.elapsedTime());

		List<String> targetInstances = getTargetInstancesFromResult(annotations);
		Map<String, String> instanceHeaders = getCompactInstanceHeaders(targetInstances);

		DiscussionsResponse response = new DiscussionsResponse();
		response.setAnnotations((List<Annotation>) annotations);
		response.setTargetInstanceHeaders(instanceHeaders);

		LOGGER.debug("Discussion loading took {} ms", tracker.stop());
		return response;
	}

	/**
	 * Searches for annotations according to the passed {@link AnnotationsSearchCriteria}.
	 *
	 * @param searchCriteria
	 *            the search criteria for the annotations search
	 * @return the annotations
	 */
	@POST
	@Path("/count")
	@Consumes(Versions.V2_JSON)
	public Response countAnnotations(AnnotationsSearchCriteria searchCriteria) {

		TimeTracker tracker = TimeTracker.createAndStart();

		List<String> instanceIds = searchAnnotations(searchCriteria, tracker);

		if (isEmpty(instanceIds)) {
			return buildCountResponse(0);
		}

		tracker.begin();
		DateRange dateRange = getDateRangeFromCriteria(searchCriteria);
		AnnotationSearchRequest searchRequest = new AnnotationSearchRequest()
				.setDateRange(dateRange)
					.setInstanceIds(instanceIds)
					.setUserIds(searchCriteria.getUserIds())
					.setLimit(searchCriteria.getLimit())
					.setOffset(searchCriteria.getOffset())
					.setStatus(searchCriteria.getStatus())
					.setText(searchCriteria.getText());

		try {
			int annotationsCount = annotationService.searchAnnotationsCountOnly(searchRequest);
			return buildCountResponse(annotationsCount);
		} finally {
			LOGGER.debug("Discussion counting took {} ms", tracker.stop());
		}
	}

	private static Response buildCountResponse(int annotationsCount) {
		return Response.ok(Json.createObjectBuilder().add("count", annotationsCount).build().toString()).build();
	}

	private List<String> searchAnnotations(AnnotationsSearchCriteria searchCriteria, TimeTracker tracker) {
		List<String> instanceIds;
		List<String> existingManuallySelectedIds = searchCriteria.getManuallySelectedObjects();
		if (!searchCriteria.isHistoricalVersion()) {
			existingManuallySelectedIds = existingManuallySelectedIds
					.stream()
					.filter(id -> instanceService.loadByDbId(id) != null)
					.collect(Collectors.toList());
		}
		if (searchCriteria.getSearchTree() != null) {
			tracker.begin();
			List<String> instanceIdsFoundFromSearch = doSearch(searchCriteria.getSearchTree());
			instanceIds = CollectionUtils.merge(existingManuallySelectedIds, instanceIdsFoundFromSearch);
			LOGGER.trace("Reference instances loading took {} ms", tracker.stop());
		} else {
			instanceIds = existingManuallySelectedIds;
		}
		return instanceIds;
	}

	/**
	 * Executes the search according to the passed search tree and returns a list of the Ids of the found instances.
	 *
	 * @param tree
	 *            is the search tree
	 * @return the list of the Ids of the found instances.
	 */
	private List<String> doSearch(Condition tree) {
		SearchRequest searchRequest = new SearchRequest(CollectionUtils.createHashMap(2));
		searchRequest.setSearchTree(tree);
		SearchArguments<Instance> searchArgs = searchService.parseRequest(searchRequest);
		searchArgs.setPageSize(searchArgs.getMaxSize());
		searchArgs.setPageNumber(1);
		searchService.search(Instance.class, searchArgs);
		return searchArgs
				.getResult()
					.stream()
					.map(Instance::getId)
					.map(Serializable::toString)
					.collect(Collectors.toList());
	}

	/**
	 * Extracts all unique target instances from the collection of {@link Annotation} returned from the search.
	 *
	 * @param annotations
	 *            the page of annotations returned from the search
	 * @return all unique target instances
	 */
	private static List<String> getTargetInstancesFromResult(Collection<Annotation> annotations) {
		return annotations
				.stream()
					.map(annotation -> annotation.getString(AnnotationProperties.HAS_TARGET))
					.distinct()
					.collect(Collectors.toList());
	}

	private static DateRange getDateRangeFromCriteria(AnnotationsSearchCriteria searchCriteria) {
		DateRange dateRangeCreated = new DateRange(null, null);
		if (searchCriteria.getCreatedFrom() != null) {
			dateRangeCreated.setFirst(searchCriteria.getCreatedFrom());
		}
		if (searchCriteria.getCreatedTo() != null) {
			dateRangeCreated.setSecond(searchCriteria.getCreatedTo());
		}
		return dateRangeCreated;
	}

	/**
	 * Gets the mapping between the IDs of the provided instances and their compact headers.
	 *
	 * @param instanceIds
	 *            the list with the instance ids
	 * @return the map with the instance ids and headers
	 */
	private Map<String, String> getCompactInstanceHeaders(List<String> instanceIds) {
		List<Instance> instances = instanceService.loadByDbId(instanceIds);
		instanceLoadDecorator.decorateResult(instances);

		Map<String, String> idsToHeaders = CollectionUtils.createHashMap(instanceIds.size());
		instances.forEach(instance -> idsToHeaders.put((String) instance.getId(),
				instance.getString(DefaultProperties.HEADER_COMPACT)));
		return idsToHeaders;
	}
}
