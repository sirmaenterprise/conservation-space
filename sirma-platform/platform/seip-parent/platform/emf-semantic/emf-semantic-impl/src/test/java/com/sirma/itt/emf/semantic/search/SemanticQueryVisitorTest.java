package com.sirma.itt.emf.semantic.search;

import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.CONNECTOR_NAME_CONSTANT;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.LINE_SEPARATOR;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.mockito.Matchers;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sirma.itt.emf.GeneralSemanticTest;
import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.search.Query;
import com.sirma.itt.seip.domain.search.Query.QueryBoost;
import com.sirma.itt.seip.time.DateRange;
import com.sirma.itt.seip.time.ISO8601DateFormat;
import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.itt.semantic.search.FTSQueryParser;

/**
 * Tests the {@link SemanticQueryVisitor} class.
 *
 * @author nvelkov
 */
public class SemanticQueryVisitorTest {

	private SemanticQueryVisitor semanticQueryVisitor;

	@BeforeMethod
	public void initTest() {
		semanticQueryVisitor = new SemanticQueryVisitor();
		FTSQueryParser parser = Mockito.mock(FTSQueryParser.class);
		Mockito.when(parser.prepare(Matchers.anyString())).then(invocation -> {
			String input = invocation.getArgumentAt(0, String.class);
			return SPARQLQueryHelper.OPEN_BRACKET + input + SPARQLQueryHelper.CLOSE_BRACKET;
		});
		ReflectionUtils.setFieldValue(semanticQueryVisitor, "parser", parser);
	}

	/**
	 * Test the {@link SemanticQueryVisitor} buildSolrQuery method with an AND predicate and an empty solr query. The
	 * expected result is an empty filter clause list.
	 *
	 * @throws Exception
	 *             if something goes wrong
	 */
	@Test(dataProvider = "solrQueryTestData")
	@SuppressWarnings("unchecked")
	public void testBuildSolrQuery(String predicate, String solrQuery, QueryBoost queryBoost) throws Exception {
		buildSimpleQuery(predicate, solrQuery, queryBoost);
		Pair<QueryBoost, Serializable> pair = getConnectorQueryFromPedingPredicates();
		Assert.assertNotNull(pair);
		Assert.assertEquals(pair.getFirst(), queryBoost);

		String query = (String) pair.getSecond();
		Assert.assertTrue(query.contains(solrQuery), "The filter and list should contain " + solrQuery);
	}

