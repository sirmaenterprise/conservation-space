package com.sirma.itt.emf.solr.services.query.parser;

import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.FacetParams;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.sirma.itt.emf.solr.configuration.SolrSearchConfiguration;
import com.sirma.itt.emf.solr.constants.SolrQueryConstants;
import com.sirma.itt.emf.solr.exception.SolrClientException;
import com.sirma.itt.emf.solr.services.SolrConnector;
import com.sirma.itt.emf.solr.services.impl.SolrSearchEngine;
import com.sirma.itt.emf.solr.services.impl.facet.FacetResultTransformer;
import com.sirma.itt.emf.solr.services.impl.facet.FacetSolrHelper;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.collections.ContextualMap;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.Query;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchDialects;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.facet.Facet;
import com.sirma.itt.seip.domain.search.facet.FacetValue;
import com.sirma.itt.seip.domain.util.DateConverter;
import com.sirma.itt.seip.domain.util.DateConverterImpl;
import com.sirma.itt.seip.search.SearchEngine;
import com.sirma.itt.seip.security.UserPreferences;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.time.ISO8601DateFormat;
import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Tests the mocked implementation of {@link SolrSearchFacade}.
 *
 * @author bbanchev
 */
@Test
public class SolrSearchFacadeTest {

	@Mock
	private SolrConnector solrConnector;

	@Spy
	FacetSolrHelper facetSolrHelper;

	@Mock
	private NamespaceRegistryService namespaceRegistryService;

	@Mock
	SolrSearchConfiguration searchConfiguration;

	/** The solr search facade. */
	@InjectMocks
	SolrSearchEngine solrSearchFacade = new SolrSearchEngine();

	@Mock
	private UserPreferences userPreferences;

	@Spy
	FacetResultTransformer facetResultTransformer;

	@Spy
	DateConverter dateConverter = new DateConverterImpl();

	/**
	 * Sets up the mocks.
	 */
	@BeforeClass
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		// mock the namespaceRegistryService
		Mockito.when(namespaceRegistryService.buildFullUri(Matchers.same("emf:idshort"))).thenReturn(
				"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#idshort");
		Mockito
				.when(namespaceRegistryService.buildFullUri(
						Matchers.same("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#idfull")))
					.thenReturn("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#idfull");

		Mockito.when(namespaceRegistryService.buildFullUri(Matchers.same("emf:admin"))).thenReturn(
				"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#admin");
		Mockito.when(namespaceRegistryService.buildFullUri(Matchers.same("emf:banchev"))).thenReturn(
				"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#banchev");
		Mockito.when(namespaceRegistryService.buildFullUri(Matchers.same("emf:CultObject"))).thenReturn(
				"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#CultObject");

		// mock the req fields
		when(searchConfiguration.getDashletsRequestFields())
				.thenReturn(new ConfigurationPropertyMock<>("uri,instanceType"));
		when(searchConfiguration.getFullTextSearchFilterQuery())
				.thenReturn(new ConfigurationPropertyMock<>("compact_header:*"));
		ReflectionUtils.setFieldValue(solrSearchFacade, "namespaceRegistryService", namespaceRegistryService);

