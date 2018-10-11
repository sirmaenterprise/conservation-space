/**
 *
 */
package com.sirma.itt.emf.label.retrieve;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.TITLE;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.emf.solr.exception.SolrClientException;
import com.sirma.itt.emf.solr.services.SolrConnector;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * @author BBonev
 *
 */
public class ObjectFieldValueRetrieverTest {

	@Mock
	private SolrConnector solrConnector;
	@InjectMocks
	private ObjectFieldValueRetriever objectFieldValueRetriever = new ObjectFieldValueRetriever();
	@Mock
	NamespaceRegistryService namespaceRegistryService;
	@Spy
	ConfigurationProperty<String> filterQueryObjects = new ConfigurationPropertyMock<>("defaultFq");

	/**
	 * Init the annotations before each test.
	 */
	@BeforeMethod
	private void init() {
		MockitoAnnotations.initMocks(this);
		when(namespaceRegistryService.buildFullUri(anyString())).then(a -> a.getArgumentAt(0, String.class));
		when(namespaceRegistryService.getShortUri(anyString())).then(a -> a.getArgumentAt(0, String.class));
	}

	/**
	 * Testing the getLabels method where a header is generated and put in the headers map.
	 *
	 * @throws SolrClientException
	 *             an exception from solr
	 */
	@Test
	public void testGetLabels() throws SolrClientException {
		initMocks("someType", "instanceType", "uri");
		Map<String, String> headers = objectFieldValueRetriever.getLabels(new String[] { "uri" }, null);
		Assert.assertEquals(headers.get("uri"), "someType");

	}

	/**
	 * Testing the getLabels method where a header is not generated because a solr exception has occured when quering
	 * solr for the results.
	 *
	 * @throws SolrClientException
	 *             an exception from solr
	 */
	@Test
	public void testGetLabelsSolrException() throws SolrClientException {
		Mockito.when(solrConnector.queryWithPost(Matchers.any(SolrQuery.class))).thenThrow(
				new SolrClientException(new Exception()));
		Map<String, String> headers = objectFieldValueRetriever.getLabels(new String[] { "shortUri" }, null);
		Assert.assertEquals(headers.get("shortUri"), null);

	}

	/**
	 * Testing the getLabel method where a header is generated and returned.
	 *
	 * @throws SolrClientException
	 *             an exception from solr
	 */
	@Test
	public void testGetLabel() throws SolrClientException {
		initMocks("someType", "instanceType", "uri");
		String header = objectFieldValueRetriever.getLabel("shortUri", null);
		Assert.assertEquals(header, "someType");
	}

	/**
	 * Testing the getLabels method where the dictionary service is not mocked so a header can't be generated for that
	 * instance.
	 *
	 * @throws SolrClientException
	 *             an exception from solr
	 */
	@Test
	public void testGetLabelsWithoutTypeDefinition() throws SolrClientException {
		initMocks("someType", "instanceType", "uri");
		Map<String, String> headers = objectFieldValueRetriever.getLabels(new String[] { "shortUri" }, null);
		Assert.assertEquals(headers.get("shortUri"), null);
	}

	/**
	 * Testing the getLabels method where solr returns a null response so the generated header is null.
	 *
	 * @throws SolrClientException
	 *             an exception from solr
	 */
	@Test
	public void testGetLabelsWithNoQueryResponse() throws SolrClientException {
		mockSolrConnector(null);
		Map<String, String> headers = objectFieldValueRetriever.getLabels(new String[] { "shortUri" }, null);
		Assert.assertEquals(headers.get("shortUri"), null);
	}

	/**
	 * Testing the getSupportedObjects method.The {@link ObjectFieldValueRetriever} should be responsible for generating
	 * header labels only.
	 */
	@Test
	public void testGetSupportedObjects() {
		Assert.assertEquals(objectFieldValueRetriever.getSupportedObjects().get(0), FieldId.OBJECT);
	}

	@SuppressWarnings("boxing")
	@Test
	public void test_getValues() throws SolrClientException {
		initMocks("someType", new String[] { "someInstanceType", "anotherOne" }, "uri");
		RetrieveResponse response = objectFieldValueRetriever.getValues("test", null, 0, 10);
		assertNotNull(response);
	}

	@SuppressWarnings("boxing")
	@Test
	public void test_getValues_withAdditionalParams() throws SolrClientException {
		initMocks("someType", new String[] { "someInstanceType", "anotherOne" }, "uri");
		SearchRequest params = new SearchRequest(new HashMap<>());
		params.add(FieldValueRetrieverParameters.FIELD, TITLE);
		params.add(FieldValueRetrieverParameters.FILTER_QUERY, "fq");
		RetrieveResponse response = objectFieldValueRetriever.getValues("test", params, 0, 10);
		assertNotNull(response);
	}

	/**
	 * Mock the {@link SolrConnector} so it returns the a mocked {@link SolrResponse} with a single {@link SolrDocument}
	 * with it's fields set.
	 *
	 * @param solrDocumentFields
	 *            the fields of the solr document to be set.
	 * @throws SolrClientException
	 *             an exception from solr
	 */
	private void mockSolrConnector(Map<String, Object> solrDocumentFields) throws SolrClientException {
		if (solrDocumentFields != null) {
			QueryResponse queryResponse = mockSolrResponse(solrDocumentFields);
			Mockito.when(solrConnector.queryWithPost(Matchers.any(SolrQuery.class))).thenReturn(queryResponse);
			Mockito.when(solrConnector.queryWithGet(Matchers.any(SolrQuery.class))).thenReturn(queryResponse);
		}
	}

	/**
	 * Mock the {@link SolrResponse} so it returns a list containing one solr document with it's field set.
	 *
	 * @param solrDocumentFields
	 *            the fields to be set.
	 * @return the query response
	 */
	private static QueryResponse mockSolrResponse(Map<String, Object> solrDocumentFields) {
		QueryResponse solrResponse = Mockito.mock(QueryResponse.class);
		SolrDocumentList solrDocumentList = new SolrDocumentList();

		solrDocumentList.add(createSolrDocument(solrDocumentFields));
		Mockito.when(solrResponse.getResults()).thenReturn(solrDocumentList);
		return solrResponse;
	}

	/**
	 * Create a {@link SolrDocument} with it's fields set.
	 *
	 * @param solrDocumentFields
	 *            the fields to set
	 * @return the solr document
	 */
	private static SolrDocument createSolrDocument(Map<String, Object> solrDocumentFields) {
		SolrDocument solrDocument = new SolrDocument();
		for (Entry<String, Object> field : solrDocumentFields.entrySet()) {
			solrDocument.setField(field.getKey(), field.getValue());
		}
		return solrDocument;
	}

	/**
	 * Init all mocks. Used to avoid code duplication.
	 * @param type
	 *            the type field of the returned solr document
	 * @param instanceType
	 *            the instanceType of the returned solr document
	 * @param uri
	 *            the uri of the returned solr document
	 * @throws SolrClientException
	 *             a solr exception
	 */
	private void initMocks(Object type, Object instanceType, Object uri) throws SolrClientException {
		Map<String, Object> solrDocumentFields = new HashMap<>();
		solrDocumentFields.put("instanceType", instanceType);
		solrDocumentFields.put(DefaultProperties.URI, uri);
		solrDocumentFields.put("type", type);
		solrDocumentFields.put(TITLE, type);
		solrDocumentFields.put("isDeleted", "false");

		mockSolrConnector(solrDocumentFields);
	}
}