	/**
	 * Tests building of query with the parameters fts and fq at the same time. The result should be concatenation of
	 * the two queries in one solr query
	 *
	 * @throws Exception
	 *             If an error occurs
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testBuildFQAndFTSQuery() throws Exception {
		Query query = new Query("fts", "testFts");
		query.setBoost(QueryBoost.INCLUDE_AND);
		semanticQueryVisitor.start();
		semanticQueryVisitor.visit(query);
		query = new Query("fq", "testFq");
		query.setBoost(QueryBoost.INCLUDE_AND);
		semanticQueryVisitor.visit(query);

		Pair<QueryBoost, Serializable> pair = getConnectorQueryFromPedingPredicates();
		Assert.assertNotNull(pair);
		Assert.assertEquals(pair.getFirst(), QueryBoost.INCLUDE_AND);

		String stringQuery = (String) pair.getSecond();
		Assert.assertNotNull(stringQuery);
		Assert.assertTrue(stringQuery.contains("testFts"));
		Assert.assertTrue(stringQuery.contains("testFq"));
		semanticQueryVisitor.end();
		stringQuery = semanticQueryVisitor.getQuery().toString();
		Assert.assertTrue(stringQuery.contains(CONNECTOR_NAME_CONSTANT));
	}

	private Pair<QueryBoost, Serializable> getConnectorQueryFromPedingPredicates() {
		Map<String, Pair<QueryBoost, Serializable>> pendingPredicates = (Map<String, Pair<QueryBoost, Serializable>>) ReflectionUtils
				.getFieldValue(semanticQueryVisitor, "pendingPredicates");
		Pair<QueryBoost, Serializable> pair = pendingPredicates.get(SemanticQueryVisitor.CONNECTOR_QUERY_PREDICATE);
		return pair;
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testBuildRegexFullTextSearchClause() throws Exception {
		// Test the escaping of the : symbol and replaceIgnoreCase of AND and OR.
		String query = "test\\:ads";
		// The query boost doesn't matter here.
		buildSimpleQuery("title", query, QueryBoost.INCLUDE_AND);
		Map<String, Serializable> bindings = (Map<String, Serializable>) ReflectionUtils.getFieldValue(semanticQueryVisitor,
				"bindings");
		Assert.assertEquals(bindings.get("titleVariable"), "test\\:ads");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testBuildBooleanClause() throws Exception {
		buildSimpleQuery("emf:isActive", Boolean.TRUE, QueryBoost.INCLUDE_AND);
		semanticQueryVisitor.end();

		Map<String, Serializable> bindings = semanticQueryVisitor.getBindings();
		Assert.assertEquals(bindings.get("emfisActiveVariable"), Boolean.TRUE);

		String query = semanticQueryVisitor.getQuery().toString();
		GeneralSemanticTest.assertValidSparqlQuery(query);
		String expectedQuery = "SELECT DISTINCT ?instance ?instanceType " + SPARQLQueryHelper.LINE_SEPARATOR
				+ " WHERE { " + SPARQLQueryHelper.LINE_SEPARATOR + "?instance emf:instanceType ?instanceType . "
				+ SPARQLQueryHelper.LINE_SEPARATOR + "?instance emf:isDeleted \"false\"^^xsd:boolean . "
				+ SPARQLQueryHelper.LINE_SEPARATOR + "?instance emf:isActive ?emfisActiveVariable . "
				+ SPARQLQueryHelper.LINE_SEPARATOR + " } ";
		Assert.assertEquals(query, expectedQuery);
	}

	/**
	 * Tests building query with passing DateRange as parameter
	 *
	 * @throws Exception
	 *             if something goes wrong
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testBuildDateClause() throws Exception {
		Calendar calendar = Calendar.getInstance();
		Date firstDate = calendar.getTime();
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		Date secondDate = calendar.getTime();
		buildSimpleQuery("emf:createdOn", new DateRange(firstDate, secondDate), QueryBoost.INCLUDE_AND);
		semanticQueryVisitor.end();

		String query = semanticQueryVisitor.getQuery().toString();
		GeneralSemanticTest.assertValidSparqlQuery(query);
		String expectedQuery = "SELECT DISTINCT ?instance ?instanceType " + SPARQLQueryHelper.LINE_SEPARATOR
				+ " WHERE { " + SPARQLQueryHelper.LINE_SEPARATOR + "?instance emf:instanceType ?instanceType . "
				+ SPARQLQueryHelper.LINE_SEPARATOR + "?instance emf:isDeleted \"false\"^^xsd:boolean . "
				+ SPARQLQueryHelper.LINE_SEPARATOR + "?instance emf:createdOn ?emfcreatedOn . "
				+ SPARQLQueryHelper.LINE_SEPARATOR
				+ " FILTER ( ?emfcreatedOn >= xsd:dateTime(\"" + ISO8601DateFormat.format(firstDate)
				+ "\") && ?emfcreatedOn <= xsd:dateTime(\"" + ISO8601DateFormat.format(secondDate) + "\") )  . "
				+ SPARQLQueryHelper.LINE_SEPARATOR + " } ";
		Assert.assertEquals(query, expectedQuery);
	}

	/**
	 * Tests building query with passing DateRange as parameter - with equal dates
	 *
	 * @throws Exception
	 *             if something goes wrong
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testBuildDateClauseEqualDates() throws Exception {
		Calendar calendar = Calendar.getInstance();
		Date date = calendar.getTime();
		buildSimpleQuery("emf:createdOn", new DateRange(date, date), QueryBoost.INCLUDE_AND);
		semanticQueryVisitor.end();

		String query = semanticQueryVisitor.getQuery().toString();
		GeneralSemanticTest.assertValidSparqlQuery(query);
		String expectedQuery = "SELECT DISTINCT ?instance ?instanceType " + SPARQLQueryHelper.LINE_SEPARATOR
				+ " WHERE { " + SPARQLQueryHelper.LINE_SEPARATOR + "?instance emf:instanceType ?instanceType . "
				+ SPARQLQueryHelper.LINE_SEPARATOR + "?instance emf:isDeleted \"false\"^^xsd:boolean . "
				+ SPARQLQueryHelper.LINE_SEPARATOR + "?instance emf:createdOn ?emfcreatedOn . "
				+ SPARQLQueryHelper.LINE_SEPARATOR + " FILTER ( ?emfcreatedOn = xsd:dateTime(\""
				+ ISO8601DateFormat.format(date) + "\") )  . "
				+ SPARQLQueryHelper.LINE_SEPARATOR + " } ";
		Assert.assertEquals(query, expectedQuery);
	}

	/**
	 * Tests building query with passing DateRange as parameter - passing start date only
	 *
	 * @throws Exception
	 *             if something goes wrong
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testBuildDateClauseFirstOnly() throws Exception {
		Calendar calendar = Calendar.getInstance();
		Date firstDate = calendar.getTime();
		buildSimpleQuery("emf:createdOn", new DateRange(firstDate, null), QueryBoost.INCLUDE_AND);
		semanticQueryVisitor.end();

		String query = semanticQueryVisitor.getQuery().toString();
		GeneralSemanticTest.assertValidSparqlQuery(query);
		String expectedQuery = "SELECT DISTINCT ?instance ?instanceType " + SPARQLQueryHelper.LINE_SEPARATOR
				+ " WHERE { " + SPARQLQueryHelper.LINE_SEPARATOR + "?instance emf:instanceType ?instanceType . "
				+ SPARQLQueryHelper.LINE_SEPARATOR + "?instance emf:isDeleted \"false\"^^xsd:boolean . "
				+ SPARQLQueryHelper.LINE_SEPARATOR + "?instance emf:createdOn ?emfcreatedOn . "
				+ SPARQLQueryHelper.LINE_SEPARATOR + " FILTER ( ?emfcreatedOn >= xsd:dateTime(\""
				+ ISO8601DateFormat.format(firstDate) + "\") )  . "
				+ SPARQLQueryHelper.LINE_SEPARATOR + " } ";
		Assert.assertEquals(query, expectedQuery);
	}

	/**
	 * Tests building query with passing DateRange as parameter - passing second date only
	 *
	 * @throws Exception
	 *             if something goes wrong
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testBuildDateClauseSecondOnly() throws Exception {
		Calendar calendar = Calendar.getInstance();
		Date date = calendar.getTime();
		buildSimpleQuery("emf:createdOn", new DateRange(null, date), QueryBoost.INCLUDE_AND);
		semanticQueryVisitor.end();

		String query = semanticQueryVisitor.getQuery().toString();
		GeneralSemanticTest.assertValidSparqlQuery(query);
		String expectedQuery = "SELECT DISTINCT ?instance ?instanceType " + SPARQLQueryHelper.LINE_SEPARATOR
				+ " WHERE { " + SPARQLQueryHelper.LINE_SEPARATOR + "?instance emf:instanceType ?instanceType . "
				+ SPARQLQueryHelper.LINE_SEPARATOR + "?instance emf:isDeleted \"false\"^^xsd:boolean . "
				+ SPARQLQueryHelper.LINE_SEPARATOR + "?instance emf:createdOn ?emfcreatedOn . "
				+ SPARQLQueryHelper.LINE_SEPARATOR + " FILTER ( ?emfcreatedOn <= xsd:dateTime(\""
				+ ISO8601DateFormat.format(date) + "\") )  . "
				+ SPARQLQueryHelper.LINE_SEPARATOR + " } ";
		Assert.assertEquals(query, expectedQuery);
	}

	/**
	 * Tests building of the filter for filtering instances by type with null value
	 */
	@Test
	public void testBuildIgnoreInstanceByTypeClauseWithNullValue() {
		semanticQueryVisitor.setIgnoreInstancesForType(null);
		Assert.assertNull(ReflectionUtils.getFieldValue(semanticQueryVisitor, "ignoreInstancesForTypeClause"));
	}