		Mockito.when(userPreferences.getLanguage()).thenReturn("en");
		ReflectionUtils.setFieldValue(dateConverter, "userPreferences", userPreferences);
		ReflectionUtils.setFieldValue(dateConverter, "dateFormats", ContextualMap.create());
		ReflectionUtils.setFieldValue(dateConverter, "converterDateFormatPattern",
				new ConfigurationPropertyMock<>("dd.MM.yyyy"));
		ReflectionUtils.setFieldValue(dateConverter, "converterDatetimeFormatPattern",
				new ConfigurationPropertyMock<>("dd.MM.yyyy, HH:mm"));
	}

	/**
	 * Test pagination.
	 */
	@Test
	public void testStringQuery() {
		SearchArguments<Instance> searchArgs = new SearchArguments<>();
		searchArgs.setDialect(SearchDialects.SOLR);

		SearchRequest request = new SearchRequest(new HashMap<>());
		request.setDialect(SearchDialects.SOLR);

		makeQuery(solrSearchFacade, searchArgs, request);

		Assert.assertEquals(searchArgs.getStringQuery(), SolrQueryConstants.QUERY_DEFAULT_ALL);
	}

	/**
	 * Test query build.
	 */
	@Test
	public void testQueryBuild() {
		SearchRequest request = new SearchRequest(new HashMap<String, List<String>>());
		request.setDialect(SearchDialects.SOLR);
		checkQueryBuild(request, SolrQueryConstants.QUERY_DEFAULT_ALL);

		List<String> locations = new LinkedList<>();
		locations.add("emf:idshort");

		request.getRequest().put("mimetype", Collections.singletonList("image/png"));
		checkQueryBuild(request, "mimetype:\"image/png\"");
		request.getRequest().put("mimetype", Collections.singletonList("^image/png"));
		checkQueryBuild(request, "mimetype:\"*image/png*\"");

		request.getRequest().clear();
		request.getRequest().put("identifier", Collections.singletonList("emf:idshort"));
		checkQueryBuild(request,
				"identifier:\"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#idshort\"");

		request.getRequest().put("identifier",
				Collections.singletonList("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#idfull"));
		checkQueryBuild(request,
				"identifier:\"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#idfull\"");

		Calendar calFrom = Calendar.getInstance();
		calFrom.set(Calendar.YEAR, 2014);
		calFrom.set(Calendar.MONTH, 11);
		calFrom.set(Calendar.DAY_OF_MONTH, 12);
		calFrom.set(Calendar.MINUTE, 0);
		calFrom.set(Calendar.HOUR_OF_DAY, 0);
		calFrom.set(Calendar.SECOND, 0);
		calFrom.set(Calendar.MILLISECOND, 0);
		// get the time, than make the offset for the server
		long timeInMillis = calFrom.getTimeInMillis();
		calFrom.setTimeZone(TimeZone.getTimeZone("UTC"));
		calFrom.setTimeInMillis(timeInMillis);

		request.getRequest().clear();
		request.getRequest().put("createdFromDate", Collections.singletonList("12.12.2014"));
		checkQueryBuild(request, "createdOn:[" + ISO8601DateFormat.format(calFrom) + " TO *]");

		Calendar calTo = Calendar.getInstance();
		// set to 23:59:59
		calTo.set(Calendar.YEAR, 2014);
		calTo.set(Calendar.MONTH, 11);
		calTo.set(Calendar.DATE, 12);
		calTo.set(Calendar.MINUTE, 59);
		calTo.set(Calendar.HOUR_OF_DAY, 23);
		calTo.set(Calendar.SECOND, 59);
		calTo.set(Calendar.MILLISECOND, 0);
		// get the time, than make the offset for the server
		timeInMillis = calTo.getTimeInMillis();
		calTo.setTimeInMillis(timeInMillis);
		calTo.setTimeZone(TimeZone.getTimeZone("UTC"));

		request.getRequest().clear();
		request.getRequest().put("createdToDate", Collections.singletonList("12.12.2014"));
		checkQueryBuild(request, "createdOn:[* TO " + ISO8601DateFormat.format(calTo) + "]");
		// already have the createdToDate
		request.getRequest().put("createdFromDate", Collections.singletonList("12.12.2014"));
		checkQueryBuild(request,
				"createdOn:[" + ISO8601DateFormat.format(calFrom) + " TO " + ISO8601DateFormat.format(calTo) + "]");

		// check with diff formatter
		ReflectionUtils.setFieldValue(dateConverter, "converterDateFormatPattern",
				new ConfigurationPropertyMock<>("yy.dd.MM"));
		ReflectionUtils.setFieldValue(dateConverter, "dateFormats", ContextualMap.create());

		request.getRequest().clear();
		request.getRequest().put("createdFromDate", Collections.singletonList("14.12.12"));
		checkQueryBuild(request, "createdOn:[" + ISO8601DateFormat.format(calFrom) + " TO *]");
		ReflectionUtils.setFieldValue(dateConverter, "converterDateFormatPattern",
				new ConfigurationPropertyMock<>("dd.MM.yyyy"));

		// check creator

		List<String> users = new LinkedList<>();
		users.add("emf:admin");
		users.add("emf:banchev");

		request.getRequest().clear();
		request.getRequest().put("createdBy[]", users);
		checkQueryBuild(request,
				"createdBy:( \"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#admin\" OR \"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#banchev\" )");

		List<String> types = new LinkedList<>();
		types.add("OT123232");
		types.add("emf:CultObject");
		request.getRequest().clear();
		request.getRequest().put("subType[]", types);
		checkQueryBuild(request,
				"( type:( \"OT123232\" ) OR rdfType:( \"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#CultObject\" ) )");
	}

	/**
	 * Check query build.
	 *
	 * @param request
	 *            the request
	 * @param queryAssert
	 *            the query assert
	 */
	private void checkQueryBuild(SearchRequest request, String queryAssert) {
		SearchArguments<Instance> searchArgs = new SearchArguments<>();
		searchArgs.setDialect(SearchDialects.SOLR);
		makeQuery(solrSearchFacade, searchArgs, request);
		Assert.assertEquals(searchArgs.getStringQuery(), queryAssert);
	}

	/**
	 * Make query.
	 *
	 * @param solrSearchEngine
	 *            the solr search facade
	 * @param searchArgs
	 *            the search args
	 * @param queryParams
	 *            the query params
	 */
	private void makeQuery(SearchEngine solrSearchEngine, SearchArguments<Instance> searchArgs,
			SearchRequest queryParams) {
		try {
			solrSearchEngine.prepareSearchArguments(queryParams, searchArgs);
			Assert.assertEquals(searchArgs.getDialect(), SearchDialects.SOLR);
			Assert.assertNotNull(searchArgs.getStringQuery(), "Should have query");
		} catch (Exception e) {
			Assert.fail("Query building failed: " + e.getMessage(), e);
		}
	}

	/**
	 * Tests the Solr query building when the faceting is disabled.
	 */
	@Test
	public void testFacetsQueryBuildingWithoutFaceting() {
		SearchArguments<Instance> searchArgs = new SearchArguments<>();
		searchArgs.setFaceted(false);
		searchArgs.setQuery(new Query("", ""));

		solrSearchFacade.search(Instance.class, searchArgs);

		ArgumentCaptor<SolrQuery> capture = getSolrConnectorCaptor();
		SolrQuery capturedQuery = capture.getValue();

		Assert.assertNull(capturedQuery.get(FacetParams.FACET), "Faceting should not be enabled!");
	}

	/**
	 * Tests the Solr query building when the faceting is enabled.
	 */
	@Test
	public void testFacetsQueryBuildingWithFaceting() {
		SearchArguments<Instance> searchArgs = new SearchArguments<>();
		searchArgs.setFaceted(true);
		searchArgs.setQuery(new Query("", ""));
		searchArgs.setFacets(new HashMap<String, Facet>(0));
		searchArgs.setFacetArguments(CollectionUtils.<String, Serializable> createLinkedHashMap(1));
		searchArgs.getFacetArguments().put(FacetParams.FACET_THREADS, "1");

		solrSearchFacade.search(Instance.class, searchArgs);

		ArgumentCaptor<SolrQuery> capture = getSolrConnectorCaptor();
		SolrQuery capturedQuery = capture.getValue();

		Assert.assertEquals(capturedQuery.get(FacetParams.FACET), "true", "Faceting should be enabled!");
		Assert.assertEquals(capturedQuery.getFacetMinCount(), 1, "Faceting should have min count of 1!");
		Assert.assertEquals(capturedQuery.get(FacetParams.FACET_MISSING), "true",
				"Faceting should include missing values!");
		Assert.assertEquals(capturedQuery.get(FacetParams.FACET_THREADS), "1",
				"Faceting should include the provided facet arguments!");
	}

	/**
	 * Tests the Solr query building when the faceting is enabled and there are facets with selections.
	 */
	@Test(enabled = false)
	public void testFacetsQueryBuildingWithSelections() {
		SearchArguments<Instance> searchArgs = new SearchArguments<>();
		searchArgs.setFaceted(true);
		searchArgs.setQuery(new Query("", ""));
		searchArgs.setFacets(new HashMap<String, Facet>(1));

		searchArgs.getFacets().put("zer0", generateTestFacet("zer0", true, CollectionUtils.<String> emptyList()));
		searchArgs.getFacets().put("one",
				generateTestFacet("one", false, Arrays.asList("kekeke", "nanana", "NO_VALUE")));
		searchArgs.getFacets().put("two", generateTestFacet("two", true, Arrays.asList("*;*")));
		searchArgs.getFacets().put("three",
				generateTestFacet("three", true, Arrays.asList("2010-01-01T12:00:00.000Z;2020-01-01T12:34:56.999Z")));

		solrSearchFacade.search(Instance.class, searchArgs);

		ArgumentCaptor<SolrQuery> capture = getSolrConnectorCaptor();
		SolrQuery capturedQuery = capture.getValue();

		String[] fq = capturedQuery.getFilterQueries();
		Assert.assertFalse(filterQueriesContains(fq, "zer0"));
		Assert.assertTrue(filterQueriesContains(fq, "one:(kekeke nanana)"));
		Assert.assertTrue(filterQueriesContains(fq, "-one:*"));
		Assert.assertTrue(filterQueriesContains(fq, "two:[* TO *]"));
		Assert.assertTrue(
				filterQueriesContains(fq, "three:[\"2010-01-01T12:00:00.000Z\" TO \"2020-01-01T12:34:56.999Z\"]"));
	}

	private boolean filterQueriesContains(String[] queries, String param) {
		for (String query : queries) {
			if (query.contains(param)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Tests the parsing of the facet fields in the Solr response when the faceting is enabled.
	 */
	@Test
	public void testFacetsResponseParsingWithFaceting() {
		SearchArguments<Instance> searchArgs = new SearchArguments<>();
		searchArgs.setFaceted(true);
		searchArgs.setQuery(new Query("", ""));
		searchArgs.setFacets(new HashMap<String, Facet>(3));
		searchArgs.getFacets().put("one", generateTestFacet("one", false, CollectionUtils.<String> emptyList()));
		searchArgs.getFacets().put("two", generateTestFacet("two", false, CollectionUtils.<String> emptyList()));
		searchArgs.getFacets().put("date1", generateTestFacet("date1", true, CollectionUtils.<String> emptyList()));

		// Mocking the response
		QueryResponse queryResponse = Mockito.mock(QueryResponse.class);
		Mockito.when(queryResponse.getFacetFields()).thenReturn(Arrays.asList(generateFacetField("one", 2, true, 5)));
		Mockito.when(queryResponse.getFacetDates()).thenReturn(Arrays.asList(generateFacetField("date1", 2, false, 0)));
		// Avoiding NPE
		Mockito.when(queryResponse.getResults()).thenReturn(new SolrDocumentList());
		try {
			Mockito.when(solrConnector.queryWithPost(Matchers.any(SolrQuery.class))).thenReturn(queryResponse);
		} catch (SolrClientException e) {
			Assert.fail(e.getMessage());
		}

		solrSearchFacade.search(Instance.class, searchArgs);

		Map<String, Facet> facets = searchArgs.getFacets();
		Assert.assertTrue(facets.size() == 3, "The facets map size should not be changed!");

		Facet facetOne = facets.get("one");
		Assert.assertNotNull(facetOne.getValues());
		Assert.assertTrue(facetOne.getValues().size() == 3);
		Assert.assertTrue(checkForFacetField(facetOne, "NO_VALUE", 5));
		Assert.assertTrue(checkForFacetField(facetOne, "count-1", 1));
		Assert.assertTrue(checkForFacetField(facetOne, "count-2", 2));

		Facet facetDate = facets.get("date1");
		Assert.assertNotNull(facetDate.getValues());
		Assert.assertTrue(facetDate.getValues().size() == 2);
		Assert.assertTrue(checkForFacetField(facetDate, "count-1", 1));
		Assert.assertTrue(checkForFacetField(facetDate, "count-2", 2));
	}

	/**
	 * Checks in the values of the provided facet for existing value that matches the given conditions.
	 *
	 * @param facet
	 *            - the provided facet
	 * @param name
	 *            - the value name to match
	 * @param count
	 *            - the value count to match
	 * @return
	 */
	private boolean checkForFacetField(Facet facet, String name, long count) {
		for (FacetValue value : facet.getValues()) {
			if (name.equals(value.getId()) && count == value.getCount()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Generates simple test facet with the provided values.
	 *
	 * @param id
	 *            - the id of the facet, also sets the solrType
	 * @param date
	 *            - if it is a date or not
	 * @param selection
	 *            - the facet's selection
	 * @return the generated facet
	 */
	private Facet generateTestFacet(String id, boolean date, List<String> selection) {
		Facet facet = new Facet();
		facet.setId(id);
		facet.setSolrFieldName(id);
		if (date) {
			facet.setSolrType("tdates");
		}
		facet.setSelectedValues(new HashSet<>(selection));
		return facet;
	}

	/**
	 * Generates a Solr's facet field with the provided information.
	 *
	 * @param name
	 *            - the field's name
	 * @param counts
	 *            - how many values to have the field
	 * @param missing
	 *            - should the field include a missing value
	 * @param missingCount
	 *            - how missing counts will have the missing value
	 * @return
	 */
	private FacetField generateFacetField(String name, int counts, boolean missing, int missingCount) {
		FacetField field = new FacetField(name);
		if (missing) {
			// the missing count
			field.add(null, missingCount);
		}
		for (int i = 1; i < counts + 1; i++) {
			field.add("count-" + i, i);
		}
		return field;
	}

	/**
	 * Creates an argument captor on {@link SolrConnector#queryWithPost(SolrQuery)} and returns the captor. Used for
	 * checking what was passed as an argument.
	 *
	 * @return the captor
	 */
	private ArgumentCaptor<SolrQuery> getSolrConnectorCaptor() {
		ArgumentCaptor<SolrQuery> solrParameters = ArgumentCaptor.forClass(SolrQuery.class);
		try {
			Mockito.verify(solrConnector, Mockito.atLeastOnce()).queryWithPost(solrParameters.capture());
		} catch (SolrClientException e) {
			Assert.fail(e.getMessage());
		}
		return solrParameters;
	}
}
