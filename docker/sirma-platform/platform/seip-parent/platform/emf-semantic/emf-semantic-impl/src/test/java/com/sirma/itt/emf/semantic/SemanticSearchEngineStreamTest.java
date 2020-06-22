package com.sirma.itt.emf.semantic;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.emf.GeneralSemanticTest;
import com.sirma.itt.emf.mocks.search.SemanticSearchEngineMock;
import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.emf.semantic.search.SemanticSearchEngine;
import com.sirma.itt.emf.semantic.search.TupleQueryResultIterator;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.Sorter;
import com.sirma.itt.seip.search.ResultItem;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * Semantic search engine test - tests the search engine streaming capability. It's separate test as the tests for
 * aggregations mess with the actual repository connections
 *
 * @author kirq4e
 */
public class SemanticSearchEngineStreamTest extends GeneralSemanticTest<SemanticSearchEngine> {

	private ValueFactory valueFactory = SimpleValueFactory.getInstance();

	/**
	 * Initializes the {@link SemanticSearchEngine}.
	 */
	@BeforeClass
	public void init() {
		SemanticDefinitionService semanticDefinitionService = mock(SemanticDefinitionService.class);
		context.put("semanticDefinitionService", semanticDefinitionService);
		service = new SemanticSearchEngineMock(context);
		TypeConverter typeConverter = mock(TypeConverter.class);
		when(typeConverter.convert(eq(Value.class), any()))
				.then(a -> valueFactory.createLiteral(a.getArgumentAt(1, Serializable.class).toString()));
		TypeConverterUtil.setTypeConverter(typeConverter);
	}

	@BeforeMethod
	@Override
	public void beforeMethod() {
		super.beforeMethod();
		noTransaction();
	}

	@Test
	public void searchStreaming_ShouldStreamAllObjectProperties() {
		RepositoryConnection connection = connectionFactory.produceReadOnlyConnection();
		int count = 0;

		TupleQuery query = SPARQLQueryHelper.prepareTupleQuery(connection,
				"SELECT (count(?property) as ?count) where { ?property a owl:ObjectProperty . }",
				CollectionUtils.emptyMap(), true);
		try (TupleQueryResultIterator result = new TupleQueryResultIterator(query.evaluate())) {
			count = ((Literal) result.next().getBinding("count").getValue()).intValue();
		} finally {
			connectionFactory.disposeConnection(connection);
		}

		SearchArguments<Instance> arguments = new SearchArguments<>();
		arguments.setStringQuery("SELECT * where { ?property a owl:ObjectProperty . }");
		arguments.setPermissionsType(SearchArguments.QueryResultPermissionFilter.NONE);
		arguments.setMaxSize(-1);
		try (Stream<ResultItem> stream = service.stream(arguments)) {
			List<Serializable> properties = stream.map(item -> item.getResultValue("property"))
					.collect(Collectors.toList());
			Assert.assertEquals(properties.size(), count);
		}
	}

	@Test
	public void searchStreaming_ShouldHandleGroupBy() {
		int expectedSearchableClasses = 0;
		int expectedNotSearchableClasses = 0;

		try (RepositoryConnection connection = connectionFactory.produceReadOnlyConnection()) {

			String queryString = "SELECT (count(?instance) as ?countSearchable) WHERE { ?instance  emf:isSearchable \"searchableParam\"^^xsd:boolean  {select distinct ?instance where { ?instance a owl:Class  }}}";

			// get searchable classes count
			try (TupleQueryResult result = SPARQLQueryHelper.prepareTupleQuery(connection,
					queryString.replace("searchableParam", "true"), CollectionUtils.emptyMap(), true).evaluate()) {
				if (result.hasNext()) {
					Literal value = (Literal) result.next().getBinding("countSearchable").getValue();
					expectedSearchableClasses = value.intValue();
				}
			}

			// get NOT searchable classes count
			try (TupleQueryResult result = SPARQLQueryHelper.prepareTupleQuery(connection,
					queryString.replace("searchableParam", "false"), CollectionUtils.emptyMap(), true).evaluate()) {
				if (result.hasNext()) {
					Literal value = (Literal) result.next().getBinding("countSearchable").getValue();
					expectedNotSearchableClasses = value.intValue();
				}
			}
		}

		SearchArguments<Instance> arguments = new SearchArguments<>();
		arguments.setStringQuery("SELECT DISTINCT ?instance WHERE { ?instance a owl:Class.\n }");
		arguments.setPermissionsType(SearchArguments.QueryResultPermissionFilter.NONE);
		arguments.setGroupBy(Collections.singletonList("emf:isSearchable"));
		arguments.setShouldGroupBy(true);
		arguments.setMaxSize(-1);
		try (Stream<ResultItem> stream = service.stream(arguments)) {
			Map<String, Map<String, Integer>> groupBy = stream.collect(Collectors.groupingBy(ResultItem::getGroupBy,
					Collectors.toMap(ResultItem::getGroupByValue, ResultItem::getGroupByCount)));
			Assert.assertEquals(groupBy.size(), 1);
			Map<String, Integer> isSearchable = groupBy.get("emf:isSearchable");
			Assert.assertEquals(isSearchable.get("false"), Integer.valueOf(expectedNotSearchableClasses));
			Assert.assertEquals(isSearchable.get("true"), Integer.valueOf(expectedSearchableClasses));
		}
	}