	/**
	 * Tests building of the filter for filtering instances by type with empty value
	 */
	@Test
	public void testBuildIgnoreInstanceByTypeClauseWithEmptyValue() {
		semanticQueryVisitor.setIgnoreInstancesForType("");
		Assert.assertNull(ReflectionUtils.getFieldValue(semanticQueryVisitor, "ignoreInstancesForTypeClause"));

		semanticQueryVisitor.setIgnoreInstancesForType(", , , , ,,,,  ");
		Assert.assertNull(ReflectionUtils.getFieldValue(semanticQueryVisitor, "ignoreInstancesForTypeClause"));
	}

	/**
	 * Tests building of the filter for filtering instances by type with one instance type as value
	 */
	@Test
	public void testBuildIgnoreInstanceByTypeClauseWithOneValue() {
		semanticQueryVisitor.setIgnoreInstancesForType("instance");
		String ignoreInstancesForTypeClause = (String) ReflectionUtils.getFieldValue(semanticQueryVisitor,
				"ignoreInstancesForTypeClause");
		Assert.assertNotNull(ignoreInstancesForTypeClause);
		Assert.assertEquals(ignoreInstancesForTypeClause,
				" FILTER ( ?instanceType != \"instance\" ) " + LINE_SEPARATOR);
	}

