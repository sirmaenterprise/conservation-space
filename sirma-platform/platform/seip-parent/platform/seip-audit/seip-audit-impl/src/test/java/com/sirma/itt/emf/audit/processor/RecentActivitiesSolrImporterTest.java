package com.sirma.itt.emf.audit.processor;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.configuration.AuditConfiguration;
import com.sirma.itt.emf.solr.exception.SolrClientException;
import com.sirma.itt.emf.solr.services.SolrDataService;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;

/**
 * Test for {@link RecentActivitiesSolrImporter}
 *
 * @author BBonev
 */
public class RecentActivitiesSolrImporterTest {

	@InjectMocks
	private RecentActivitiesSolrImporter solrImporter;

	@Mock
	private AuditConfiguration auditConfiguration;
	@Mock
	private SolrDataService importService;
	@Mock
	private AuditActivityReducer activityReducer;
	@Mock
	private SolrClient solrClient;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		when(activityReducer.reduce(anyCollection())).thenReturn(Arrays.asList(createStoredActivity()).stream());
		when(auditConfiguration.getRecentActivitiesSolrClient()).then(a -> new ConfigurationPropertyMock<>(solrClient));
	}

	private static StoredAuditActivity createStoredActivity() {
		StoredAuditActivity activity = new StoredAuditActivity();
		return activity;
	}

	@Test
	public void doNothingOnNoData() throws Exception {
		solrImporter.importActivities(null);
		solrImporter.importActivities(Collections.emptyList());

		verify(importService, never()).addData(any(), anyCollection());
	}

	@Test(expected = RollbackedException.class)
	public void shouldFailOnFailedImport() throws Exception {
		when(importService.addData(any(), anyCollection())).thenThrow(SolrClientException.class);

		solrImporter.importActivities(Arrays.asList(new AuditActivity()));
	}

	@Test
	public void onSuccessfulImport() throws Exception {
		when(importService.addData(any(), anyCollection())).thenReturn(mock(SolrResponse.class));

		solrImporter.importActivities(Arrays.asList(new AuditActivity()));

		verify(importService).addData(any(), anyCollection());
	}

	@Test
	public void getLastKnownDateOnEmptyDatabaseShouldReturnNull() throws Exception {
		when(solrClient.request(any(QueryRequest.class), any())).thenReturn(new NamedList<>());

		assertNull(solrImporter.getLastKnownActivityDate());
	}

	@Test
	public void getLastKnownDateOnNonEmptyDatabaseShouldNotReturnNull() throws Exception {

		NamedList<Object> namedList = new NamedList<>();
		SolrDocumentList documentList = new SolrDocumentList();
		SolrDocument document = new SolrDocument();
		document.put("timestamp", new Date());
		documentList.add(document);
		namedList.add("response", documentList);
		when(solrClient.request(any(QueryRequest.class), any())).thenReturn(namedList);

		assertNotNull(solrImporter.getLastKnownActivityDate());
	}

	@Test(expected = RollbackedException.class)
	public void getLastKnownDateShouldFailOnNonDateTimestamp() throws Exception {

		NamedList<Object> namedList = new NamedList<>();
		SolrDocumentList documentList = new SolrDocumentList();
		SolrDocument document = new SolrDocument();
		document.put("timestamp", "2016-10-10T13:22:55.488Z");
		documentList.add(document);
		namedList.add("response", documentList);
		when(solrClient.request(any(QueryRequest.class), any())).thenReturn(namedList);

		solrImporter.getLastKnownActivityDate();
	}

	@Test(expected = RollbackedException.class)
	public void getLastKnownDateShouldFailSolrConnectionFail() throws Exception {
		when(solrClient.request(any(QueryRequest.class), any())).thenThrow(IOException.class);

		solrImporter.getLastKnownActivityDate();
	}

	@Test(expected = RollbackedException.class)
	public void getLastKnownDateShouldFailSolrParsingFail() throws Exception {
		when(solrClient.request(any(QueryRequest.class), any())).thenThrow(SolrServerException.class);

		solrImporter.getLastKnownActivityDate();
	}

	@Test(expected = RollbackedException.class)
	public void getLastKnownDateShouldFailOnMissingSolrClient() throws Exception {
		ConfigurationProperty<SolrClient> clientConfig = Mockito.mock(ConfigurationProperty.class);
		Mockito.when(clientConfig.isNotSet()).thenReturn(true);
		when(auditConfiguration.getRecentActivitiesSolrClient()).then(a -> clientConfig);

		solrImporter.getLastKnownActivityDate();
	}
}
