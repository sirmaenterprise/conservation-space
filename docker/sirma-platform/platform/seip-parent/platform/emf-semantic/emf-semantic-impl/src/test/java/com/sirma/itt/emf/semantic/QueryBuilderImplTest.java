package com.sirma.itt.emf.semantic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.emf.mocks.search.QueryBuilderMock;
import com.sirma.itt.emf.semantic.persistence.ValueConverter;
import com.sirma.itt.emf.semantic.queries.QueryBuilderImpl;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.convert.TypeConverterImpl;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.search.NamedQueries;
import com.sirma.itt.semantic.queries.QueryBuilder;

/**
 * Tests for {@link QueryBuilderImpl}.
 *
 * @author kirq4e
 */
public class QueryBuilderImplTest {

	private QueryBuilder service;

	@Before
	public void init() {
		service = new QueryBuilderMock(null);
	}

	/**
	 * Tests generation of the query checkExistingInstances with URI
	 */
	@Test
	public void checkExistingInstancesWithURI() {
		String expectedQuery = "SELECT DISTINCT ?instance ?instanceType WHERE {{ "
				+ "select (emf:testInstance as ?instance) ?instanceType"
				+ " where {emf:testInstance emf:instanceType ?instanceType . \n"
				+ "emf:testInstance emf:isDeleted \"false\"^^xsd:boolean . } }}";

		String query = service.buildQueryByName(NamedQueries.CHECK_EXISTING_INSTANCE,
				Arrays.asList(new Pair<>(NamedQueries.Params.URIS, Arrays.asList("emf:testInstance"))));

		assertEquals(expectedQuery, query);
	}

	/**
	 * Tests generation of the query checkExistingInstances without parameters. The expected result is null
	 */
	@Test
	public void checkExistingInstancesWithoutParameters() {
		String query = service.buildQueryByName(NamedQueries.CHECK_EXISTING_INSTANCE,
				Arrays.asList(new Pair<>(NamedQueries.Params.URIS, new ArrayList<>())));
		assertNull(query);
	}

	/**
	 * Tests generation of the query checkExistingInstances with multiple URIs
	 */
	@Test
	public void checkMultipleExistingInstances() {
		String expectedQuery = "SELECT DISTINCT ?instance ?instanceType WHERE {{ select (emf:testInstance as ?instance) ?instanceType "
				+ "where {emf:testInstance emf:instanceType ?instanceType . \n"
				+ "emf:testInstance emf:isDeleted \"false\"^^xsd:boolean . } }\n"
				+ "	 UNION{ select (emf:testInstance2 as ?instance) ?instanceType where {emf:testInstance2 emf:instanceType ?instanceType . \n"
				+ "emf:testInstance2 emf:isDeleted \"false\"^^xsd:boolean . } }}";

		String query = service.buildQueryByName(NamedQueries.CHECK_EXISTING_INSTANCE, Arrays
				.asList(new Pair<>(NamedQueries.Params.URIS, Arrays.asList("emf:testInstance", "emf:testInstance2"))));

		assertEquals(expectedQuery, query);
	}

	/**
	 * Tests generation of the query checkExistingInstances with multiple URIs
	 */
	@Test
	public void missingQuery() {
		String query = service.buildQueryByName("unknown query", Arrays
				.asList(new Pair<>(NamedQueries.Params.URIS, Arrays.asList("emf:testInstance", "emf:testInstance2"))));

		assertNull(query);
	}

	/**
	 * Tests query selectInstancesCallback
	 */
	@Test
	public void selectInstancesCallback() {
		String expectedQuery = "SELECT DISTINCT ?uri ?propertyName ?propertyValue"
				+ " WHERE { { ?uri ?propertyName ?propertyValue . ?uri emf:isDeleted \"false\"^^xsd:boolean . "
				+ "FILTER EXISTS { ?propertyName a emf:DefinitionObjectProperty. ?propertyValue emf:isDeleted \"false\"^^xsd:boolean . } } "
				+ "UNION { ?uri ?propertyName ?propertyValue . ?uri emf:isDeleted \"false\"^^xsd:boolean . "
				+ "FILTER EXISTS { ?propertyName a emf:DefinitionDataProperty. } } FILTER( ?uri = emf:testInstance || ?uri = <http://testInstance2> ).} ";
		String query = service.buildQueryByName(NamedQueries.SELECT_BY_IDS, Arrays.asList(
				new Pair<>(NamedQueries.Params.URIS, Arrays.asList("emf:testInstance", "http://testInstance2"))));

		assertEquals(expectedQuery.replaceAll("\\s+", " "), query.replaceAll("\\s+", " "));
	}