	/**
	 * Tests building of the filter for filtering instances by type with multiple instance types as value
	 */
	@Test
	public void testBuildIgnoreInstanceByTypeClauseWithMultiValues() {
		semanticQueryVisitor.setIgnoreInstancesForType("instance,    user,   group");
		String ignoreInstancesForTypeClause = (String) ReflectionUtils.getFieldValue(semanticQueryVisitor,
				"ignoreInstancesForTypeClause");
		Assert.assertNotNull(ignoreInstancesForTypeClause);
		Assert.assertEquals(ignoreInstancesForTypeClause,
				" FILTER ( ?instanceType != \"instance\" && ?instanceType != \"user\" && ?instanceType != \"group\" ) "
						+ LINE_SEPARATOR);
	}

	/**
	 * Tests building of the filter for filtering instances by type and set flat not to apply the filter
	 */
	@Test
	public void testBuildIgnoreInstanceByTypeClauseNotApplyFilter() {
		semanticQueryVisitor.setIgnoreInstancesForType("instance,    user,   group");
		semanticQueryVisitor.setApplyFilterForType(false);
		String ignoreInstancesForTypeClause = (String) ReflectionUtils.getFieldValue(semanticQueryVisitor,
				"ignoreInstancesForTypeClause");
		Assert.assertNotNull(ignoreInstancesForTypeClause);
		Assert.assertEquals(ignoreInstancesForTypeClause,
				" FILTER ( ?instanceType != \"instance\" && ?instanceType != \"user\" && ?instanceType != \"group\" ) "
						+ LINE_SEPARATOR);

		semanticQueryVisitor.start();

		StringBuilder whereClause = (StringBuilder) ReflectionUtils.getFieldValue(semanticQueryVisitor, "whereClause");
		Assert.assertFalse(whereClause.toString().contains(ignoreInstancesForTypeClause));
	}

	/**
	 * Tests building of the filter for filtering instances by type and set flat not to apply the filter
	 */
	@Test
	public void testUnprocessedParameters() {
		String queryString = "select * where { ?instance emf:createdOn ?systemTime }";
		Map<String, Serializable> queryArguments = CollectionUtils.createHashMap(2);
		queryArguments.put(SPARQLQueryHelper.QUERY_EXISTING_BODY, queryString);
		queryArguments.put("systemTime", new Date());

		Query searchQuery = Query.getEmpty();
		searchQuery.and(Query.fromMap(queryArguments, QueryBoost.INCLUDE_AND));
		try {
			searchQuery.visit(semanticQueryVisitor);
			String query = semanticQueryVisitor.getQuery().toString();
			Assert.assertTrue(semanticQueryVisitor.getBindings().containsKey("systemTime"));
		} catch (Exception e) {
			Assert.fail("Unexpected exception is thrown!", e);
		}
	}

