/**
 *
 */
package com.sirma.itt.emf.semantic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;

import org.openrdf.repository.RepositoryException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.sirma.itt.emf.mocks.search.QueryBuilderMock;
import com.sirma.itt.emf.semantic.persistence.ValueConverter;
import com.sirma.itt.emf.semantic.queries.QueryBuilderImpl;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.convert.TypeConverterImpl;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.search.NamedQueries;

/**
 * Tests QueryBuilder
 *
 * @author kirq4e
 */
public class QueryBuilderImplTest {

	private QueryBuilderImpl service;

	/**
	 * Initializes service
	 *
	 * @throws RepositoryException
	 */
	@BeforeClass
	public void init() {
		service = new QueryBuilderMock(null);
	}

	/**
	 * Tests generation of the query checkExistingInstances with URI
	 */
	@Test
	public void testCheckExistingInstancesWithURI() {
		String expectedQuery = "SELECT DISTINCT ?instance ?instanceType WHERE {{ select (emf:testInstance as ?instance) ?instanceType where {emf:testInstance emf:instanceType ?instanceType . emf:testInstance emf:isDeleted \"false\"^^xsd:boolean } }}";

		String query = service.buildQueryByName(NamedQueries.CHECK_EXISTING_INSTANCE,
				Arrays.asList(new Pair<String, Object>(NamedQueries.Params.URIS, Arrays.asList("emf:testInstance"))));

		Assert.assertEquals(query, expectedQuery);
	}

	/**
	 * Tests generation of the query checkExistingInstances without parameters. The expected result is null
	 */
	@Test
	public void testCheckExistingInstancesWithoutParameters() {
		String query = service.buildQueryByName(NamedQueries.CHECK_EXISTING_INSTANCE,
				Arrays.asList(new Pair<String, Object>(NamedQueries.Params.URIS, new ArrayList<>())));
		Assert.assertNull(query);
	}

	/**
	 * Tests generation of the query checkExistingInstances with multiple URIs
	 */
	@Test
	public void testCheckMultipleExistingInstances() {
		String expectedQuery = "SELECT DISTINCT ?instance ?instanceType WHERE {{ select (emf:testInstance as ?instance) ?instanceType where {emf:testInstance emf:instanceType ?instanceType . emf:testInstance emf:isDeleted \"false\"^^xsd:boolean } }\n"
				+ "	 UNION{ select (emf:testInstance2 as ?instance) ?instanceType where {emf:testInstance2 emf:instanceType ?instanceType . emf:testInstance2 emf:isDeleted \"false\"^^xsd:boolean } }}";

		String query = service.buildQueryByName(NamedQueries.CHECK_EXISTING_INSTANCE,
				Arrays.asList(new Pair<String, Object>(NamedQueries.Params.URIS,
						Arrays.asList("emf:testInstance", "emf:testInstance2"))));

		Assert.assertEquals(query, expectedQuery);
	}

	/**
	 * Tests generation of the query checkExistingInstances with multiple URIs
	 */
	@Test
	public void testMissingQuery() {
		String query = service.buildQueryByName("unknown query",
				Arrays.asList(new Pair<String, Object>(NamedQueries.Params.URIS,
						Arrays.asList("emf:testInstance", "emf:testInstance2"))));

		Assert.assertNull(query);
	}

	/**
	 * Tests query selectInstancesCallback
	 */
	@Test
	public void testSelectInstancesCallback() {
		String expectedQuery = "SELECT DISTINCT ?uri ?propertyName ?propertyValue WHERE { "
				+ "?uri ?propertyName ?propertyValue . " + "?uri emf:isDeleted \"false\"^^xsd:boolean . "
				+ "{ ?propertyName a emf:DefinitionObjectProperty. ?propertyValue emf:isDeleted \"false\"^^xsd:boolean . } "
				+ "UNION { ?propertyName a emf:DefinitionDataProperty. } "
				+ "FILTER( ?uri = emf:testInstance || ?uri = <http://testInstance2> ).} ";
		String query = service.buildQueryByName(NamedQueries.SELECT_BY_IDS,
				Arrays.asList(new Pair<String, Object>(NamedQueries.Params.URIS,
						Arrays.asList("emf:testInstance", "http://testInstance2"))));

		Assert.assertEquals(query.replaceAll("\\s+", " "), expectedQuery.replaceAll("\\s+", " "));
	}

