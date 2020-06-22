package com.sirma.itt.emf.semantic;

import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.BLOCK_END;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.BLOCK_START;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.LINE_SEPARATOR;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.OBJECT_VARIABLE_FOR_REPLACE;
import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.VARIABLE;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.emf.GeneralSemanticTest;
import com.sirma.itt.emf.mocks.NamespaceRegistryMock;
import com.sirma.itt.emf.semantic.exception.SemanticPersistenceException;
import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.emf.semantic.search.TupleQueryResultIterator;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.search.Sorter;
import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.semantic.model.vocabulary.Security;

/**
 * @author kirq4e
 */
@SuppressWarnings("static-method")
public class SPARQLQueryHelperTest extends GeneralSemanticTest<SPARQLQueryHelper> {

	/**
	 * Initializes service
	 *
	 * @throws RepositoryException
	 */
	@BeforeClass
	public void init() {
		SPARQLQueryHelper.setCaseInsenitiveOrderByList("dcterms:title");
	}

	@BeforeMethod
	@Override
	public void beforeMethod() {
		super.beforeMethod();
		noTransaction();
	}

	/**
	 * Tests permissions filter for user that is not administrator and instance name - instance
	 */
	@Test
	public void testGetPermissionsFilter() {
		String expectedPermissionsFilter = loadPermissionFilterRegex("readPermissionsQuery.sparql");

		String permissionsFilter = SPARQLQueryHelper.getPermissionsFilter("instance", false, true);
		Assert.assertTrue(permissionsFilter.trim().matches(expectedPermissionsFilter.trim()));
	}

	/**
	 * Tests permissions filter for user that is manager or has write permissions
	 */
	@Test
	public void testGetWritePermissionsFilter() {
		String expectedPermissionsFilter = loadPermissionFilterRegex("writePermissionsQuery.sparql") ;

		String permissionsFilter = SPARQLQueryHelper.getPermissionsFilter("instance", false, false);
		Assert.assertTrue(permissionsFilter.trim().matches(expectedPermissionsFilter.trim()));
	}

	private static String loadPermissionFilterRegex(String permissionsTemplateFileName) {
		String expectedPermissionsFilter = BLOCK_START + LINE_SEPARATOR
				+ ResourceLoadUtil.loadResource(SPARQLQueryHelper.class, permissionsTemplateFileName) + LINE_SEPARATOR
				+ BLOCK_END;

		return expectedPermissionsFilter
				.replace(OBJECT_VARIABLE_FOR_REPLACE, VARIABLE + "instance")
					.replace(SPARQLQueryHelper.PERMISSIONS_SUFFIX, "\\d+")
					.replace("{", "\\{")
					.replace("}", "\\}")
					.replace("!", "\\!")
					.replace("?", "\\?")
					.replace(".", "\\.")
					.replace("(", "\\(")
					.replace(")", "\\)");
	}

	/**
	 * Tests appending order by to a query without order by clause
	 */
	@Test
	public void testAppendOrderByToQuery1() {
		String query = SPARQLQueryHelper.appendOrderByToQuery("SELECT * WHERE { ?s ?p ?o . }",
				Arrays.asList(new Sorter("emf:modifiedOn", true)), false);

		Assert.assertFalse(query.contains("ORDER BY"));
	}

	/**
	 * Tests appending order by to a query with order by clause
	 */
	@Test
	public void testAppendOrderByToQuery2() {
		String query = SPARQLQueryHelper.appendOrderByToQuery("SELECT * WHERE { ?s ?p ?o . }",
				Arrays.asList(new Sorter("emf:modifiedOn", true)), true);

		Assert.assertTrue(query.contains("ORDER BY"));
	}

	/**
	 * Tests appending order by to a query with wrong query
	 */
	@Test
	public void testAppendOrderByToQuery3() {
		String expectedQuery = "SELECT * WHERE { ?s ?p ?o . ";
		String query = SPARQLQueryHelper.appendOrderByToQuery(expectedQuery,
				Arrays.asList(new Sorter("emf:modifiedOn", true)), true);

		Assert.assertEquals(query, expectedQuery);
	}

