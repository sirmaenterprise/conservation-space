package com.sirma.itt.emf.semantic;

import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.CONNECTOR_NAME_CONSTANT;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.GeneralSemanticTest;
import com.sirma.itt.emf.mocks.search.SemanticSearchEngineMock;
import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.emf.semantic.search.SemanticSearchEngine;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.PropertyInstance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchDialects;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.facet.Facet;
import com.sirma.itt.seip.domain.search.facet.FacetQueryParameters;
import com.sirma.itt.seip.search.SearchQueryParameters;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirma.itt.semantic.TransactionalRepositoryConnection;

/**
 * Semantic search engine test - tests the search engine.
 *
 * @author kirq4e
 */
public class SemanticSearchEngineTest extends GeneralSemanticTest<SemanticSearchEngine> {

	@Mock
	private ValueFactory valueFactory;

	/**
	 * Initializes the {@link SemanticSearchEngine}.
	 */
	@BeforeClass
	public void init() {
		service = new SemanticSearchEngineMock(context);
		TypeConverter typeConverter = mock(TypeConverter.class);
		when(typeConverter.convert(eq(Value.class), any()))
				.then(a -> valueFactory.createLiteral(a.getArgumentAt(1, Serializable.class).toString()));
		TypeConverterUtil.setTypeConverter(typeConverter);
	}

	@Test
	public void testParseSearchRequest() {
		SearchArguments<Instance> searchArguments = new SearchArguments<>();
		SearchRequest request = new SearchRequest(CollectionUtils.createHashMap(1));
		String value = "Test";
		request.add(SearchQueryParameters.META_TEXT, value);
		request.add(SearchQueryParameters.FQ, value);
		request.add(SearchQueryParameters.MIMETYPE, value);
		request.add(SearchQueryParameters.IDENTIFIER, value);
		request.add(SearchQueryParameters.CREATED_BY, value);
		request.add(SearchQueryParameters.CREATED_FROM_DATE, value);
		request.add(SearchQueryParameters.CREATED_TO_DATE, value);
		request.add(SearchQueryParameters.OBJECT_TYPE, value);
		request.add(SearchQueryParameters.SUB_TYPE, value);
		request.add(SearchQueryParameters.SUB_TYPE, "emf:Case");
		request.add(DefaultProperties.SEMANTIC_TYPE, value);
		request.add(SearchQueryParameters.LOCATION, value);
		request.add(SearchQueryParameters.OBJECT_RELATIONSHIP, value);

		service.prepareSearchArguments(request, searchArguments);
		Assert.assertTrue(searchArguments.getArguments().containsKey("fts"));
		Assert.assertTrue(searchArguments.getArguments().containsKey("fq"));
		Assert.assertTrue(searchArguments.getArguments().containsKey("context"));
		Assert.assertTrue(searchArguments.getArguments().containsKey("relations"));
		Assert.assertTrue(searchArguments.getArguments().containsKey("emf:mimetype"));
		Assert.assertTrue(searchArguments.getArguments().containsKey("dcterms:identifier"));
		Assert.assertTrue(searchArguments.getArguments().containsKey("emf:createdOn"));
		Assert.assertTrue(searchArguments.getArguments().containsKey(DefaultProperties.SEMANTIC_TYPE));
		Assert.assertTrue(searchArguments.getArguments().containsKey("emf:createdBy"));
		Assert.assertTrue(searchArguments.getArguments().containsKey("emf:type"));

		searchArguments = new SearchArguments<>();
		request = new SearchRequest(CollectionUtils.createHashMap(1));
		request.add(SearchQueryParameters.QUERY_TEXT, value);
		service.prepareSearchArguments(request, searchArguments);
		Assert.assertTrue(searchArguments.getStringQuery().equals(value));

		searchArguments = new SearchArguments<>();
		request = new SearchRequest(CollectionUtils.createHashMap(1));
		request.add(SearchQueryParameters.QUERY_TEXT, value);
		service.prepareSearchArguments(request, searchArguments);
		Assert.assertTrue(searchArguments.getStringQuery().equals(value));

		searchArguments = new SearchArguments<>();
		request = new SearchRequest(CollectionUtils.createHashMap(1));
		request.setDialect(SearchDialects.SOLR);
		boolean isApplicable = service.prepareSearchArguments(request, searchArguments);
		Assert.assertFalse(isApplicable);

		isApplicable = service.prepareSearchArguments(null, null);
		Assert.assertFalse(isApplicable);

		request = new SearchRequest();
		isApplicable = service.prepareSearchArguments(request, null);
		Assert.assertFalse(isApplicable);
	}

