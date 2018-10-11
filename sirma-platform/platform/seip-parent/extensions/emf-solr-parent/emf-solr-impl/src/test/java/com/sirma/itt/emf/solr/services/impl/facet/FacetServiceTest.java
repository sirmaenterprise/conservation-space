package com.sirma.itt.emf.solr.services.impl.facet;

import static org.mockito.Matchers.anyCollection;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.sirma.itt.emf.label.retrieve.FieldValueRetrieverService;
import com.sirma.itt.emf.solr.exception.SolrClientException;
import com.sirma.itt.emf.solr.services.SolrConnector;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.concurrent.GenericAsyncTask;
import com.sirma.itt.seip.concurrent.TaskExecutor;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.SearchableProperty;
import com.sirma.itt.seip.domain.search.facet.Facet;
import com.sirma.itt.seip.domain.search.facet.FacetConfiguration;
import com.sirma.itt.seip.domain.search.facet.FacetValue;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.search.SearchConfiguration;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.search.SearchablePropertiesService;
import com.sirma.itt.seip.search.facet.FacetArgumentTransformer;
import com.sirma.itt.seip.search.facet.FacetConfigurationProvider;
import com.sirma.itt.seip.search.facet.FacetService;
import com.sirma.itt.seip.search.facet.FacetSortService;
import com.sirma.itt.seip.testutil.mocks.DataTypeDefinitionMock;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Tests {@link FacetServiceImpl} implementation.
 */
public class FacetServiceTest {

	@Mock
	private FacetArgumentTransformer facetArgumentTransformer;

	@Mock
	private FacetResultTransformer facetResultTransformer;

	@Mock
	private FacetSortService facetSortService;

	@Mock
	private FacetConfigurationProvider facetConfigurationProvider;

	@Mock
	private FacetConfigurationProperties facetConfigurationProperties;

	@Mock
	private SolrConnector solrConnector;

	@Mock
	private SearchablePropertiesService searchablePropertiesService;

	@Mock
	private SemanticDefinitionService semanticDefinitionService;

	@Mock
	private NamespaceRegistryService namespaceRegistryService;

	@Mock
	private LabelProvider labelProvider;

	@Mock
	private TaskExecutor taskExecutor;

	@Mock
	private SearchService searchService;

	@Mock
	private DefinitionService definitionService;

	@Mock
	private FieldValueRetrieverService fieldvalueRetrieverService;

	@InjectMocks
	private FacetService facetService = new FacetServiceImpl();

	@Mock
	private SearchConfiguration searchConfiguration;

	@Spy
	FacetSolrHelper facetSolrHelper;
	@Mock
	InstanceTypeResolver instanceTypeResolver;

	/**
	 * Initialize mocks. Populate commonProperties set.
	 */
	@BeforeClass
	public void init() {
		MockitoAnnotations.initMocks(this);
		when(searchConfiguration.getSearchFacetResultExceedDisable()).thenReturn(Boolean.TRUE);
		when(searchConfiguration.getSearchResultMaxSize()).thenReturn(1000);
	}

	/**
	 * Tests the facet arguments preparing when the faceting is disabled.
	 */
	@Test
	public void testFacetArgumentsPreparingWithoutFaceting() {
		SearchRequest request = new SearchRequest(new HashMap<String, List<String>>());
		SearchArguments<Instance> searchArgs = new SearchArguments<>();
		searchArgs.setFaceted(false);

		facetService.prepareArguments(request, searchArgs);

		Assert.assertTrue(CollectionUtils.isEmpty(searchArgs.getUries()), "Faceting should be disabled!");
	}

	/**
	 * Tests the preparation of facet arguments when the faceting is enabled and some test data is provided.
	 */
	@Test
	public void testFacetArgumentsPreparing() {
		SearchRequest request = new SearchRequest(new HashMap<String, List<String>>());
		SearchArguments<Instance> searchArgs = new SearchArguments<>();
		searchArgs.setFacets(new HashMap<>());
		searchArgs.setFaceted(true);
		searchArgs.requestAllFoundInstanceIds(true);

		Facet facet = new Facet();
		facet.setId("facet");

		mockFacetArgumentTransformer(facet);
		facetService.prepareArguments(request, searchArgs);

		Assert.assertTrue(CollectionUtils.isNotEmpty(searchArgs.getFacets()), "Faceting should be enabled!");
	}