	/**
	 * Tests appending order by to a query without sort fields
	 */
	@Test
	public void testAppendOrderByToQuery4() {
		String expectedQuery = "SELECT * WHERE { ?s ?p ?o . ";
		String query = SPARQLQueryHelper.appendOrderByToQuery(expectedQuery, new ArrayList<>(), true);

		Assert.assertEquals(query, expectedQuery);
	}

	/**
	 * Tests appending order by to a query with to lower case sort field
	 */
	@Test
	public void testAppendOrderByToQuery5() {
		String query = SPARQLQueryHelper.appendOrderByToQuery("SELECT * WHERE { ?s ?p ?o . }",
				Arrays.asList(new Sorter("dcterms:title", true)), true);

		Assert.assertTrue(query.contains("bind(lcase"));
	}

	/**
	 * Tests appending order by to a query with to lower case sort field
	 */
	@Test
	public void testAppendOrderByToQuery6() {
		String query = SPARQLQueryHelper.appendOrderByToQuery("SELECT * WHERE { ?s ?p ?o . }",
				Arrays.asList(new Sorter("dcterms:title", true)), true);

		Assert.assertTrue(query.contains("bind(lcase"));
	}

	/**
	 * Tests appending order by to a query object properties sorting
	 */
	@Test
	public void testAppendOrder_forRelations() {

		String query = SPARQLQueryHelper.appendOrderByToQuery("SELECT * WHERE { ?s ?p ?o . }",
				Collections.singletonList(new Sorter("emf:createdBy", true).setObjectProperty(true)), true);

		Assert.assertTrue(query.contains("MIN("), "should have duplicate result handling");
		Assert.assertTrue(query.contains(SPARQLQueryHelper.SORT_TITLE), "should sort by the static header");
	}

	/**
	 * Tests appending order by to a query optional object properties sorting
	 */
	@Test
	public void testAppendOrder_forOptionalRelations() {

		String query = SPARQLQueryHelper.appendOrderByToQuery("SELECT * WHERE { ?s ?p ?o . }",
				Collections.singletonList(new Sorter("emf:createdBy", true, true).setObjectProperty(true)), true);

		Assert.assertTrue(query.contains("MIN("), "should have duplicate result handling");
		Assert.assertTrue(query.contains(SPARQLQueryHelper.SORT_TITLE), "should sort by the static header");
		Assert.assertTrue(query.contains(SPARQLQueryHelper.OPTIONAL_BLOCK_START), "should support optional values");
	}

	/**
	 * Tests appending offset to query
	 */
	@Test
	public void testAppendOffsetToQuery() {
		String query = SPARQLQueryHelper.appendOffsetToQuery("SELECT * WHERE { ?s ?p ?o . }", 10);

		Assert.assertEquals(query, "SELECT * WHERE { ?s ?p ?o . } OFFSET 10");
	}

	/**
	 * Tests appending limit to query
	 */
	@Test
	public void testAppendLimitToQuery() {
		String query = SPARQLQueryHelper.appendLimitToQuery("SELECT * WHERE { ?s ?p ?o . }", 10);

		Assert.assertEquals(query, "SELECT * WHERE { ?s ?p ?o . } LIMIT 10");
	}

