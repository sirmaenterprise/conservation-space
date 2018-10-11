package com.sirma.itt.emf.label.retrieve;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

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
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.HeadersService;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Tests the HeaderFieldValueRetriever.
 *
 * @author nvelkov
 */
public class HeaderFieldValueRetrieverTest {

	@InjectMocks
	private HeaderFieldValueRetriever headerFieldValueRetriever = new HeaderFieldValueRetriever();

	@Mock
	private SolrConnector solrConnector;

	@Mock
	private HeadersService headersService;

	@Mock
	private InstanceTypeResolver instanceTypeResolver;

	@Spy
	private ConfigurationProperty<String> filterQueryObjects = new ConfigurationPropertyMock<>("");

	@Mock
	private InstanceService instanceService;

	@Mock
	private NamespaceRegistryService namespaceRegistryService;

	/**
	 * Init the annotations before each test.
	 */
	@BeforeMethod
	private void init() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Testing the getLabels method where a header is generated and put in the headers map.
	 */
	@Test
	public void testGetLabels() throws SolrClientException {
		initMocks(true, true, true, "someType", "instanceType", "uri");
		Map<String, String> headers = headerFieldValueRetriever.getLabels(new String[] { "shortUri" }, null);
		Assert.assertEquals(headers.get("shortUri"), "generatedHeader");
	}

	/**
	 * Testing the getLabels method where a header is not generated because a solr exception has occured when quering
	 * solr for the results.
	 */
	@Test
	public void testGetLabelsSolrException() throws SolrClientException {
		Mockito.when(solrConnector.queryWithPost(Matchers.any(SolrQuery.class))).thenThrow(
				new SolrClientException(new Exception()));
		mockInstanceService();
		Map<String, String> headers = headerFieldValueRetriever.getLabels(new String[] { "shortUri" }, null);
		Assert.assertEquals(headers.get("shortUri"), null);
	}

	/**
	 * Testing the getLabel method where a header is generated and returned.
	 */
	@Test
	public void testGetLabel() throws SolrClientException {
		initMocks(true, true, true, "someType", "instanceType", "uri");
		String header = headerFieldValueRetriever.getLabel("shortUri", null);
		Assert.assertEquals(header, "generatedHeader");
	}

	/**
	 * Testing the getLabels method where the dictionary service is not mocked so a header can't be generated for that
	 * instance.
	 */
	@Test
	public void testGetLabelsWithoutTypeDefinition() throws SolrClientException {
		initMocks(false, true, true, "someType", "instanceType", "uri");
		mockInstanceService();
		Map<String, String> headers = headerFieldValueRetriever.getLabels(new String[] { "shortUri" }, null);
		Assert.assertEquals(headers.get("shortUri"), null);
	}

	/**
	 * Testing the getLabels method where multiple instance types are returned from solr so only the first one is used.
	 */
	@Test
	public void testGetLabelsWithMultipleReturnedInstanceTypes() throws SolrClientException {
		initMocks(true, true, true, "someType", new String[] { "someInstanceType", "anotherOne" }, "uri");
		Map<String, String> headers = headerFieldValueRetriever.getLabels(new String[] { "shortUri" }, null);
		Assert.assertEquals(headers.get("shortUri"), "generatedHeader");
	}

	/**
	 * Testing the getLabels method where solr returns a null response so the generated header is null.
	 */
	@Test
	public void testGetLabelsWithNoQueryResponse() throws SolrClientException {
		mockSolrConnector(null);
		mockInstanceService();
		Map<String, String> headers = headerFieldValueRetriever.getLabels(new String[] { "shortUri" }, null);
		Assert.assertEquals(headers.get("shortUri"), null);
	}

	/**
	 * Testing the getSupportedObjects method.The {@link HeaderFieldValueRetriever} should be responsible for generating
	 * header labels only.
	 */
	@Test
	public void testGetSupportedObjects() {
		Assert.assertEquals(FieldId.HEADER, headerFieldValueRetriever.getSupportedObjects().get(0));
	}

	@SuppressWarnings("boxing")
	@Test
	public void test_getValues() throws SolrClientException {
		initMocks(true, true, true, "someType", new String[] { "someInstanceType", "anotherOne" }, "uri");
		RetrieveResponse response = headerFieldValueRetriever.getValues("test", null, 0, 10);
		assertNotNull(response);
	}

	/**
	 * Mock the {@link SolrConnector} so it returns the a mocked {@link SolrResponse} with a single {@link SolrDocument}
	 * with it's fields set.
	 *
	 * @param solrDocumentFields
	 *            the fields of the solr document to be set.
	 */
	private void mockSolrConnector(Map<String, Object> solrDocumentFields) throws SolrClientException {
		if (solrDocumentFields != null) {
			QueryResponse queryResponse = mockSolrResponse(solrDocumentFields);
			when(solrConnector.queryWithPost(Matchers.any(SolrQuery.class))).thenReturn(queryResponse);
		}
	}

	/**
	 * Mock the {@link SolrResponse} so it returns a list containing one solr document with it's field set.
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
	 */
	private static SolrDocument createSolrDocument(Map<String, Object> solrDocumentFields) {
		SolrDocument solrDocument = new SolrDocument();
		for (Entry<String, Object> field : solrDocumentFields.entrySet()) {
			solrDocument.setField(field.getKey(), field.getValue());
		}
		return solrDocument;
	}

	/**
	 * Mock the {@link HeadersService} so it returns a generatedHeader string.
	 */
	private void mockHeaderService() {
		when(headersService.generateInstanceHeader(any(Instance.class), anyString())).thenReturn("generatedHeader");
	}

	/**
	 * Mock the {@link InstanceService}
	 */
	private void mockInstanceService() {
		when(instanceService.loadDeleted(any(Serializable.class))).thenReturn(Optional.empty());
	}

	/**
	 * Init all mocks. Used to avoid code duplication.
	 *
	 * @param mockInstanceResolver
	 *            if the instance resolver should be mocked
	 * @param mockHeaderService
	 *            if the {@link HeadersService} should be moked
	 * @param mockSolrConnector
	 *            if the {@link SolrConnector} should be mocked
	 * @param type
	 *            the type field of the returned solr document
	 * @param instanceType
	 *            the instanceType of the returned solr document
	 * @param uri
	 *            the uri of the returned solr document
	 */
	private void initMocks(boolean mockInstanceResolver, boolean mockHeaderService, boolean mockSolrConnector,
			Object type, Object instanceType, Object uri) throws SolrClientException {
		Map<String, Object> solrDocumentFields = new HashMap<>();
		solrDocumentFields.put("instanceType", instanceType);
		solrDocumentFields.put(DefaultProperties.URI, uri);
		solrDocumentFields.put("type", type);
		solrDocumentFields.put("isDeleted", "false");
		if (mockInstanceResolver) {
			when(instanceTypeResolver.resolveInstances(anyList())).thenAnswer(a -> {
				Collection<Serializable> collection = a.getArgumentAt(0, Collection.class);
				return collection.stream().map(id -> {
					EmfInstance instance = new EmfInstance();
					instance.setId(id);
					return instance;
				}).collect(Collectors.toList());
			});
			when(namespaceRegistryService.getShortUri(anyString())).thenAnswer(a -> {
				return a.getArguments()[0];
			});

			mockInstanceService();
		}
		if (mockHeaderService) {
			mockHeaderService();
		}
		if (mockSolrConnector) {
			mockSolrConnector(solrDocumentFields);
		}
	}
}
