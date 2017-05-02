package com.sirma.itt.emf.audit.solr.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.emf.audit.configuration.AuditConfiguration;
import com.sirma.itt.emf.audit.processor.StoredAuditActivitiesWrapper;
import com.sirma.itt.emf.audit.processor.StoredAuditActivity;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.time.DateRange;

/**
 * Test for {@link RecentActivitiesRetrieverImpl}.
 *
 * @author nvelkov
 */
public class RecentActivitiesRetrieverImplTest {

	@Mock
	private AuditConfiguration configuration;

	@InjectMocks
	private RecentActivitiesRetriever retriever = new RecentActivitiesRetrieverImpl();

	/**
	 * Init the mocks.
	 */
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Test the get activities method.
	 *
	 * @throws SolrServerException
	 *             the solr server exception
	 * @throws IOException
	 *             the io exception
	 */
	@Test
	public void testGetActivities() throws SolrServerException, IOException {
		SolrDocument document = createDocument();
		mockSolrClient(createQueryResponse(document));

		StoredAuditActivitiesWrapper wrapper = retriever.getActivities(Arrays.asList("1"));
		assertActivities(wrapper.getActivities(), document);
	}

	/**
	 * Test the activities retrieval method with offset and limit. Solr should handle this logic.
	 *
	 * @throws SolrServerException
	 *             the solr server exception
	 * @throws IOException
	 *             the io exception
	 */
	@Test
	public void testGetActivitiesWithLimitAndOffset() throws SolrServerException, IOException {
		SolrDocument document = createDocument();
		mockSolrClient(createQueryResponse(document));

		StoredAuditActivitiesWrapper wrapper = retriever.getActivities(Arrays.asList("1"), 0, 100);
		assertActivities(wrapper.getActivities(), document);
	}

	@Test
	public void getActivitiesWithLimitAndOffsetAndDateRage() throws SolrServerException, IOException {
		SolrDocument document = createDocument();
		try (SolrClient clientMock = mockSolrClient(createQueryResponse(document))) {

			StoredAuditActivitiesWrapper wrapper = retriever.getActivities(Arrays.asList("1"), 0, 100,
					Optional.of(new DateRange(new Date(), new Date())));
			assertActivities(wrapper.getActivities(), document);
			verify(clientMock).query(argThat(CustomMatcher.of((SolrQuery query) -> {
				assertEquals(2, query.getFilterQueries().length);
			})), eq(METHOD.POST));
		}
	}

	private static void assertActivities(List<StoredAuditActivity> activities, SolrDocument document) {
		Assert.assertEquals(activities.size(), 1);
		Assert.assertEquals(activities.get(0).getInstanceId(), document.getFieldValue("instanceId"));
		Assert.assertEquals(activities.get(0).getRelation(), document.getFieldValue("relation"));
		Assert.assertEquals(activities.get(0).getUserId(), document.getFieldValue("userId"));
		Assert.assertEquals(activities.get(0).getAction(), document.getFieldValue("action"));
		Assert.assertEquals(activities.get(0).getInstanceType(), document.getFieldValue("instanceType"));
		Assert.assertEquals(activities.get(0).getOperation(), document.getFieldValue("operation"));
		Assert.assertEquals(activities.get(0).getRequestId(), document.getFieldValue("requestId"));
		Assert.assertEquals(activities.get(0).getTimestamp(), document.getFieldValue("timestamp"));
		Assert.assertEquals(activities.get(0).getAddedTargetProperties(),
				document.getFieldValue("addedTargetProperties"));
		Assert.assertEquals(activities.get(0).getRemovedTargetProperties(),
				document.getFieldValue("removedTargetProperties"));
		Assert.assertEquals(activities.get(0).getIds(), document.getFieldValue("ids"));
	}

	/**
	 * Test the activities retrieval method when a solr server exception is thrown from solr.
	 *
	 * @throws SolrServerException
	 *             the solr server exception
	 * @throws IOException
	 *             the io exception
	 */
	@SuppressWarnings("resource")
	@Test(expected = EmfApplicationException.class)
	public void getActivitiesSolrServerException() throws SolrServerException, IOException {
		SolrDocument document = createDocument();
		SolrClient client = mockSolrClient(createQueryResponse(document));
		when(client.query(any(SolrQuery.class), any())).thenThrow(new SolrServerException("exc"));

		retriever.getActivities(Arrays.asList("1"));
	}

	/**
	 * Test the activities retrieval method when an io exception is thrown from solr.
	 *
	 * @throws SolrServerException
	 *             the solr server exception
	 * @throws IOException
	 *             the io exception
	 */
	@SuppressWarnings("resource")
	@Test(expected = EmfApplicationException.class)
	public void getActivitiesIOException() throws SolrServerException, IOException {
		SolrDocument document = createDocument();
		SolrClient client = mockSolrClient(createQueryResponse(document));
		when(client.query(any(SolrQuery.class), Matchers.any())).thenThrow(new IOException("exc"));

		retriever.getActivities(Arrays.asList("1"));
	}

	@Test
	public void testGetActivitiesSolrClientNotSet() throws SolrServerException, IOException {
		ConfigurationProperty<SolrClient> property = new ConfigurationPropertyMock<>();
		when(configuration.getRecentActivitiesSolrClient()).thenReturn(property);

		StoredAuditActivitiesWrapper wrapper = retriever.getActivities(Arrays.asList("1"), 0, 100);
		Assert.assertEquals(wrapper.getTotal(), 0);
		Assert.assertEquals(wrapper.getActivities().size(), 0);
	}

	private SolrClient mockSolrClient(QueryResponse response) throws SolrServerException, IOException {
		SolrClient client = mock(SolrClient.class);
		when(client.query(any(SolrQuery.class), any())).thenReturn(response);

		ConfigurationProperty<SolrClient> property = new ConfigurationPropertyMock<>(client);
		when(configuration.getRecentActivitiesSolrClient()).thenReturn(property);
		return client;
	}

	private static QueryResponse createQueryResponse(SolrDocument document) {
		SolrDocumentList documents = new SolrDocumentList();
		documents.add(document);
		documents.setNumFound(1);

		NamedList<Object> results = new NamedList<>();
		results.add("response", documents);

		QueryResponse response = new QueryResponse();
		response.setResponse(results);
		return response;
	}

	private static SolrDocument createDocument() {
		SolrDocument document = new SolrDocument();
		document.setField("instanceId", "1");
		document.setField("instanceType", "type");
		document.setField("timestamp", new Date());
		document.setField("relation", "relation");
		document.setField("operation", "operation");
		document.setField("requestId", "requestId");
		document.setField("action", "action");
		document.setField("userId", "user");
		document.setField("addedTargetProperties", new HashSet<>(Arrays.asList("addedProperties")));
		document.setField("removedTargetProperties", new HashSet<>(Arrays.asList("removedProperties")));
		document.setField("ids", new HashSet<>(Arrays.asList("1", "2")));
		return document;
	}

}