	@Test
	public void searchStreaming_ShouldReturnEmptyStreamIfNothingIsFound() {
		SearchArguments<Instance> arguments = new SearchArguments<>();
		arguments.setStringQuery("SELECT * where { ?property emf:createdBy emf:misho . }");
		arguments.setPermissionsType(SearchArguments.QueryResultPermissionFilter.NONE);
		arguments.setMaxSize(-1);
		try (Stream<ResultItem> stream = service.stream(arguments)) {
			List<Serializable> properties = stream.map(item -> item.getResultValue("property"))
					.collect(Collectors.toList());
			Assert.assertEquals(properties.size(), 0);
		}
	}

	@Test
	public void searchStreaming_SortingInSemanticDbShouldBeTheSameAsInHouse() throws Exception {

		String notSorted = doSearch(false);

		setPropertyAsIgnoreCaseSortable("rdfs:comment");

		String withGDbSorting = doSearch(true);

		Assert.assertNotEquals(notSorted, withGDbSorting, "Graph DB sorting does not sort anything");

		enableInHouseSorting();

		String withInHouseSorting = doSearch(true);

		Assert.assertNotEquals(notSorted, withInHouseSorting, "In house sorting does not sort anything");

		Assert.assertEquals(withGDbSorting, withInHouseSorting, "In house sorting differs from graph DB sorting");
	}

	private String doSearch(boolean sorted) {
		SearchArguments<Instance> arguments = new SearchArguments<>();
		arguments.setStringQuery("SELECT ?instance ?comment where { ?instance a owl:ObjectProperty ."
				+ " ?instance rdfs:comment ?comment }");
		arguments.setPermissionsType(SearchArguments.QueryResultPermissionFilter.NONE);
		arguments.setMaxSize(-1);
		if (sorted) {
			arguments.addSorter(Sorter.ascendingSorter("rdfs:comment"));
		}
		try (Stream<ResultItem> stream = service.stream(arguments)) {
			AtomicInteger count = new AtomicInteger();
			return stream.map(item -> "" + count.getAndIncrement() + ". " + item.getResultValue("comment"))
					.collect(Collectors.joining("\n"));
		}
	}

	private void setPropertyAsIgnoreCaseSortable(String property) throws Exception {
		Field field = SemanticSearchEngine.class.getDeclaredField("configurations");
		field.setAccessible(true);
		SemanticSearchConfigurations configurations = (SemanticSearchConfigurations) ReflectionUtils
				.getFieldValue((field), service);
		ConfigurationPropertyMock<String> sortResultsInGdb = (ConfigurationPropertyMock<String>) configurations
				.getListOfCaseInsensitiveProperties();
		sortResultsInGdb.setValue(property);
	}

	private void enableInHouseSorting() throws Exception {
		Field field = SemanticSearchEngine.class.getDeclaredField("configurations");
		field.setAccessible(true);
		SemanticSearchConfigurations configurations = (SemanticSearchConfigurations) ReflectionUtils
				.getFieldValue((field), service);
		ConfigurationPropertyMock<Boolean> sortResultsInGdb = (ConfigurationPropertyMock<Boolean>) configurations
				.getSortResultsInGdb();
		sortResultsInGdb.setValue(Boolean.FALSE);
	}

	@Override
	protected String getTestDataFile() {
		return "SemanticSearchEngineTest.trig";
	}
}