	/**
	 * Verifies that the generated semantic query contains the values from the facets with selected values.
	 *
	 * @param uri
	 *            uri of the facet
	 * @param selectedValue
	 *            the selected facet value
	 * @param rangeClass
	 *            the range class of the facet (searchable property)
	 * @throws MalformedQueryException
	 *             the malformed query exception from
	 *             {@link TransactionalRepositoryConnection#prepareTupleQuery(QueryLanguage, String)}
	 * @throws QueryEvaluationException
	 *             the evaluation exception from {@link TupleQuery#evaluate()}
	 */
	@SuppressWarnings("unchecked")
	@Test(dataProvider = "facets")
	public void testParseFacetArguments(String uri, String selectedValue, String rangeClass)
			throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		SearchArguments<Instance> searchArguments = new SearchArguments<>();
		SearchRequest request = new SearchRequest(CollectionUtils.createHashMap(1));
		request.add(SearchQueryParameters.QUERY_FROM_DEFINITION, "definition");
		request.add(FacetQueryParameters.REQUEST_FACET_ARGUMENTS, "facetArguments");
		request.add(SearchQueryParameters.CREATED_FROM_DATE, "25.03.2016");
		request.add(SearchQueryParameters.CREATED_TO_DATE, "29.03.2016");
		service.prepareSearchArguments(request, searchArguments);

		Facet facet = new Facet();
		facet.setUri(uri);
		facet.setRangeClass(rangeClass);
		Set<String> selectedValues = CollectionUtils.createHashSet(1);
		selectedValues.add(selectedValue);
		facet.setSelectedValues(selectedValues);

		searchArguments.setFacetsWithSelectedValues(Arrays.asList(facet));

		ArgumentCaptor<String> query = ArgumentCaptor.forClass(String.class);

		// Mock the connection.
		javax.enterprise.inject.Instance<TransactionalRepositoryConnection> connectionInstance = Mockito
				.mock(javax.enterprise.inject.Instance.class);

		@SuppressWarnings("resource")
		TransactionalRepositoryConnection connection = mock(TransactionalRepositoryConnection.class);
		ReflectionUtils.setField(service, "connection", connectionInstance);
		when(connectionInstance.get()).thenReturn(connection);

		// Mock the tuple query and the results. We don't really need any results so those methods are sufficient.
		TupleQuery tupleQuery = mock(TupleQuery.class);
		TupleQueryResult tupleQueryResult = mock(TupleQueryResult.class);

		when(tupleQuery.getIncludeInferred()).thenReturn(true);
		when(tupleQuery.evaluate()).thenReturn(tupleQueryResult);
		// We will capture the query that will be passed to the semantic db and check that it contains the selected
		// facet values.
		when(connection.prepareTupleQuery(Matchers.any(QueryLanguage.class), query.capture())).thenReturn(
				tupleQuery);

