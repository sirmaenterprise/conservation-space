package com.sirmaenterprise.sep.annotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.annotations.AnnotationSearchRequest;
import com.sirma.itt.seip.annotations.AnnotationService;
import com.sirma.itt.seip.annotations.model.Annotation;
import com.sirma.itt.seip.annotations.model.AnnotationProperties;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.domain.search.tree.SearchCriteriaBuilder;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.time.DateRange;

/**
 * Tests the functionality of {@link DiscussionsRest}
 *
 * @author Vilizar Tsonev
 */
public class DiscussionsRestTest {

	@InjectMocks
	private DiscussionsRest discussionsRest;

	@Mock
	private AnnotationService annotationService;

	@Mock
	private InstanceService instanceService;

	@Mock
	private InstanceLoadDecorator instanceLoadDecorator;

	@Mock
	private SearchService searchService;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		SearchArguments<Instance> argumentsToReturn = new SearchArguments<>();
		List<Instance> instancesToReturn = mockReturnedInstances();
		argumentsToReturn.setResult(instancesToReturn);

		when(searchService.parseRequest(any(SearchRequest.class))).thenReturn(argumentsToReturn);
		when(instanceService.loadByDbId(any(List.class))).thenReturn(instancesToReturn);
		when(instanceService.loadByDbId(any(String.class))).thenReturn(mock(Instance.class));
	}

	/**
	 * Verifies that {@link AnnotationSearchRequest} is built with the correct parameters, according to the passed
	 * {@link AnnotationsSearchCriteria}.
	 */
	@Test
	public void testSearchCriteriaIsProperlyParsed() {
		Date createdFrom = new Date();
		Date createdTo = new Date();
		AnnotationsSearchCriteria criteria = mockCriteria(createdFrom, createdTo);

		discussionsRest.searchAnnotations(criteria);

		DateRange expectedDateRange = new DateRange(createdFrom, createdTo);
		// we expect the manually selected objects to be merged with the ones found by the search criteria
		List<String> expectedInstanceIds = Arrays.asList("manuallySelected1", "manuallySelected2", "object1",
				"object2");
		List<String> expectedUserIds = Arrays.asList("user1", "user2");

		ArgumentCaptor<AnnotationSearchRequest> requestCaptor = ArgumentCaptor.forClass(AnnotationSearchRequest.class);
		verify(annotationService).searchAnnotations(requestCaptor.capture());

		assertEquals(expectedInstanceIds, requestCaptor.getValue().getInstanceIds());
		assertEquals(expectedDateRange, requestCaptor.getValue().getDateRange());
		assertEquals(expectedUserIds, requestCaptor.getValue().getUserIds());
		assertEquals(Integer.valueOf(1), requestCaptor.getValue().getOffset());
		assertEquals(Integer.valueOf(20), requestCaptor.getValue().getLimit());
	}

	/**
	 * Verifies that the returned {@link DiscussionsResponse} is built with the correct annotations list and headers
	 * mapping.
	 */
	@Test
	public void testResponseIsProperlyBuilt() {
		Date createdFrom = new Date();
		Date createdTo = new Date();
		AnnotationsSearchCriteria criteria = mockCriteria(createdFrom, createdTo);

		Collection<Annotation> returnedAnnotations = mockReturnedAnnotations();
		when(annotationService.searchAnnotations(any(AnnotationSearchRequest.class))).thenReturn(returnedAnnotations);

		DiscussionsResponse expectedResponse = new DiscussionsResponse();
		expectedResponse.setAnnotations((List<Annotation>) returnedAnnotations);
		Map<String, String> expectedHeadersMapping = new HashMap<>();
		expectedHeadersMapping.put("object1", "header1");
		expectedHeadersMapping.put("object2", "header2");
		expectedResponse.setTargetInstanceHeaders(expectedHeadersMapping);

		DiscussionsResponse actualResponse = discussionsRest.searchAnnotations(criteria);

		assertEquals(returnedAnnotations, actualResponse.getAnnotations());
		assertEquals(expectedHeadersMapping, actualResponse.getTargetInstanceHeaders());
	}

	/**
	 * Verifies that {@link AnnotationSearchRequest} is built with the correct parameters when
	 * {@link AnnotationService#searchAnnotationsCountOnly} is invoked.
	 */
	@Test
	public void testCountAnnotationsCriteriaProperlyBuilt() {
		Date createdFrom = new Date();
		Date createdTo = new Date();
		AnnotationsSearchCriteria criteria = mockCriteria(createdFrom, createdTo);

		discussionsRest.countAnnotations(criteria);

		DateRange expectedDateRange = new DateRange(createdFrom, createdTo);
		List<String> expectedInstanceIds = Arrays.asList("manuallySelected1", "manuallySelected2", "object1",
				"object2");
		List<String> expectedUserIds = Arrays.asList("user1", "user2");

		ArgumentCaptor<AnnotationSearchRequest> requestCaptor = ArgumentCaptor.forClass(AnnotationSearchRequest.class);
		verify(annotationService).searchAnnotationsCountOnly(requestCaptor.capture());

		assertEquals(expectedInstanceIds, requestCaptor.getValue().getInstanceIds());
		assertEquals(expectedDateRange, requestCaptor.getValue().getDateRange());
		assertEquals(expectedUserIds, requestCaptor.getValue().getUserIds());
		assertEquals(Integer.valueOf(1), requestCaptor.getValue().getOffset());
		assertEquals(Integer.valueOf(20), requestCaptor.getValue().getLimit());
	}

	/**
	 * Verifies that {@link AnnotationSearchRequest} is built with the correct parameters, and all instances which are
	 * manually selected but deleted are ignored.
	 */
	@Test
	public void testDeletedInstancesAreIgnored() {
		Date createdFrom = new Date();
		Date createdTo = new Date();
		AnnotationsSearchCriteria criteria = mockCriteria(createdFrom, createdTo);
		when(instanceService.loadByDbId(any(String.class))).thenReturn(mock(Instance.class)).thenReturn(null);
		discussionsRest.searchAnnotations(criteria);

		DateRange expectedDateRange = new DateRange(createdFrom, createdTo);
		// we expect deleted instances to be ignored
		List<String> expectedInstanceIds = Arrays.asList("manuallySelected1", "object1", "object2");
		ArgumentCaptor<AnnotationSearchRequest> requestCaptor = ArgumentCaptor.forClass(AnnotationSearchRequest.class);
		verify(annotationService).searchAnnotations(requestCaptor.capture());

		assertEquals(expectedInstanceIds, requestCaptor.getValue().getInstanceIds());
		assertEquals(expectedDateRange, requestCaptor.getValue().getDateRange());
		assertEquals(Integer.valueOf(1), requestCaptor.getValue().getOffset());
		assertEquals(Integer.valueOf(20), requestCaptor.getValue().getLimit());
	}

	private static Collection<Annotation> mockReturnedAnnotations() {
		Collection<Annotation> returnedAnnotations = new LinkedList<>();
		Annotation annotation1 = new Annotation();
		annotation1.add(AnnotationProperties.HAS_TARGET, "object1");

		returnedAnnotations.add(annotation1);
		Annotation annotation2 = new Annotation();
		annotation2.add(AnnotationProperties.HAS_TARGET, "object2");
		returnedAnnotations.add(annotation2);
		return returnedAnnotations;
	}

	private static List<Instance> mockReturnedInstances() {
		Instance object1 = mock(Instance.class);
		when(object1.getId()).thenReturn("object1");
		when(object1.getString(DefaultProperties.HEADER_COMPACT)).thenReturn("header1");

		Instance object2 = mock(Instance.class);
		when(object2.getId()).thenReturn("object2");
		when(object2.getString(DefaultProperties.HEADER_COMPACT)).thenReturn("header2");

		List<Instance> result = new LinkedList<>();
		result.add(object1);
		result.add(object2);
		return result;
	}

	private static AnnotationsSearchCriteria mockCriteria(Date createdFrom, Date createdTo) {
		AnnotationsSearchCriteria criteria = new AnnotationsSearchCriteria();
		Condition tree = SearchCriteriaBuilder.createConditionBuilder().build();
		criteria.setSearchTree(tree);
		List<String> manuallySelectedObjects = new LinkedList<>();
		manuallySelectedObjects.add("manuallySelected1");
		manuallySelectedObjects.add("manuallySelected2");
		criteria.setManuallySelectedObjects(manuallySelectedObjects);
		criteria.setOffset(1);
		criteria.setLimit(20);

		criteria.setCreatedFrom(createdFrom);
		criteria.setCreatedTo(createdTo);

		List<String> userIds = Arrays.asList("user1", "user2");
		criteria.setUserIds(userIds);
		return criteria;
	}
}