	/**
	 * Tests prepareTupleQuery method
	 */
	@Test
	public void testPrepareTupleQuery() {
		RepositoryConnection repositoryConnection = null;
		try {
			repositoryConnection = connectionFactory.produceConnection();
			String query = "select * where { ?user sec:canRead ?instance . ?instance dcterms:title ?title. ?instance dcterms:title ?title2 . "
					+ "?instance dcterms:title ?title3 . ?instance dcterms:title ?title4 . ?instance emf:createdOn ?createdOn .}";
			Map<String, Serializable> bindings = CollectionUtils.createHashMap(6);
			bindings.put("instance", "emf:test_instance");
			bindings.put("user", Security.SYSTEM_ALL_OTHER_USERS);
			bindings.put("title", "Test\\:Test");
			bindings.put("title2", "TestTest");
			bindings.put("title3", null);
			bindings.put("title4", null);
			bindings.put("createdOn", new Date());
			SPARQLQueryHelper.setNamespaceRegistryService(new NamespaceRegistryMock(context));
			TupleQuery tupleQuery = SPARQLQueryHelper.prepareTupleQuery(repositoryConnection, query, bindings, true,
					10);

			Assert.assertNotNull(tupleQuery);
			Assert.assertEquals(tupleQuery.getBindings().size(), 5);
		} finally {
			if (repositoryConnection != null) {
				try {
					repositoryConnection.close();
				} catch (RepositoryException e) {
					throw new SemanticPersistenceException(e);
				}
			}
		}
	}

	/**
	 * Tests prepareTupleQuery method
	 */
	@Test
	public void testPrepareTupleQuery2() {
		RepositoryConnection repositoryConnection = null;
		try {
			repositoryConnection = connectionFactory.produceConnection();
			String query = "select * where { ?user sec:canRead ?instance . ?instance dcterms:title ?title. ?instance dcterms:title ?title2 . "
					+ "?instance dcterms:title ?title3 . ?instance dcterms:title ?title4 . ?instance emf:createdOn ?createdOn .}";
			Map<String, Serializable> bindings = CollectionUtils.createHashMap(6);
			bindings.put("instance", "emf:test_instance");
			bindings.put("user", Security.SYSTEM_ALL_OTHER_USERS);
			bindings.put("title", "Test\\:Test");
			bindings.put("title2", "TestTest");
			bindings.put("title3", null);
			bindings.put("title4", null);
			bindings.put("createdOn", new Date());
			SPARQLQueryHelper.setNamespaceRegistryService(new NamespaceRegistryMock(context));
			TupleQuery tupleQuery = SPARQLQueryHelper.prepareTupleQuery(repositoryConnection, query, bindings, true);

			Assert.assertNotNull(tupleQuery);
			Assert.assertEquals(tupleQuery.getBindings().size(), 5);
		} finally {
			if (repositoryConnection != null) {
				try {
					repositoryConnection.close();
				} catch (RepositoryException e) {
					throw new SemanticPersistenceException(e);
				}
			}
		}
	}

	/**
	 * Tests prepareBooleanQuery method
	 */
	@Test
	public void testPrepareBooleanQuery() {
		RepositoryConnection repositoryConnection = null;
		try {
			repositoryConnection = connectionFactory.produceConnection();
			String query = "ASK { ?instance ?p ?o . ?instance ?createdBy ?user . ?instance dcterms:title ?title . ?instance dcterms:title ?title2 . ?instance dcterms:title ?title3 .?instance dcterms:title ?title4 ."
					+ "?instance emf:createdOn ?createdOn .}";
			Map<String, Serializable> bindings = CollectionUtils.createHashMap(6);
			bindings.put("instance", "emf:test_instance");
			bindings.put("user", Security.SYSTEM_ALL_OTHER_USERS);
			bindings.put("title", "Test\\:Test");
			bindings.put("title2", "TestTest");
			bindings.put("title3", null);
			bindings.put("title4", null);
			bindings.put("createdOn", new Date());
			SPARQLQueryHelper.setNamespaceRegistryService(new NamespaceRegistryMock(context));
			BooleanQuery tupleQuery = SPARQLQueryHelper.prepareBooleanQuery(repositoryConnection, query, bindings, true,
					10);

			Assert.assertNotNull(tupleQuery);
			Assert.assertEquals(tupleQuery.getBindings().size(), 5);
		} finally {
			if (repositoryConnection != null) {
				try {
					repositoryConnection.close();
				} catch (RepositoryException e) {
					throw new SemanticPersistenceException(e);
				}
			}
		}
	}

