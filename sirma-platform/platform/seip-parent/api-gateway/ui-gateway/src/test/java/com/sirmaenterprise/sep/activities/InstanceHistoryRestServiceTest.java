package com.sirmaenterprise.sep.activities;

import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.KEY_LIMIT;
import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.KEY_OFFSET;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.emf.audit.processor.RecentActivity;
import com.sirma.itt.emf.audit.processor.StoredAuditActivitiesWrapper;
import com.sirma.itt.emf.audit.processor.StoredAuditActivity;
import com.sirma.itt.emf.audit.solr.service.RecentActivitiesRetriever;
import com.sirma.itt.emf.audit.solr.service.RecentActivitiesSentenceGenerator;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.rest.models.SearchResponseWrapper;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;
import com.sirma.itt.seip.search.SearchService;

/**
 * Test for {@link InstanceHistoryRestService}
 *
 * @author BBonev
 */
public class InstanceHistoryRestServiceTest {

	@InjectMocks
	private InstanceHistoryRestService rest;

	@Mock
	private UriInfo uriInfo;
	@Mock
	private MultivaluedMap<String, String> query;
	@Mock
	private MultivaluedMap<String, String> path;
	@Mock
	private RequestInfo requestInfo;
	@Mock
	private RecentActivitiesRetriever retriever;
	@Mock
	private RecentActivitiesSentenceGenerator generator;
	@Mock
	private SearchService searchService;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);

		StoredAuditActivitiesWrapper wrapper = new StoredAuditActivitiesWrapper();
		wrapper.setActivities(Collections.emptyList());
		wrapper.setTotal(110);

		when(retriever.getActivities(anyCollectionOf(Serializable.class), anyInt(), anyInt(), any(Optional.class)))
				.thenReturn(wrapper);
		when(retriever.getActivities(anyCollectionOf(Serializable.class), any(Optional.class))).thenReturn(wrapper);
		when(generator.generateSentences(anyListOf(StoredAuditActivity.class))).thenReturn(Collections.emptyList());

		when(uriInfo.getQueryParameters()).thenReturn(query);
		when(uriInfo.getPathParameters()).thenReturn(path);
		when(requestInfo.getUriInfo()).thenReturn(uriInfo);

		when(query.get(KEY_LIMIT)).thenReturn(Arrays.asList("5"));
		when(query.get(KEY_OFFSET)).thenReturn(Arrays.asList("10"));
	}

	@Test
	public void testloadHistory() {
		when(path.get("id")).thenReturn(Arrays.asList("emf:instance-id"));
		SearchResponseWrapper<RecentActivity> history = rest.loadHistory(requestInfo);

		Assert.assertEquals(110L, history.getTotal());
		Assert.assertEquals(5, history.getLimit());
		Assert.assertEquals(10, history.getOffset());
		Assert.assertTrue(CollectionUtils.isEmpty(history.getResults()));
	}

	@Test
	public void testLoadAllActivities() {
		when(query.get(KEY_LIMIT)).thenReturn(Arrays.asList("-1"));
		when(query.get(KEY_OFFSET)).thenReturn(Arrays.asList("0"));

		SearchResponseWrapper<RecentActivity> history = rest.loadHistory(requestInfo);

		Assert.assertEquals(110L, history.getTotal());
		Assert.assertEquals(-1, history.getLimit());
		Assert.assertEquals(0, history.getOffset());
		Assert.assertTrue(CollectionUtils.isEmpty(history.getResults()));
	}

	@Test
	public void loadHistoryForSearchedInstancesShouldSearch() throws Exception {
		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance-id");
		doAnswer(a -> {
			SearchArguments<Instance> arguments = a.getArgumentAt(1, SearchArguments.class);
			arguments.setResult(Arrays.asList(instance));
			return arguments;
		}).when(searchService).search(any(), any());

		SearchArguments<Instance> arguments = new SearchArguments<>();
		RecentActivitiesRequest request = new RecentActivitiesRequest()
				.setIds(new ArrayList<>())
					.setLimit(5)
					.setOffset(10);
		SearchResponseWrapper<RecentActivity> history = rest.loadHistoryForSearchedInstances(arguments, request);

		Assert.assertEquals(110L, history.getTotal());
		Assert.assertEquals(5, history.getLimit());
		Assert.assertEquals(10, history.getOffset());
		Assert.assertTrue(CollectionUtils.isEmpty(history.getResults()));
	}

	@Test
	public void batchLoadHistory() throws Exception {
		SearchResponseWrapper<RecentActivity> history = rest.batchLoadHistory(
				new RecentActivitiesRequest().setLimit(5).setOffset(10).setIds(Arrays.asList("emf:instance-id")));

		Assert.assertEquals(110L, history.getTotal());
		Assert.assertEquals(5, history.getLimit());
		Assert.assertEquals(10, history.getOffset());
		Assert.assertTrue(CollectionUtils.isEmpty(history.getResults()));
	}

}
