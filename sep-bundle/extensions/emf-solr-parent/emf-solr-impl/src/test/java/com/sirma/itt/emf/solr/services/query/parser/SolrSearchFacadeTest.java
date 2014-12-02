package com.sirma.itt.emf.solr.services.query.parser;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import javax.ws.rs.core.MultivaluedMap;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.search.SearchDialects;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.Sorter;
import com.sirma.itt.emf.solr.constants.SolrQueryConstants;
import com.sirma.itt.emf.time.ISO8601DateFormat;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Tests the mocked implementation of {@link SolrSearchFacade}.
 *
 * @author bbanchev
 */
public class SolrSearchFacadeTest {

	/** The solr search facade. */
	private static SolrSearchFacade solrSearchFacade;

	/**
	 * Sets up the mocks.s
	 */
	@BeforeClass
	public void setUp() {
		solrSearchFacade = new SolrSearchFacade();
		NamespaceRegistryService namespaceRegistryService = Mockito
				.mock(NamespaceRegistryService.class);
		// mock the namespaceRegistryService
		Mockito.when(namespaceRegistryService.buildFullUri(Mockito.same("emf:idshort")))
				.thenReturn(
						"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#idshort");
		Mockito.when(
				namespaceRegistryService.buildFullUri(Mockito
						.same("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#idfull")))
				.thenReturn(
						"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#idfull");

		Mockito.when(namespaceRegistryService.buildFullUri(Mockito.same("emf:admin"))).thenReturn(
				"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#admin");
		Mockito.when(namespaceRegistryService.buildFullUri(Mockito.same("emf:banchev")))
				.thenReturn(
						"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#banchev");
		Mockito.when(namespaceRegistryService.buildFullUri(Mockito.same("emf:CultObject")))
				.thenReturn(
						"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#CultObject");

		// mock the req fields
		ReflectionUtils.setField(solrSearchFacade, "fqBasicSearch", "compact_header:*");
		ReflectionUtils.setField(solrSearchFacade, "namespaceRegistryService",
				namespaceRegistryService);
		ReflectionUtils.setField(solrSearchFacade, "defaultFields", "uri,instanceType");
		ReflectionUtils.setField(solrSearchFacade, "converterDateFormatPattern", "dd.MM.yyyy");

		solrSearchFacade.onStartup(null);
	}