	/**
	 * Test faceting when searching returns more than one rdf type. Only common facets and rdfType should be returned.
	 *
	 * @throws SolrClientException
	 *             the solr client exception
	 */
	@Test
	public void testFacetWithMoreRDFTypes() throws SolrClientException {
		SearchArguments<Instance> arguments = mockSearchArguments(null);
		mockSemanticDefinitionService();
		SearchableProperty property = new SearchableProperty();
		property.setId("objectFacet");
		property.setLabelId(property::getId);
		property.setLabelProvider(Function.identity());
		mockSearchablePropertiesService(property);
		mockDefinitionService();
		mockSolrConnector(arguments, 2);
		mockFacetConfigurationProvider("rdfType", "field_0", "field_1", "field_2", "field_3", "field_4", "objectFacet");
		facetService.facet(arguments);
		Assert.assertEquals(arguments.getFacets().size(), 7);
	}

	/**
	 * Test faceting when searching returns more than one rdf type. Common facets and selected facets used for filtering
	 * should be returnes.
	 *
	 * @throws SolrClientException
	 *             the solr client exception
	 */
	@Test
	public void testFacetWithMoreRDFTypesAndSelectedUncommonFacets() throws SolrClientException {
		SearchArguments<Instance> arguments = mockSearchArguments(2);
		mockSemanticDefinitionService();
		mockSolrConnector(arguments, 2);
		mockFacetConfigurationProvider("rdfType", "field_0", "field_1", "field_2", "field_3", "field_4");
		facetService.facet(arguments);
		Assert.assertEquals(arguments.getFacets().size(), 7);
	}

	/**
	 * Test faceting when search returnes only one rdf type. All properties send for faceting which have values with
	 * count more than 0 are returned.
	 *
	 * @throws SolrClientException
	 *             the solr client exception
	 */
	@Test
	public void testFacetWithOneRDFTypes() throws SolrClientException {
		SearchArguments<Instance> arguments = mockSearchArguments(null);
		mockSolrConnector(arguments, 1);
		SearchableProperty property = new SearchableProperty();
		property.setId("objectFacet");
		property.setLabelId(property::getId);
		property.setLabelProvider(Function.identity());
		mockSearchablePropertiesService(property);
		mockSemanticDefinitionService();
		mockNamespaceRegistryService();
		mockInstanceTypeResolver();
		facetService.facet(arguments);
		Assert.assertEquals(arguments.getFacets().size(), 7);
		for (Entry<String, Facet> facetEntry : arguments.getFacets().entrySet()) {
			Facet facet = facetEntry.getValue();
			Assert.assertTrue(arguments.getFacets().containsKey(facet.getId()),
					"Returned facet is not amongst faceted fields.");
		}
	}

	@Test
	public void testFilterObjectFacets() throws SolrClientException {
		SearchArguments<Instance> arguments = mockSearchArguments(null);
		mockSolrConnector(arguments, 1);
		Facet facet = new Facet();
		facet.setId("objectProp");
		facet.setPropertyType("object");

		FacetValue value = new FacetValue();
		value.setCount(1);
		value.setId("test");
		value.setText("label");
		List<FacetValue> values = new ArrayList<>();
		values.add(value);
		facet.setValues(values);
		arguments.getFacets().put("objectProp", facet);
		facetService.filterObjectFacets(arguments);
	}