	/**
	 * Tests prepareBooleanQuery method
	 */
	@Test
	public void testPrepareUpdateQuery() {
		RepositoryConnection repositoryConnection = null;
		try {
			repositoryConnection = connectionFactory.produceConnection();
			String query = "DELETE { ?instance ?p ?o. } INSERT { ?instance ?p ?o . } WHERE { ?instance ?p ?o . ?instance ?createdBy ?user . ?instance dcterms:title ?title . ?instance dcterms:title ?title2 . ?instance dcterms:title ?title3 .?instance dcterms:title ?title4 ."
					+ "?instance emf:createdOn ?createdOn .}";
			Map<String, Serializable> bindings = CollectionUtils.createHashMap(6);
			bindings.put("instance", "emf:test_instance");
			bindings.put("user", Security.SYSTEM_ALL_OTHER_USERS);
			bindings.put("title", "Test\\:Test");
			bindings.put("title2", "TestTest");
			bindings.put("title3", null);
			bindings.put("title4", null);
			bindings.put("createdOn", new Date());
			SPARQLQueryHelper.setNamespaceRegistryService(new NamespaceRegistryMock(context));
			Update tupleQuery = SPARQLQueryHelper.prepareUpdateQuery(repositoryConnection, query, bindings, true);

			Assert.assertNotNull(tupleQuery);
			Assert.assertEquals(tupleQuery.getBindings().size(), 5);
			Assert.assertTrue(tupleQuery.getIncludeInferred());
		} finally {
			if (repositoryConnection != null) {
				try {
					repositoryConnection.close();
				} catch (RepositoryException e) {
					throw new SemanticPersistenceException(e);
				}
			}
		}
	}

	@Test
	public void testQueryWrappingWithGroupBy() {
		String queryForWrapping = "?instance emf:status ?status .";
		String wrappedQuery = SPARQLQueryHelper.wrapQueryWithGroupBy(queryForWrapping, "emf:definitionId", false);

		String expected = "SELECT (?groupByProperty AS ?name) (count(?groupByProperty) AS ?count) ";
		expected += "WHERE { ?instance emf:definitionId ?groupByProperty .  { ?instance emf:status ?status . }  } GROUP BY ?groupByProperty";
		Assert.assertEquals(wrappedQuery, expected);
	}

	@Test
	public void testQueryWrappingWithGroupByObjectProperty() {
		String queryForWrapping = "?instance emf:status ?status .";
		String wrappedQuery = SPARQLQueryHelper.wrapQueryWithGroupBy(queryForWrapping, "emf:parentOf", true);

		String expected = "SELECT (?groupByProperty AS ?name) (count(?groupByProperty) AS ?count) ";
		expected += "WHERE { ?instance emf:parentOf ?groupByProperty . ?groupByProperty emf:isDeleted \"false\"^^xsd:boolean .  { ?instance emf:status ?status . }  } GROUP BY ?groupByProperty";
		Assert.assertEquals(wrappedQuery, expected);
	}