	/**
	 * Tests query loadPropertiesCallback
	 */
	@Test
	public void loadPropertiesCallback() {
		String expectedQuery = "SELECT DISTINCT ?uri ?propertyName ?propertyValue WHERE { \n" + "	 { \n"
				+ "		 SELECT DISTINCT (emf:testInstance as ?uri) ?propertyName ?propertyValue { \n"
				+ "			 emf:testInstance ?propertyName ?propertyValue . \n" + "		 } \n" + "	 }\n"
				+ "	 UNION	 { \n"
				+ "		 SELECT DISTINCT (emf:testInstance2 as ?uri) ?propertyName ?propertyValue { \n"
				+ "			 emf:testInstance2 ?propertyName ?propertyValue . \n" + "		 } \n" + "	 }\n" + "}";
		String query = service.buildQueryByName(NamedQueries.LOAD_PROPERTIES, Arrays
				.asList(new Pair<>(NamedQueries.Params.URIS, Arrays.asList("emf:testInstance", "emf:testInstance2"))));

		assertEquals(expectedQuery, query);
	}

	/**
	 * Tests query selectInstancesCallback with deleted instances
	 */
	@Test
	public void selectInstancesCallbackWithDeleted() {
		try {
			Options.ALLOW_LOADING_OF_DELETED_INSTANCES.enable();
			String expectedQuery = "SELECT DISTINCT ?uri ?propertyName ?propertyValue WHERE { { ?uri ?propertyName ?propertyValue . "
					+ "FILTER EXISTS { ?propertyName a emf:DefinitionObjectProperty. ?propertyValue emf:isDeleted \"false\"^^xsd:boolean . } } "
					+ "UNION { ?uri ?propertyName ?propertyValue . FILTER EXISTS { ?propertyName a emf:DefinitionDataProperty. } } "
					+ "FILTER( ?uri = emf:testInstance || ?uri = <http://testInstance2> ).} ";
			String query = service.buildQueryByName(NamedQueries.SELECT_BY_IDS, Arrays.asList(
					new Pair<>(NamedQueries.Params.URIS, Arrays.asList("emf:testInstance", "http://testInstance2"))));

			assertEquals(expectedQuery.replaceAll("\\s+", " "), query.replaceAll("\\s+", " "));
		} finally {
			Options.ALLOW_LOADING_OF_DELETED_INSTANCES.disable();
		}
	}

	@Test
	public void buildValueFilter() throws Exception {
		Function<String, String> function = service.buildValueFilter("emf:test", "value");
		assertNotNull(function);
		assertEquals("?instance emf:test \"value\". ", function.apply("?instance"));
		assertEquals("?subject emf:test \"value\". ", function.apply("?subject"));
		assertTrue(service.buildValueFilter("emf:test", "  ").apply("?instance").isEmpty());
	}

	@Test
	public void buildValuesFilter() throws Exception {
		TypeConverterImpl converter = new TypeConverterImpl();
		new ValueConverter().register(converter);
		TypeConverterUtil.setTypeConverter(converter);

		Function<String, String> function = service.buildValuesFilter("emf:test", Arrays.asList("value1", "value2"));
		assertNotNull(function);
		assertEquals(" { ?instance emf:test \"value1\".  } UNION { ?instance emf:test \"value2\".  } .",
				function.apply("?instance"));

		assertEquals("?instance emf:test \"value\". ",
				service.buildValuesFilter("emf:test", Arrays.asList("value")).apply("?instance"));

		function = service.buildValuesFilter("emf:test", Arrays.asList("value1", "value2", "  ", null, ""));
		assertEquals(" { ?instance emf:test \"value1\".  } UNION { ?instance emf:test \"value2\".  } .",
				function.apply("?instance"));
	}
}
