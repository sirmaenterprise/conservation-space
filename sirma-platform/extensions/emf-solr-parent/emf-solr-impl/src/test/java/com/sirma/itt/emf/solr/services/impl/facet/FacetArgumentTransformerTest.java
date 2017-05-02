package com.sirma.itt.emf.solr.services.impl.facet;

import static org.mockito.Matchers.any;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.ws.rs.core.MultivaluedHashMap;

import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.FacetParams;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.SearchableProperty;
import com.sirma.itt.seip.domain.search.facet.Facet;
import com.sirma.itt.seip.domain.search.facet.FacetConfiguration;
import com.sirma.itt.seip.domain.search.facet.FacetQueryParameters;
import com.sirma.itt.seip.search.SearchablePropertiesService;
import com.sirma.itt.seip.search.facet.FacetArgumentTransformer;
import com.sirma.itt.seip.search.facet.FacetConfigurationProvider;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Tests the utility methods for argument transforming in {@link FacetArgumentTransformer}.
 *
 * @author Mihail Radkov
 * @since 1.10.1
 */
public class FacetArgumentTransformerTest {

	@Mock
	private FacetConfigurationProperties configuration;

	@Mock
	private FacetConfigurationProvider facetConfigurationProvider;

	@Mock
	private SearchablePropertiesService searchablePropertiesService;

	@Mock
	private NamespaceRegistryService namespaceRegistryService;

	@InjectMocks
	private FacetArgumentTransformer argumentTransformer = new FacetArgumentTransformerImpl();