	/**
	 * Test the retrieval of all available facets. The returned available facets should be filtered based on the
	 * returned instance types from the semantic.
	 *
	 * @throws SolrClientException
	 *             if an exception occurs.
	 */
	@Test
	public void testGetAvailableFacets() throws SolrClientException {
		SearchArguments<Instance> searchArgs = mockSearchArguments(null);
		searchArgs.requestAllFoundInstanceIds(true);

		mockDefinitionService();
		mockSearchService(searchArgs);
		mockSemanticDefinitionService();
		mockNamespaceRegistryService();
		mockSearchablePropertiesService();

		List<Facet> facetsCopy = new ArrayList<>(searchArgs.getFacets().values());
		Mockito.when(facetSortService.sort(Matchers.anyCollection())).thenReturn(facetsCopy);

		facetService.getAvailableFacets(new SearchRequest());
		facetService.filterObjectFacets(searchArgs);

		Assert.assertTrue(searchArgs.getUries().contains("id"), "the collection should have contained 'id'");
		Assert.assertEquals(searchArgs.getFacets().get("field_0").getId(), "field_0", "the facet field_0 is missing.");
		Assert.assertEquals(searchArgs.getFacets().get("field_0").getValues(), null,
				"The available facets shouldn't have any values loaded.");
	}

	/**
	 * Test the label assignment.
	 */
	@Test
	public void testAssignLabels() {
		mockFacetResultTransformer();
		SearchArguments<Instance> arguments = new SearchArguments<>();

		Map<String, Facet> facets = new HashMap<>();
		Facet facet = new Facet();
		facet.setId("someid");
		facet.setText("someText");
		facets.put(facet.getId(), facet);
		arguments.setFacets(facets);

		facetService.assignLabels(arguments);
		Assert.assertEquals(arguments.getFacets().get("someid").getText(), "someLabel");
	}

	/**
	 * Test the queryObjectsWithBatchSize method. The mocked object facet shouldn't be changed when it goes through this
	 * task.
	 *
	 * @throws SolrClientException
	 *             a solr exception
	 */
	@Test
	public void testQueryObjectsWithBatchSize() throws SolrClientException {
		ConfigurationProperty<Integer> property = Mockito.mock(ConfigurationProperty.class);
		Mockito.when(property.get()).thenReturn(10);
		Mockito.when(facetConfigurationProperties.getBatchSize()).thenReturn(property);
		SearchArguments<Instance> searchArguments = mockSearchArguments(5);
		mockSolrConnector(searchArguments, 2);
		mockTaskExecutor();
		facetService.filterObjectFacets(searchArguments);

		// Assert that the facet value has not been removed because
		// it's invalid and that the label has not been changed.
		FacetValue facetValue = searchArguments.getFacets().get("objectFacet").getValues().get(0);
		Assert.assertNotNull(facetValue);
		Assert.assertEquals(facetValue.getText(), "someHeader");
	}

	private void mockInstanceTypeResolver() {
		when(instanceTypeResolver.resolveReferences(anyCollection())).thenAnswer(a -> {
			Collection<?> collection = a.getArgumentAt(0, Collection.class);

			Collection<InstanceReference> result = new ArrayList<>();
			int i = 0;
			for (Object id : collection) {
				InstanceReference reference = Mockito.mock(InstanceReference.class);
				String uri = "rdfType_value_" + i++;
				ClassInstance instance = new ClassInstance();
				instance.setId(uri);
				when(reference.getType()).thenReturn(instance);
				when(reference.getId()).thenReturn(id.toString());
				result.add(reference);
			}
			return result;
		});
	}