		service.search(Instance.class, searchArguments);
		Assert.assertTrue(query.getValue().contains(uri));
	}

	/**
	 * Tests setting of connector name for partial query
	 *
	 * @throws Exception
	 */
	@Test
	public void testSetConnectorNameForPartialQuery() throws Exception {
		String query = "?instance emf:type \"TESTDEF\"";
		SearchArguments<Instance> searchArguments = new SearchArguments<>();
		searchArguments.setStringQuery(query);
		Map<String, Serializable> arguments = new HashMap<>();
		arguments.put("fts", "test");
		searchArguments.setArguments(arguments);

		mockRepositoryConnection();

		service.search(Instance.class, searchArguments);
	}

	/**
	 * Tests setting of connector name for full query
	 *
	 * @throws Exception
	 */
	@Test
	public void testSetConnectorNameForFullQuery() throws Exception {
		String query = "SELECT ?instance where { ?instance emf:type \"TESTDEF\" . ?search a " + CONNECTOR_NAME_CONSTANT
				+ " ; solr:query \"*:* ; solr:entities ?instance . " + "}";
		SearchArguments<Instance> searchArguments = new SearchArguments<>();
		searchArguments.setStringQuery(query);

		mockRepositoryConnection();

		service.search(Instance.class, searchArguments);
	}

	@Test
	public void testAggregation() throws Exception {
		SearchArguments<Instance> searchArguments = new SearchArguments<>();
		searchArguments.setShouldGroupBy(true);
		searchArguments.setGroupBy(Arrays.asList("emf:dataProperty"));

		BindingSetMock set = new BindingSetMock();
		set.setValue("name", "APPROVED", XMLSchema.STRING).setValue("count", 123, XMLSchema.INT);
		mockRepositoryConnectionForAggregatedResults(set);

		service.search(Instance.class, searchArguments);

		Assert.assertNotNull(searchArguments.getAggregatedData());
		Map<String, Serializable> aggregatedProperty = searchArguments.getAggregatedData().get("emf:dataProperty");
		Assert.assertNotNull(aggregatedProperty);
		Assert.assertTrue(aggregatedProperty.containsKey("APPROVED"));
		Assert.assertEquals(aggregatedProperty.get("APPROVED"), 123);
	}

	@Test
	public void testAggregationForURIProperties() throws Exception {
		SearchArguments<Instance> searchArguments = new SearchArguments<>();
		searchArguments.setShouldGroupBy(true);
		searchArguments.setGroupBy(Arrays.asList("emf:objectProperty"));

		BindingSetMock set = new BindingSetMock();
		String nameSpace = "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#";
		set.setURIValue("name", nameSpace, "123-456").setValue("count", 123, XMLSchema.INT);
		mockRepositoryConnectionForAggregatedResults(set);

		service.search(Instance.class, searchArguments);

		Assert.assertNotNull(searchArguments.getAggregatedData());
		Map<String, Serializable> aggregatedProperty = searchArguments.getAggregatedData().get("emf:objectProperty");
		Assert.assertNotNull(aggregatedProperty);
		Assert.assertTrue(aggregatedProperty.containsKey("emf:123-456"));
		Assert.assertEquals(aggregatedProperty.get("emf:123-456"), 123);
	}

	@Test
	public void testGroupByArgumentsPreparation() {
		SearchArguments<Instance> searchArguments = new SearchArguments<>();
		SearchRequest request = new SearchRequest(CollectionUtils.createHashMap(1));
		request.add(SearchQueryParameters.GROUP_BY, "emf:property");

		service.prepareSearchArguments(request, searchArguments);

		Assert.assertTrue(searchArguments.shouldGroupBy());
		Assert.assertNotNull(searchArguments.getGroupBy());
		Assert.assertEquals(searchArguments.getGroupBy().size(), 1);
		Assert.assertEquals(searchArguments.getGroupBy().iterator().next(), "emf:property");
	}
	
	@Test
	public void testGroupByManuallySelectedArgumentsPreparation() {
		SearchArguments<Instance> searchArguments = new SearchArguments<>();
		SearchRequest request = new SearchRequest(CollectionUtils.createHashMap(1));
		request.add(SearchQueryParameters.GROUP_BY, "emf:property");
		request.add(SearchQueryParameters.SELECTED_OBJECTS, "test1");
		request.add(SearchQueryParameters.SELECTED_OBJECTS, "test2");
		request.add(SearchQueryParameters.SELECTED_OBJECTS, "test3");

		service.prepareSearchArguments(request, searchArguments);

		Assert.assertTrue(searchArguments.shouldGroupBy());
		Assert.assertNotNull(searchArguments.getGroupBy());
		Assert.assertEquals(searchArguments.getGroupBy().size(), 1);
		Assert.assertEquals(searchArguments.getGroupBy().iterator().next(), "emf:property");

		Assert.assertEquals(searchArguments.getCondition().getRules().size(), 1);
		Assert.assertEquals(searchArguments.getStringQuery(), "test query");

	}
	private void mockRepositoryConnection() throws RepositoryException, MalformedQueryException {
		TransactionalRepositoryConnection repositoryConnection = mock(TransactionalRepositoryConnection.class);
		when(repositoryConnection.prepareTupleQuery(eq(QueryLanguage.SPARQL), Matchers.anyString()))
				.then(mockEmptyAnswer());
		ReflectionUtils.setField(service, "connection", new InstanceProxyMock<>(repositoryConnection));
	}
	/**
	 * Mocks the semantic repository to first return aggregated results and then to simulate empty on second invocation.
	 * @param set - the set to return from the mocked connection
	 */
	private void mockRepositoryConnectionForAggregatedResults(BindingSet set) throws RepositoryException, MalformedQueryException {
		TransactionalRepositoryConnection repositoryConnection = mock(TransactionalRepositoryConnection.class);
		when(repositoryConnection.prepareTupleQuery(eq(QueryLanguage.SPARQL), Matchers.anyString()))
				.then(new Answer<TupleQuery>() {
					@Override
					public TupleQuery answer(InvocationOnMock invocation) throws Throwable {
						TupleQuery query = mock(TupleQuery.class);
						TupleQueryResult result = mock(TupleQueryResult.class);
						when(result.hasNext()).thenReturn(true, true, false);
						when(result.next()).thenReturn(set);
						when(query.evaluate()).thenReturn(result);
						return query;
					}
				});
		ReflectionUtils.setField(service, "connection", new InstanceProxyMock<>(repositoryConnection));
		
		SemanticDefinitionService definitionService = mock(SemanticDefinitionService.class);
		when(definitionService.getRelation(Mockito.eq("emf:dataProperty"))).thenReturn(null);
		
		PropertyInstance objectPropertyInstance = new PropertyInstance();
		objectPropertyInstance.setId("emf:objectProperty");
		when(definitionService.getRelation(eq("emf:objectProperty"))).thenReturn(objectPropertyInstance);
		
		ReflectionUtils.setField(service, "semanticDefinitionService", definitionService);
	}

	private Answer<TupleQuery> mockEmptyAnswer() {
		return new Answer<TupleQuery>() {
			@Override
			public TupleQuery answer(InvocationOnMock invocation) throws Throwable {
				String queryString = invocation.getArgumentAt(1, String.class);
				System.out.println(queryString);
				Assert.assertTrue(queryString.contains("solr:ftsearch"));
				Assert.assertFalse(queryString.contains(CONNECTOR_NAME_CONSTANT));
				TupleQuery query = mock(TupleQuery.class);
				TupleQueryResult result = mock(TupleQueryResult.class);
				when(result.hasNext()).thenReturn(false);
				when(query.evaluate()).thenReturn(result);
				return query;
			}
		};
	}

	@DataProvider(name = "facets", parallel = false)
	public Object[][] prepareFacets() {
		List<Object[]> testData = new ArrayList<>();
		Object[] facetData = new Object[3];
		facetData[0] = "emf:test1";
		facetData[1] = null;
		facetData[2] = null;
		testData.add(facetData);

		facetData = new Object[3];
		facetData[0] = SPARQLQueryHelper.RDF_TYPE;
		facetData[1] = "emf:Case";
		facetData[2] = null;
		testData.add(facetData);

		facetData = new Object[3];
		facetData[0] = SPARQLQueryHelper.EMF_TYPE;
		facetData[1] = "TSTTEST";
		facetData[2] = null;
		testData.add(facetData);

		facetData = new Object[3];
		facetData[0] = "emf:createdOn";
		facetData[1] = "2016-03-23;2016-03-29";
		facetData[2] = "dateTime";
		testData.add(facetData);

		facetData = new Object[3];
		facetData[0] = "emf:modifiedOn";
		facetData[1] = "*;2015-10-12";
		facetData[2] = "date";
		testData.add(facetData);

		facetData = new Object[3];
		facetData[0] = "emf:isSearchable";
		facetData[1] = "true";
		facetData[2] = "boolean";
		testData.add(facetData);

		facetData = new Object[3];
		facetData[0] = "emf:priority";
		facetData[1] = SPARQLQueryHelper.NO_VALUE;
		facetData[2] = "string";
		testData.add(facetData);

		facetData = new Object[3];
		facetData[0] = "emf:priority";
		facetData[1] = "HIGH";
		facetData[2] = "string";
		testData.add(facetData);

		return testData.stream().toArray(Object[][]::new);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getTestDataFile() {
		return "SemanticSearchEngineTest.trig";
	}

	/**
	 * Helper class used in the tests to mock {@link BindingSet}.
	 *
	 * @author Mihail Radkov
	 */
	private static class BindingSetMock implements BindingSet {

		private final Map<String, Value> bindings;

		/**
		 * Constructs new mock class.
		 */
		public BindingSetMock() {
			this.bindings = CollectionUtils.createHashMap(2);
		}

		/**
		 * Stores the provided data in the inner map as {@link Value}.
		 *
		 * @param key
		 * 		- the key of the value
		 * @param value
		 * 		- the value
		 * @param dataType
		 * 		- the value's type
		 * @return - the current instance of the mock for chaining
		 */
		public BindingSetMock setValue(String key, Serializable value, URI dataType) {
			LiteralValueMock valueMock = new LiteralValueMock(value, dataType);
			this.bindings.put(key, valueMock);
			return this;
		}

		public BindingSetMock setURIValue(String key, String nameSpace, String localName) {
			URIValueMock valueMock = new URIValueMock(nameSpace, localName);
			this.bindings.put(key, valueMock);
			return this;
		}

		@Override
		public Iterator<Binding> iterator() {
			return null;
		}

		@Override
		public Set<String> getBindingNames() {
			return null;
		}

		@Override
		public Binding getBinding(String s) {
			return null;
		}

		@Override
		public boolean hasBinding(String s) {
			return false;
		}

		@Override
		public Value getValue(String s) {
			return bindings.get(s);
		}

		@Override
		public int size() {
			return 1;
		}
	}

	/**
	 * Helper class mocking semantic literal value for test usage.
	 *
	 * @author Mihail Radkov
	 */
	private static class LiteralValueMock implements Value, Literal {

		private final Serializable value;
		private final URI dataType;

		/**
		 * Initialises new instance of a mock value with the provided data.
		 *
		 * @param value
		 * 		- the value
		 * @param dataType
		 * 		- the value's type
		 */
		public LiteralValueMock(Serializable value, URI dataType) {
			this.value = value;
			this.dataType = dataType;
		}

		@Override
		public String stringValue() {
			return (String) value;
		}

		@Override
		public URI getDatatype() {
			return dataType;
		}

		@Override
		public String getLabel() {
			return null;
		}

		@Override
		public String getLanguage() {
			return null;
		}

		@Override
		public byte byteValue() {
			return 0;
		}

		@Override
		public short shortValue() {
			return 0;
		}

		@Override
		public int intValue() {
			return (Integer) value;
		}

		@Override
		public long longValue() {
			return 0;
		}

		@Override
		public BigInteger integerValue() {
			return null;
		}

		@Override
		public BigDecimal decimalValue() {
			return null;
		}

		@Override
		public float floatValue() {
			return 0;
		}

		@Override
		public double doubleValue() {
			return 0;
		}

		@Override
		public boolean booleanValue() {
			return false;
		}

		@Override
		public XMLGregorianCalendar calendarValue() {
			return null;
		}
	}

	private static class URIValueMock implements Value, URI {

		private final String nameSpace;
		private final String localName;

		/**
		 * Initialises new instance of a mock URI value with the provided data.
		 *
		 */
		public URIValueMock(String nameSpace, String localName) {
			this.nameSpace = nameSpace;
			this.localName = localName;
		}

		@Override
		public String getNamespace() {
			return nameSpace;
		}

		@Override
		public String getLocalName() {
			return localName;
		}

		@Override
		public String stringValue() {
			return null;
		}

	}
}