	/**
	 * Initializes mocks.
	 */
	@BeforeMethod
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Tests the logic inside {@link FacetArgumentTransformer#populateFacets(SearchRequest, SearchArguments)} when there
	 * are facets but without configurations.
	 */
	@Test
	public void testFacetPopulationWithoutConfigurations() {
		Mockito.reset(facetConfigurationProvider, searchablePropertiesService);
		mockFacetConfigurationProvider("rdfType");

		SearchableProperty searchableProperty1 = getSearchableProperty("createdBy");
		SearchableProperty searchableProperty2 = getSearchableProperty("createdOn");
		mockSearchablePropertiesService(Arrays.asList(searchableProperty1, searchableProperty2));

		SearchRequest searchRequest = new SearchRequest(new MultivaluedHashMap<>());
		SearchArguments<Instance> searchArguments = new SearchArguments<>();

		argumentTransformer.populateFacets(searchRequest, searchArguments);

		Map<String, Facet> facets = searchArguments.getFacets();
		Assert.assertEquals(facets.size(), 1);
		Assert.assertNotNull(facets.get("rdfType"));
	}

	/**
	 * Tests the logic inside {@link FacetArgumentTransformer#populateFacets(SearchRequest, SearchArguments)} when the
	 * ignoreFacetConfiguration parameter is set and only two of the facet properties have configuration.
	 */
	@Test
	public void testFacetPopulationWithIgnoredConfiguration() {
		Mockito.reset(facetConfigurationProvider, searchablePropertiesService);
		mockFacetConfigurationProvider("createdBy");
		mockFacetConfigurationProvider("createdOn");

		SearchableProperty searchableProperty1 = getSearchableProperty("createdBy");
		SearchableProperty searchableProperty2 = getSearchableProperty("createdOn");
		SearchableProperty searchableProperty3 = getSearchableProperty("priority");
		SearchableProperty searchableProperty4 = getSearchableProperty("hasChild");
		SearchableProperty searchableProperty5 = getSearchableProperty("rdfType");
		mockSearchablePropertiesService(Arrays.asList(searchableProperty1, searchableProperty2, searchableProperty3,
				searchableProperty4, searchableProperty5));

		SearchRequest searchRequest = new SearchRequest(new MultivaluedHashMap<>());
		SearchArguments<Instance> searchArguments = new SearchArguments<>();
		searchRequest.add(FacetQueryParameters.REQUEST_IGNORE_FACET_CONFIGURATION, "true");

		argumentTransformer.populateFacets(searchRequest, searchArguments);
		Map<String, Facet> facets = searchArguments.getFacets();
		Assert.assertEquals(facets.size(), 5);
		Assert.assertNotNull(facets.get("createdBy"));
		Assert.assertNotNull(facets.get("createdOn"));
		Assert.assertNotNull(facets.get("priority"));
		Assert.assertNotNull(facets.get("hasChild"));
		Assert.assertNotNull(facets.get("rdfType"));
	}

	/**
	 * Tests the logic inside {@link FacetArgumentTransformer#populateFacets(SearchRequest, SearchArguments)} when there
	 * are facets with configurations.
	 */
	@Test
	public void testFacetPopulation() {
		Mockito.reset(facetConfigurationProvider, searchablePropertiesService);
		mockFacetConfigurationProvider("rdfType");
		mockFacetConfigurationProvider("createdBy");
		mockFacetConfigurationProvider("createdOn");

		SearchableProperty searchableProperty1 = getSearchableProperty("createdBy");
		SearchableProperty searchableProperty2 = getSearchableProperty("createdOn");
		mockSearchablePropertiesService(Arrays.asList(searchableProperty1, searchableProperty2));

		SearchRequest searchRequest = new SearchRequest(new MultivaluedHashMap<>());
		searchRequest.add("facetArguments[]", "rdfType:somelonguri, rdfType:anotherlonguri");
		SearchArguments<Instance> searchArguments = new SearchArguments<>();

		argumentTransformer.populateFacets(searchRequest, searchArguments);

		Map<String, Facet> facets = searchArguments.getFacets();
		Assert.assertEquals(facets.size(), 3);
		Assert.assertNotNull(facets.get("rdfType"));
		Assert.assertNotNull(facets.get("createdBy"));
		Assert.assertNotNull(facets.get("createdOn"));
	}

	/**
	 * Test the facet population with preselected facets, meaning that the facets are provisioned separately and not
	 * taken from the {@link SearchablePropertiesService}.
	 */
	@Test
	public void testFacetPopulationWithPreselectedFacets() {
		Mockito.reset(facetConfigurationProvider, searchablePropertiesService);
		mockFacetConfigurationProvider("rdfType");
		mockFacetConfigurationProvider("type");

		SearchableProperty searchableProperty = getSearchableProperty("rdfType");
		SearchableProperty typeSearchableProperty = getSearchableProperty("type");

		mockSearchablePropertiesService(Arrays.asList(searchableProperty, typeSearchableProperty));
		SearchRequest searchRequest = new SearchRequest(new MultivaluedHashMap<>());
		searchRequest.add("facetArguments[]", "rdfType:somelonguri, rdfType:anotherlonguri");
		searchRequest.add("forType", "forType");
		searchRequest.add("facetField[]", "rdfType");
		searchRequest.add("facetField[]", "type");
		SearchArguments<Instance> searchArguments = new SearchArguments<>();

		argumentTransformer.populateFacets(searchRequest, searchArguments);

		Map<String, Facet> facets = searchArguments.getFacets();
		Assert.assertEquals(facets.size(), 2);
		Assert.assertNotNull(facets.get("rdfType"));
	}

	/**
	 * Tests the logic inside {@link FacetArgumentTransformer#preserveFacetSelections(SearchRequest, SearchArguments)}
	 * when there are <b>no</b> facet arguments in the {@link SearchRequest}.
	 */
	@Test
	public void testFacetSelectionPreservingWithoutArguments() {
		Facet facet = new Facet();
		facet.setId("myFacet");

		Map<String, Facet> facets = new HashMap<>();
		facets.put("myFacet", facet);

		SearchRequest searchRequest = new SearchRequest(new MultivaluedHashMap<>());
		SearchArguments<Instance> searchArguments = new SearchArguments<>();
		searchArguments.setFacets(facets);

		argumentTransformer.preserveFacetSelections(searchRequest, searchArguments);

		Facet myFacet = searchArguments.getFacets().get("myFacet");
		Assert.assertNull(myFacet.getSelectedValues());
	}

	/**
	 * Tests the logic inside {@link FacetArgumentTransformer#preserveFacetSelections(SearchRequest, SearchArguments)}
	 * when there are facet arguments in the {@link SearchRequest} but are not correctly provided.
	 */
	@Test
	public void testFacetSelectionPreservingWithIncorrectArguments() {
		Facet facet = new Facet();
		facet.setId("myFacet");

		Map<String, Facet> facets = new HashMap<>();
		facets.put("myFacet", facet);

		SearchRequest searchRequest = new SearchRequest(new MultivaluedHashMap<>());
		searchRequest.add(FacetQueryParameters.REQUEST_FACET_ARGUMENTS, "myFacet");

		SearchArguments<Instance> searchArguments = new SearchArguments<>();
		searchArguments.setFacets(facets);

		argumentTransformer.preserveFacetSelections(searchRequest, searchArguments);

		Facet myFacet = searchArguments.getFacets().get("myFacet");
		Assert.assertNull(myFacet.getSelectedValues());
	}

	/**
	 * Tests the logic inside {@link FacetArgumentTransformer#preserveFacetSelections(SearchRequest, SearchArguments)}
	 * when there are facet arguments in the {@link SearchRequest} and are correctly provided, but there is not a
	 * {@link Facet} for the specific argument.
	 */
	@Test
	public void testFacetSelectionPreservingWithMissingFacet() {
		Facet facet = new Facet();
		facet.setId("myFacet");

		Map<String, Facet> facets = new HashMap<>();
		facets.put("myFacet", facet);

		SearchableProperty searchableProperty1 = getSearchableProperty("myOtherFacet");
		mockSearchablePropertiesService(Arrays.asList(searchableProperty1));

		SearchRequest searchRequest = new SearchRequest(new MultivaluedHashMap<>());
		searchRequest.add(FacetQueryParameters.REQUEST_FACET_ARGUMENTS,
				"myOtherFacet" + FacetQueryParameters.ARGUMENT_SEPARATOR + "myValue");

		Mockito
				.when(searchablePropertiesService.getSearchableProperty(Matchers.any(), Matchers.eq("myOtherFacet")))
					.thenReturn(Optional.ofNullable(null));

		SearchArguments<Instance> searchArguments = new SearchArguments<>();
		searchArguments.setFacets(facets);

		argumentTransformer.preserveFacetSelections(searchRequest, searchArguments);

		Facet myFacet = searchArguments.getFacets().get("myFacet");
		Assert.assertNull(myFacet.getSelectedValues());
	}

	/**
	 * Tests the logic inside {@link FacetArgumentTransformer#preserveFacetSelections(SearchRequest, SearchArguments)}
	 * when there are facet arguments in the {@link SearchRequest} and are correctly provided, but there is not a
	 * {@link Facet} for the specific argument.
	 */
	@Test
	public void testFacetSelectionPreservingWithRdfTypeFacet() {
		Facet facet = new Facet();
		facet.setId("myFacet");

		Map<String, Facet> facets = new HashMap<>();
		facets.put("myFacet", facet);

		SearchableProperty searchableProperty1 = getSearchableProperty("rdfType");
		mockSearchablePropertiesService(Arrays.asList(searchableProperty1));

		SearchRequest searchRequest = new SearchRequest(new MultivaluedHashMap<>());
		searchRequest.add(FacetQueryParameters.REQUEST_FACET_ARGUMENTS,
				"rdfType" + FacetQueryParameters.ARGUMENT_SEPARATOR + "myValue");

		Mockito
				.when(searchablePropertiesService.getSearchableProperty(Matchers.any(), Matchers.eq("rdfType")))
					.thenReturn(Optional.ofNullable(null));

		SearchArguments<Instance> searchArguments = new SearchArguments<>();
		searchArguments.setFacets(facets);

		argumentTransformer.preserveFacetSelections(searchRequest, searchArguments);

		Facet myFacet = searchArguments.getFacetsWithSelectedValues().get(0);
		Assert.assertEquals(myFacet.getSelectedValues().iterator().next(), "myValue");
	}

	/**
	 * Tests the logic inside {@link FacetArgumentTransformer#preserveFacetSelections(SearchRequest, SearchArguments)}
	 * when there are facet arguments in the {@link SearchRequest} and are correctly provided and there are
	 * {@link Facet}s for them.
	 */
	@Test
	public void testFacetSelectionPreserving() {
		Facet facet = new Facet();
		facet.setId("myFacet");

		Map<String, Facet> facets = new HashMap<>();
		facets.put("myFacet", facet);

		SearchRequest searchRequest = new SearchRequest(new MultivaluedHashMap<>());
		searchRequest.add(FacetQueryParameters.REQUEST_FACET_ARGUMENTS,
				"myFacet" + FacetQueryParameters.ARGUMENT_SEPARATOR + "myValue");

		SearchArguments<Instance> searchArguments = new SearchArguments<>();
		searchArguments.setFacets(facets);

		argumentTransformer.preserveFacetSelections(searchRequest, searchArguments);

		Facet myFacet = searchArguments.getFacets().get("myFacet");
		Assert.assertNotNull(myFacet.getSelectedValues());
		Assert.assertEquals(myFacet.getSelectedValues().size(), 1);
		Assert.assertEquals(myFacet.getSelectedValues().iterator().next(), "myValue");
	}

	/**
	 * Tests the logic inside
	 * {@link FacetArgumentTransformer#mergeAdditionalFilterQueries(SearchRequest, SearchArguments)} when there are not
	 * additional filters.
	 */
	@Test
	public void testAdditionalFiltersMergingWithoutFilters() {
		SearchRequest searchRequest = new SearchRequest(new MultivaluedHashMap<>());
		SearchArguments<Instance> searchArguments = new SearchArguments<>();

		argumentTransformer.mergeAdditionalFilterQueries(searchRequest, searchArguments);

		Assert.assertEquals(searchArguments.getArguments().size(), 0);
	}

	/**
	 * Tests the logic inside
	 * {@link FacetArgumentTransformer#mergeAdditionalFilterQueries(SearchRequest, SearchArguments)} when the additional
	 * filters are provided <b>IN</b>correctly.
	 */
	@Test
	public void testAdditionalFiltersMergingWithoutCorrectFilters() {
		SearchRequest searchRequest = new SearchRequest(new MultivaluedHashMap<>());
		SearchArguments<Instance> searchArguments = new SearchArguments<>();

		searchRequest.add("fq[]", "uno");
		searchRequest.add("fq[]", "does");

		argumentTransformer.mergeAdditionalFilterQueries(searchRequest, searchArguments);

		Assert.assertEquals(searchArguments.getArguments().size(), 0);
	}

	/**
	 * Tests the logic inside
	 * {@link FacetArgumentTransformer#mergeAdditionalFilterQueries(SearchRequest, SearchArguments)} when the additional
	 * filters are provided correctly.
	 */
	@Test
	public void testAdditionalFiltersMerging() {
		SearchRequest searchRequest = new SearchRequest(new MultivaluedHashMap<>());
		SearchArguments<Instance> searchArguments = new SearchArguments<>();

		char separator = FacetQueryParameters.ARGUMENT_SEPARATOR;
		searchRequest.add("fq[]", "uno" + separator + "one");
		searchRequest.add("fq[]", "does" + separator + "two");

		argumentTransformer.mergeAdditionalFilterQueries(searchRequest, searchArguments);

		Assert.assertEquals(searchArguments.getArguments().size(), 1);

		Serializable serializable = searchArguments.getArguments().get(CommonParams.FQ);
		Assert.assertEquals(serializable.toString(), "uno" + separator + "one AND does" + separator + "two");
	}

	/**
	 * Tests the logic inside
	 * {@link FacetArgumentTransformer#mergeAdditionalFilterQueries(SearchRequest, SearchArguments)} when the additional
	 * filters are provided correctly and there are existing filters in the search arguments.
	 */
	@Test
	public void testAdditionalFiltersMergingWithExistingFilters() {
		SearchRequest searchRequest = new SearchRequest(new MultivaluedHashMap<>());
		SearchArguments<Instance> searchArguments = new SearchArguments<>();

		searchArguments.getArguments().put(CommonParams.FQ, "two");

		char separator = FacetQueryParameters.ARGUMENT_SEPARATOR;
		searchRequest.add("fq[]", "uno" + separator + "one");

		argumentTransformer.mergeAdditionalFilterQueries(searchRequest, searchArguments);

		Assert.assertEquals(searchArguments.getArguments().size(), 1);

		Serializable serializable = searchArguments.getArguments().get(CommonParams.FQ);
		Assert.assertEquals(serializable.toString(), "two AND uno" + separator + "one");
	}

	/**
	 * Tests the logic in {@link FacetArgumentTransformerImpl#prepareDateArguments(SearchRequest, SearchArguments)} when
	 * there are <b>NOT</b> predefined date arguments.
	 */
	@Test
	public void testDateArgumentsPreparingWithoutInitialArguments() {
		ConfigurationProperty<String> startDateConfig = new ConfigurationPropertyMock<>("start");
		Mockito.when(configuration.getDateStart()).thenReturn(startDateConfig);
		ConfigurationProperty<String> endDateConfig = new ConfigurationPropertyMock<>("end");
		Mockito.when(configuration.getDateEnd()).thenReturn(endDateConfig);
		ConfigurationProperty<String> dateGapConfig = new ConfigurationPropertyMock<>("gap");
		Mockito.when(configuration.getDateGap()).thenReturn(dateGapConfig);
		ConfigurationProperty<String> otherDateConfig = new ConfigurationPropertyMock<>("other");
		Mockito.when(configuration.getOther()).thenReturn(otherDateConfig);

		SearchRequest searchRequest = new SearchRequest(new MultivaluedHashMap<>());
		SearchArguments<Instance> searchArguments = new SearchArguments<>();

		argumentTransformer.prepareDateArguments(searchRequest, searchArguments);

		Map<String, Serializable> facetArguments = searchArguments.getFacetArguments();

		Assert.assertNotNull(facetArguments);
		Assert.assertEquals(facetArguments.size(), 4);

		Assert.assertTrue(facetArguments.containsKey(FacetParams.FACET_DATE_START));
		Assert.assertTrue(facetArguments.containsValue("start"));

		Assert.assertTrue(facetArguments.containsKey(FacetParams.FACET_DATE_END));
		Assert.assertTrue(facetArguments.containsValue("end"));

		Assert.assertTrue(facetArguments.containsKey(FacetParams.FACET_DATE_GAP));
		Assert.assertTrue(facetArguments.containsValue("gap"));

		Assert.assertTrue(facetArguments.containsKey(FacetParams.FACET_DATE_OTHER));
		Assert.assertTrue(facetArguments.containsValue("other"));
	}

	/**
	 * Tests the logic in {@link FacetArgumentTransformerImpl#prepareDateArguments(SearchRequest, SearchArguments)} when
	 * there are predefined date arguments.
	 */
	@Test
	public void testDateArgumentsPreparingWithInitialArguments() {
		SearchRequest searchRequest = new SearchRequest(new MultivaluedHashMap<>());
		SearchArguments<Instance> searchArguments = new SearchArguments<>();

		searchRequest.add(FacetParams.FACET_DATE_START, "the start");
		searchRequest.add(FacetParams.FACET_DATE_END, "the end");
		searchRequest.add(FacetParams.FACET_DATE_GAP, "the gap");
		searchRequest.add(FacetParams.FACET_DATE_OTHER, "the other");

		argumentTransformer.prepareDateArguments(searchRequest, searchArguments);

		Map<String, Serializable> facetArguments = searchArguments.getFacetArguments();

		Assert.assertNotNull(facetArguments);
		Assert.assertEquals(facetArguments.size(), 4);

		Assert.assertTrue(facetArguments.containsKey(FacetParams.FACET_DATE_START));
		Assert.assertTrue(facetArguments.containsValue("the start"));

		Assert.assertTrue(facetArguments.containsKey(FacetParams.FACET_DATE_END));
		Assert.assertTrue(facetArguments.containsValue("the end"));

		Assert.assertTrue(facetArguments.containsKey(FacetParams.FACET_DATE_GAP));
		Assert.assertTrue(facetArguments.containsValue("the gap"));

		Assert.assertTrue(facetArguments.containsKey(FacetParams.FACET_DATE_OTHER));
		Assert.assertTrue(facetArguments.containsValue("the other"));
	}

	/**
	 * Creates hollow {@link SearchableProperty} with the provided ID as property ID.
	 *
	 * @param id
	 *            - the provided ID
	 * @return the hollow property
	 */
	// TODO: Find all those methods and move them to util class
	private SearchableProperty getSearchableProperty(String id) {
		SearchableProperty prop = new SearchableProperty();
		prop.setId(id);
		prop.setLabelId(prop::getId);
		prop.setLabelProvider(Function.identity());
		return prop;
	}

	/**
	 * Mocks the {@link SearchablePropertiesService#getSearchableSolrProperties(String, Boolean, Boolean, Boolean)} to
	 * return the provided list of searchable properties.
	 *
	 * @param properties
	 *            - the provided properties
	 */
	private void mockSearchablePropertiesService(List<SearchableProperty> properties) {
		Mockito.when(searchablePropertiesService.getSearchableSolrProperties(any(), any(), any(), any())).thenReturn(
				properties);

		for (SearchableProperty property : properties) {
			Mockito
					.when(searchablePropertiesService.getSearchableProperty(Matchers.anyString(),
							Matchers.eq(property.getId())))
						.thenReturn(Optional.of(property));
		}
	}

	/**
	 * Mocks the {@link FacetConfigurationProvider#getFacetConfigField(String)} to return an empty configuration for the
	 * specific ID.
	 *
	 * @param id
	 *            - the specific ID
	 */
	private void mockFacetConfigurationProvider(String id) {
		Mockito.when(facetConfigurationProvider.getFacetConfigField(id)).thenReturn(new FacetConfiguration());
	}

}