	/**
	 * Mock search arguments object.
	 *
	 * @param numberOfSelectedUncommon
	 *            the number of selected uncommon properties
	 * @return the search arguments used for searching and faceting
	 */
	private static SearchArguments<Instance> mockSearchArguments(Integer numberOfSelectedUncommon) {
		SearchArguments<Instance> arguments = new SearchArguments<>();
		arguments.requestAllFoundInstanceIds(true);
		arguments.setFaceted(true);
		arguments.setUries(new ArrayList<>());
		for (int i = 0; i < 10; i++) {
			arguments.getUries().add(Integer.toString(i));
		}
		arguments.setFacets(new HashMap<String, Facet>(7));
		Facet rdfFacet = new Facet();
		rdfFacet.setId("rdfType");
		rdfFacet.setSolrFieldName("rdfType");

		// Create and add facet values to the rdf type facet.
		FacetValue rdfTypeFacetValue = new FacetValue();
		rdfTypeFacetValue.setCount(1);
		rdfTypeFacetValue.setText("rdfType");
		rdfTypeFacetValue.setId("rdfType_value_0");
		List<FacetValue> facetValues = new ArrayList<>();
		facetValues.add(rdfTypeFacetValue);
		rdfFacet.setValues(facetValues);

		// Create an object facet.
		Facet objectFacet = new Facet();
		objectFacet.setId("objectFacet");
		objectFacet.setSolrFieldName("objectFacetSolrFieldName");
		objectFacet.setPropertyType("object");
		FacetValue objectFacetValue = new FacetValue();
		objectFacetValue.setCount(1);
		objectFacetValue.setText("someHeader");
		objectFacetValue.setId("objectFacetValueId");
		List<FacetValue> objectFacetvalues = new ArrayList<>();
		objectFacetvalues.add(objectFacetValue);
		objectFacet.setValues(objectFacetvalues);

		arguments.getFacets().put(rdfFacet.getId(), rdfFacet);
		arguments.getFacets().put(objectFacet.getId(), objectFacet);
		for (int i = 0; i < 5; i++) {
			Facet facet = new Facet();
			facet.setId("field_" + i);
			facet.setSolrFieldName("solr_field_" + i);

			if (numberOfSelectedUncommon != null && numberOfSelectedUncommon > 0 && numberOfSelectedUncommon <= 3
					&& i > 2) {
				Set<String> selectedValues = new HashSet<>();
				selectedValues.add(facet.getSolrFieldName() + "_value_0");
				facet.setSelectedValues(selectedValues);
				numberOfSelectedUncommon--;
			}
			arguments.getFacets().put(facet.getId(), facet);
		}
		return arguments;
	}

	/**
	 * Mock solr connector.
	 *
	 * @param arguments
	 *            search arguments used for faceting
	 * @param rdfTypeCount
	 *            number of returned rdf types after searching
	 * @throws SolrClientException
	 *             the solr client exception
	 */
	private void mockSolrConnector(SearchArguments<Instance> arguments, int rdfTypeCount) throws SolrClientException {
		Assert.assertTrue(rdfTypeCount > 0, "RDF Type must be greater than 0.");
		NamedList<Object> response = new NamedList<>();
		List<FacetField> facets = new ArrayList<>();
		List<Count> facetValues = new ArrayList<>();
		SolrDocumentList solrDocumentList = new SolrDocumentList();
		for (Facet facet : arguments.getFacets().values()) {
			String solrFieldName = facet.getSolrFieldName();
			int count = 5;
			if (solrFieldName.equals("rdfType")) {
				count = rdfTypeCount;
			}
			FacetField field = Mockito.mock(FacetField.class);
			Mockito.when(field.getName()).thenReturn(solrFieldName);
			Mockito.when(field.getValueCount()).thenReturn(count);
			Mockito.when(field.getValues()).thenReturn(facetValues);
			for (int i = 0; i < count; i++) {
				Count facetValue = new Count(field, solrFieldName + "_value_" + i, 5);
				facetValues.add(facetValue);
				SolrDocument document = new SolrDocument();
				document.setField("breadcrumb_header", "header");
				document.setField(DefaultProperties.URI, solrFieldName);
				solrDocumentList.add(document);
			}
			facets.add(field);

		}

		NamedList<Object> facetCounts = new NamedList<>();
		response.add("facet_counts", facetCounts);

		QueryResponse solrQueryResponse = Mockito.mock(QueryResponse.class);
		Mockito.when(solrQueryResponse.getResponse()).thenReturn(response);
		Mockito.when(solrQueryResponse.getFacetFields()).thenReturn(facets);
		Mockito.when(solrQueryResponse.getResults()).thenReturn(solrDocumentList);
		Mockito.when(solrConnector.queryWithPost(Matchers.any(SolrQuery.class))).thenReturn(solrQueryResponse);
	}