	/**
	 * Test pagination.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testPagination() {
		SearchArguments<Instance> searchArgs = new SearchArguments<>();

		MultivaluedMap<String, String> queryParams = Mockito.mock(MultivaluedMap.class);
		makeQuery(solrSearchFacade, searchArgs, queryParams);
		Assert.assertEquals(searchArgs.getStringQuery(), SolrQueryConstants.QUERY_DEFAULT_ALL);
		Assert.assertEquals(searchArgs.getPageNumber(), 0);
		Mockito.when(queryParams.getFirst(Mockito.same("pageNumber"))).thenReturn("100");
		searchArgs = new SearchArguments<>();
		makeQuery(solrSearchFacade, searchArgs, queryParams);
		Assert.assertEquals(searchArgs.getPageNumber(), 100);
		Mockito.when(queryParams.getFirst(Mockito.same("pageSize"))).thenReturn("101");
		searchArgs = new SearchArguments<>();
		makeQuery(solrSearchFacade, searchArgs, queryParams);
		Assert.assertEquals(searchArgs.getPageNumber(), 100);
		Assert.assertEquals(searchArgs.getPageSize(), 101);
		Assert.assertEquals(searchArgs.getMaxSize(), 101);

	}

	/**
	 * Test query build.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testQueryBuild() {
		MultivaluedMap<String, String> queryParams = Mockito.mock(MultivaluedMap.class);
		checkQueryBuild(queryParams, queryParams.get(Mockito.any()), null,
				SolrQueryConstants.QUERY_DEFAULT_ALL);

		List<String> locations = new LinkedList<>();
		locations.add("emf:idshort");
		locations.add("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#idfull");
		String queryLoc = "partOfRelation:( \"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#idshort\" OR \"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#idfull\" )";
		queryParams = Mockito.mock(MultivaluedMap.class);
		checkQueryBuild(queryParams, queryParams.get(Mockito.same("location[]")), locations,
				queryLoc);

		queryParams = Mockito.mock(MultivaluedMap.class);
		checkQueryBuild(queryParams, queryParams.getFirst(Mockito.same("mimetype")), "image/png",
				"mimetype:\"image/png\"");
		queryParams = Mockito.mock(MultivaluedMap.class);
		checkQueryBuild(queryParams, queryParams.getFirst(Mockito.same("mimetype")), "^image/png",
				"mimetype:\"*image/png*\"");
		queryParams = Mockito.mock(MultivaluedMap.class);
		checkQueryBuild(queryParams, queryParams.getFirst(Mockito.same("identifier")),
				"emf:idshort",
				"identifier:\"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#idshort\"");
		queryParams = Mockito.mock(MultivaluedMap.class);
		checkQueryBuild(queryParams, queryParams.getFirst(Mockito.same("identifier")),
				"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#idfull",
				"identifier:\"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#idfull\"");

		queryParams = Mockito.mock(MultivaluedMap.class);

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
		checkQueryBuild(queryParams, queryParams.getFirst(Mockito.same("createdFromDate")),
				"12.12.2014", "createdOn:[" + ISO8601DateFormat.format(calFrom) + " TO *]");

		queryParams = Mockito.mock(MultivaluedMap.class);
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

		checkQueryBuild(queryParams, queryParams.getFirst(Mockito.same("createdToDate")),
				"12.12.2014", "createdOn:[* TO " + ISO8601DateFormat.format(calTo) + "]");
		// already have the createdToDate
		checkQueryBuild(queryParams, queryParams.getFirst(Mockito.same("createdFromDate")),
				"12.12.2014", "createdOn:[" + ISO8601DateFormat.format(calFrom) + " TO "
						+ ISO8601DateFormat.format(calTo) + "]");

		// check with diff formatter
		ReflectionUtils.setField(solrSearchFacade, "converterDateFormatPattern", "yy.dd.MM");
		queryParams = Mockito.mock(MultivaluedMap.class);
		checkQueryBuild(queryParams, queryParams.getFirst(Mockito.same("createdFromDate")),
				"14.12.12", "createdOn:[" + ISO8601DateFormat.format(calFrom) + " TO *]");
		ReflectionUtils.setField(solrSearchFacade, "converterDateFormatPattern", "dd.MM.yyyy");

		// check creator
		queryParams = Mockito.mock(MultivaluedMap.class);
		List<String> users = new LinkedList<>();
		users.add("emf:admin");
		users.add("emf:banchev");

		checkQueryBuild(
				queryParams,
				queryParams.get(Mockito.same("createdBy[]")),
				users,
				"createdBy:( \"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#admin\" OR \"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#banchev\" )");

		queryParams = Mockito.mock(MultivaluedMap.class);
		List<String> types = new LinkedList<>();
		types.add("OT123232");
		types.add("emf:CultObject");
		checkQueryBuild(
				queryParams,
				queryParams.get(Mockito.same("subType[]")),
				types,
				"type:( \"OT123232\" ) AND rdfType:( \"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#CultObject\" )");

	}

	/**
	 * Check query build.
	 *
	 * @param queryParams
	 *            the query params
	 * @param when
	 *            the when
	 * @param then
	 *            the then
	 * @param queryAssert
	 *            the query assert
	 */
	private void checkQueryBuild(MultivaluedMap<String, String> queryParams, Object when,
			Object then, String queryAssert) {

		SearchArguments<Instance> searchArgs = new SearchArguments<>();
		Mockito.when(when).thenReturn(then);
		makeQuery(solrSearchFacade, searchArgs, queryParams);
		Assert.assertEquals(searchArgs.getStringQuery(), queryAssert);
	}

	/**
	 * Test order.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testOrder() {
		MultivaluedMap<String, String> queryParams = Mockito.mock(MultivaluedMap.class);
		SearchArguments<Instance> searchArgs;
		Mockito.when(queryParams.getFirst(Mockito.same("orderBy"))).thenReturn("dcterm:title");
		searchArgs = new SearchArguments<>();
		makeQuery(solrSearchFacade, searchArgs, queryParams);
		Assert.assertEquals(searchArgs.getSorter(), new Sorter("_sort_title", "asc"));

		Mockito.when(queryParams.getFirst(Mockito.same("orderBy"))).thenReturn("emf:modifiedOn");
		searchArgs = new SearchArguments<>();
		makeQuery(solrSearchFacade, searchArgs, queryParams);
		Assert.assertEquals(searchArgs.getSorter(), new Sorter("_sort_modifiedOn", "asc"));

		Mockito.when(queryParams.getFirst(Mockito.same("orderBy"))).thenReturn("emf:modifiedBy");
		Mockito.when(queryParams.getFirst(Mockito.same("orderDirection"))).thenReturn("desc");
		searchArgs = new SearchArguments<>();
		makeQuery(solrSearchFacade, searchArgs, queryParams);
		Assert.assertEquals(searchArgs.getSorter(), new Sorter("_sort_modifiedBy", "desc"));
	}

	/**
	 * Make query.
	 *
	 * @param solrSearchFacade
	 *            the solr search facade
	 * @param searchArgs
	 *            the search args
	 * @param queryParams
	 *            the query params
	 */
	private void makeQuery(SolrSearchFacade solrSearchFacade, SearchArguments<Instance> searchArgs,
			MultivaluedMap<String, String> queryParams) {
		try {
			solrSearchFacade.prepareBasicQuery(queryParams, searchArgs);
			Assert.assertEquals(searchArgs.getDialect(), SearchDialects.SOLR);
			Assert.assertNotNull(searchArgs.getStringQuery(), "Should have query");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