	@Test
	public void testReadPermissionsFilterQuery() throws QueryEvaluationException {
		RepositoryConnection repositoryConnection = null;
		try {
			repositoryConnection = connectionFactory.produceConnection();
			String readPermissionsQuery = "SELECT DISTINCT * WHERE {"
					+ SPARQLQueryHelper.getPermissionsFilter("instance", false, true) + "}";
			String replaceAll = readPermissionsQuery.replaceAll("\\?currentUser", "emf:consumer");
			Map<String, Serializable> bindings = CollectionUtils.createHashMap(1);
			TupleQuery tupleQuery = SPARQLQueryHelper.prepareTupleQuery(repositoryConnection, replaceAll, bindings,
					true);
			List<Pair<String, String>> result = parsePermissionsFilterResult(tupleQuery.evaluate());

			// the user has explicit read role on the instance
			assertTrue(instanceExists(result, "c663fcd7-5f8d-4d1b-9100-43c7c8874815"));

			// this instance should be in the results, because the user has read permissions on the library
			assertTrue(instanceExists(result, "5d1a5319-079e-4ad6-bc95-e7adb4c07812"));

			// this instance should be in the results, because the all other users have read role
			assertTrue(instanceExists(result, "13328fcf-9d27-4f82-b2cb-222c254fa01a"));

			// this instance should be in the results, because the user is member of a group with read role
			assertTrue(instanceExists(result, "93b6ebd8-0239-472d-8ed7-703fc4e4b313"));

			// emf:consumer doesn't have assigned any permissions to this instance and should not be in the results
			assertFalse(instanceExists(result, "instanceWithoutPermissionsForEmfConsumer"));
		} finally {
			if (repositoryConnection != null) {
				try {
					repositoryConnection.close();
				} catch (RepositoryException e) {
					throw new SemanticPersistenceException(e);
				}
			}
		}
	}

	@Test
	public void testWritePermissionsFilterQuery() throws QueryEvaluationException {
		RepositoryConnection repositoryConnection = null;
		try {
			repositoryConnection = connectionFactory.produceConnection();
			String readPermissionsQuery = "SELECT DISTINCT * WHERE { ?instance emf:isPartOfObjectLibrary \"true\"^^xsd:boolean . "
					+ SPARQLQueryHelper.getPermissionsFilter("instance", false, false) + "}";
			String replaceAll = readPermissionsQuery.replaceAll("\\?currentUser", "emf:user1");
			Map<String, Serializable> bindings = CollectionUtils.createHashMap(1);
			TupleQuery tupleQuery = SPARQLQueryHelper.prepareTupleQuery(repositoryConnection, replaceAll, bindings,
					true);
			List<Pair<String, String>> result = parsePermissionsFilterResult(tupleQuery.evaluate());

			assertTrue(instanceExists(result, "Document"));
			assertFalse(instanceExists(result, "Project"));
		} finally {
			if (repositoryConnection != null) {
				try {
					repositoryConnection.close();
				} catch (RepositoryException e) {
					throw new SemanticPersistenceException(e);
				}
			}
		}
	}

	@Test
	public void shouldAppendCorrectSorterForRanking() {
		Sorter rankingSorter = new Sorter(SPARQLQueryHelper.RANKING_SORTER_FIELD, true);
		String orderByQuery = SPARQLQueryHelper.appendOrderByToQuery("SELECT * WHERE { ?s ?p ?o . }",
				Collections.singletonList(rankingSorter), true);
		String expected = "?instance " + SPARQLQueryHelper.SOLR_SCORE;
		Assert.assertTrue(orderByQuery.contains(expected));
	}

	private static List<Pair<String, String>> parsePermissionsFilterResult(TupleQueryResult result) {
		List<Pair<String, String>> parsed = new ArrayList<>();

		try (TupleQueryResultIterator resultIterator = new TupleQueryResultIterator(result)) {
			while (resultIterator.hasNext()) {
				BindingSet binding = resultIterator.next();
				String permissionRoleName = "permissionsRole";
				for (String name : binding.getBindingNames()) {
					if (name.startsWith("permissionsRole")) {
						permissionRoleName = name;
						break;
					}
				}
				Pair<String, String> pair = new Pair<>(binding.getValue("instance").stringValue(),
						binding.getValue(permissionRoleName).stringValue());
				parsed.add(pair);
			}
		}

		return parsed;
	}

	private static boolean instanceExists(List<Pair<String, String>> result, String instanceId) {
		for (Pair<String, String> pair : result) {
			if (pair.getFirst().endsWith(instanceId)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected String getTestDataFile() {
		return "SPARQLQueryHelperTestData.ttl";
	}

}