	/**
	 * Tests query loadPropertiesCallback
	 */
	@Test
	public void testLoadPropertiesCallback() {
		String expectedQuery = "SELECT DISTINCT ?uri ?propertyName ?propertyValue WHERE { \n" + "	 { \n"
				+ "		 SELECT DISTINCT (emf:testInstance as ?uri) ?propertyName ?propertyValue { \n"
				+ "			 emf:testInstance ?propertyName ?propertyValue . \n" + "		 } \n" + "	 }\n"
				+ "	 UNION	 { \n"
				+ "		 SELECT DISTINCT (emf:testInstance2 as ?uri) ?propertyName ?propertyValue { \n"
				+ "			 emf:testInstance2 ?propertyName ?propertyValue . \n" + "		 } \n" + "	 }\n" + "}";
		String query = service.buildQueryByName(NamedQueries.LOAD_PROPERTIES,
				Arrays.asList(new Pair<String, Object>(NamedQueries.Params.URIS,
						Arrays.asList("emf:testInstance", "emf:testInstance2"))));

		Assert.assertEquals(query, expectedQuery);
	}

	/**
	 * Tests query selectInstancesCallback with deleted instances
	 */
	@Test
	public void testSelectInstancesCallbackWithDeleted() {
		try {
			Options.ALLOW_LOADING_OF_DELETED_INSTANCES.enable();
			String expectedQuery = "SELECT DISTINCT ?uri ?propertyName ?propertyValue WHERE { "
					+ "?uri ?propertyName ?propertyValue . "
					+ "{ ?propertyName a emf:DefinitionObjectProperty. ?propertyValue emf:isDeleted \"false\"^^xsd:boolean . } "
					+ "UNION { ?propertyName a emf:DefinitionDataProperty. } "
					+ "FILTER( ?uri = emf:testInstance || ?uri = <http://testInstance2> ).} ";
			String query = service.buildQueryByName(NamedQueries.SELECT_BY_IDS,
					Arrays.asList(new Pair<String, Object>(NamedQueries.Params.URIS,
							Arrays.asList("emf:testInstance", "http://testInstance2"))));

			Assert.assertEquals(query.replaceAll("\\s+", " "), expectedQuery.replaceAll("\\s+", " "));
		} finally {
			Options.ALLOW_LOADING_OF_DELETED_INSTANCES.disable();
		}
	}

	@Test
	public void buildValueFilter() throws Exception {
		Function<String, String> function = service.buildValueFilter("emf:test", "value");
		Assert.assertNotNull(function);
		Assert.assertEquals(function.apply("?instance"), "?instance emf:test \"value\". ");
		Assert.assertEquals(function.apply("?subject"), "?subject emf:test \"value\". ");

		Assert.assertEquals(service.buildValueFilter("emf:test", "  ").apply("?instance"), "");
	}

	@Test
	public void buildValuesFilter() throws Exception {
		TypeConverterImpl converter = new TypeConverterImpl();
		new ValueConverter().register(converter);
		TypeConverterUtil.setTypeConverter(converter);

		Function<String, String> function = service.buildValuesFilter("emf:test", Arrays.asList("value1", "value2"));
		Assert.assertNotNull(function);
		Assert.assertEquals(function.apply("?instance"),
				" { ?instance emf:test \"value1\".  } UNION { ?instance emf:test \"value2\".  } .");

		Assert.assertEquals(service.buildValuesFilter("emf:test", Arrays.asList("value")).apply("?instance"),
				"?instance emf:test \"value\". ");

		function = service.buildValuesFilter("emf:test", Arrays.asList("value1", "value2", "  ", null, ""));
		Assert.assertEquals(function.apply("?instance"),
				" { ?instance emf:test \"value1\".  } UNION { ?instance emf:test \"value2\".  } .");
	}

}