	/**
	 * Mock searchable properties service.
	 */
	private void mockSearchablePropertiesService(SearchableProperty... properties) {
		List<SearchableProperty> searchableProperties = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			SearchableProperty searchableProperty = new SearchableProperty();
			searchableProperty.setId("field_" + i);
			searchableProperty.setLabelId(searchableProperty::getId);
			searchableProperty.setLabelProvider(Function.identity());
			searchableProperties.add(searchableProperty);
		}
		searchableProperties.addAll(Arrays.asList(properties));
		Mockito
				.when(searchablePropertiesService.getSearchableSolrProperties(Matchers.anyString(),
						Matchers.anyBoolean(), Matchers.anyBoolean(), Matchers.anyBoolean()))
					.thenReturn(searchableProperties);
	}

	/**
	 * Mock semantic definition service.
	 */
	private void mockSemanticDefinitionService() {
		List<String> classes = new ArrayList<>(1);
		classes.add("rdfType_value_0");
		Mockito.when(semanticDefinitionService.getTopLevelTypes()).thenReturn(() -> classes);
	}

	/**
	 * Mock namespace registry service.
	 */
	private void mockNamespaceRegistryService() {
		Mockito.when(namespaceRegistryService.getShortUri(Matchers.anyString())).thenAnswer(
				invocation -> (String) invocation.getArguments()[0]);
	}

	private void mockFacetConfigurationProvider(String... configurations) {
		for (String configuration : configurations) {
			FacetConfiguration facetConfiguration = new FacetConfiguration();
			facetConfiguration.setDefault(true);
			facetConfiguration.setOrder(0);
			facetConfiguration.setSort("alphabetical");
			facetConfiguration.setSortOrder("ascending");
			facetConfiguration.setLabel(configuration);
			Mockito.when(facetConfigurationProvider.getFacetConfigField(configuration)).thenReturn(facetConfiguration);
		}
	}

	/**
	 * Mock the facet argument transformer so that when the populateFacets method is called, it adds the input facets to
	 * the searchArgs.
	 *
	 * @param facets
	 */
	@SuppressWarnings("unchecked")
	private void mockFacetArgumentTransformer(Facet... facets) {
		Mockito.doAnswer(invocation -> {
			SearchArguments<Instance> searchArgs = (SearchArguments<Instance>) invocation.getArguments()[1];
			for (Facet facet : facets) {
				searchArgs.getFacets().put(facet.getId(), facet);
			}
			return null;
		}).when(facetArgumentTransformer).populateFacets(Matchers.any(SearchRequest.class),
				Matchers.any(SearchArguments.class));
	}

	@SuppressWarnings("unchecked")
	private void mockSearchService(SearchArguments<Instance> searchArgs) {
		Mockito.when(searchService.parseRequest(Matchers.any(SearchRequest.class))).thenReturn(searchArgs);
		Mockito.doAnswer(invocation -> {
			SearchArguments<Instance> args = (SearchArguments<Instance>) invocation.getArguments()[1];
			args.setUries(new ArrayList<>());
			args.getUries().add("id");
			return null;
		}).when(searchService).search(Matchers.any(), Matchers.any(SearchArguments.class));
	}

	private void mockDefinitionService() {
		DataTypeDefinition dataTypeDefinition = new DataTypeDefinitionMock(Instance.class, "rdfType_value_0");
		Mockito.when(definitionService.getDataTypeDefinition(Mockito.anyObject())).thenReturn(dataTypeDefinition);
	}

	/**
	 * Mock the facet result transformer so it assigns labels on the given facet values.
	 */
	private void mockFacetResultTransformer() {
		Mockito.doAnswer(invocation -> {
			@SuppressWarnings("unchecked")
			Collection<Facet> facets = (Collection<Facet>) invocation.getArguments()[0];
			for (Facet facet : facets) {
				facet.setText("someLabel");
			}
			return null;
		}).when(facetResultTransformer).assignLabels(Matchers.any());
	}

	private void mockTaskExecutor() {
		Mockito.doAnswer(invocation -> {
			@SuppressWarnings("unchecked")
			List<GenericAsyncTask> tasks = (List<GenericAsyncTask>) invocation.getArguments()[0];
			for (GenericAsyncTask task : tasks) {
				task.call();
			}
			return null;
		}).when(taskExecutor).execute(Matchers.any());
	}
}