	@Test
	public void testBuildListClause() {
		Map<String, Serializable> queryArguments = CollectionUtils.createHashMap(1);
		queryArguments.put("oa:hasTarget", (Serializable) Arrays.asList("emf:test1", "emf:test2", "emf:test3"));

		Query searchQuery = Query.getEmpty();
		searchQuery.and(Query.fromMap(queryArguments, QueryBoost.INCLUDE_AND));
		try {
			searchQuery.visit(semanticQueryVisitor);
			String query = semanticQueryVisitor.getQuery().toString();
			GeneralSemanticTest.assertValidSparqlQuery(query);
			Assert.assertTrue(query.contains("FILTER ( ?oahasTargetMainVariable = ?oahasTargetVariable || ?oahasTargetMainVariable = ?oahasTargetVariable1 || ?oahasTargetMainVariable = ?oahasTargetVariable2 )  ."));
			Assert.assertTrue(semanticQueryVisitor.getBindings().containsKey("oahasTargetVariable"));
			Assert.assertTrue(semanticQueryVisitor.getBindings().containsKey("oahasTargetVariable1"));
			Assert.assertTrue(semanticQueryVisitor.getBindings().containsKey("oahasTargetVariable2"));
			Assert.assertFalse(semanticQueryVisitor.getBindings().containsKey("oahasTargetMainVariable"));
		} catch (Exception e) {
			Assert.fail("Unexpected exception is thrown!", e);
		}
	}
	
	@Test
	public void testBuildLargeListClause() {
		Map<String, Serializable> queryArguments = CollectionUtils.createHashMap(1);
		List<Serializable> testData = new ArrayList<>(2000);
		for(int counter = 0; counter < 2000; counter++) {
			testData.add("emf:test" + counter);
		}
		
		queryArguments.put("oa:hasTarget", (Serializable) testData);

		Query searchQuery = Query.getEmpty();
		searchQuery.and(Query.fromMap(queryArguments, QueryBoost.INCLUDE_AND));
		try {
			searchQuery.visit(semanticQueryVisitor);
			String query = semanticQueryVisitor.getQuery().toString();
			Assert.assertTrue(query.contains(SPARQLQueryHelper.VALUES));
			for(Serializable value : testData) {
				Assert.assertTrue(query.contains(value.toString()));
			}
			
			GeneralSemanticTest.assertValidSparqlQuery(query);
		} catch (Exception e) {
			Assert.fail("Unexpected exception is thrown!", e);
		}
	}

	/**
	 * Build the simple query.
	 *
	 * @param key
	 *            the query key
	 * @param value
	 *            the query value
	 * @param queryBoost
	 *            the query boost
	 * @throws Exception
	 *             if something goes wrong
	 */
	private void buildSimpleQuery(String key, Serializable value, QueryBoost queryBoost) throws Exception {
		Query query = new Query(key, value);
		query.setBoost(queryBoost);
		semanticQueryVisitor.start();
		semanticQueryVisitor.visit(query);
	}
	
	/**
	 * Provides test data for testing generation of Solr query
	 *
	 * @return
	 */
	@DataProvider(name = "solrQueryTestData")
	public static Object[][] generateTestDataForSolrQuery() {
		return new Object[][] { { "fq", "", QueryBoost.INCLUDE_AND }, { "fq", "TestAnd", QueryBoost.INCLUDE_AND },
			{ "fq", "TestOr", QueryBoost.INCLUDE_OR }, { "fts", "TestOr", QueryBoost.INCLUDE_OR },
			{ "fts", "TestAnd", QueryBoost.INCLUDE_AND } };
	}
}
