package com.sirma.itt.emf.semantic.search;

import static com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper.SORT_VARIABLE_SUFFIX;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.TupleQueryResultHandler;
import org.eclipse.rdf4j.query.TupleQueryResultHandlerException;
import org.eclipse.rdf4j.query.impl.AbstractQuery;
import org.eclipse.rdf4j.query.impl.MapBindingSet;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sirma.itt.emf.semantic.SemanticSearchConfigurations;
import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.Sorter;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.model.DataType;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.configuration.SemanticConfiguration;

/**
 * Test for {@link SemanticSearchEngine}.
 *
 * @author BBonev
 */
public class SemanticSearchEngineTest {

	private static final Random RANDOM = new SecureRandom();

	@InjectMocks
	private SemanticSearchEngine searchEngine;

	@Mock
	private RepositoryConnection repositoryConnection;
	@Mock
	private NamespaceRegistryService namespaceRegistryService;
	@Spy
	private SecurityContextManager securityContextManager = new SecurityContextManagerFake();
	@Mock
	private TypeConverter typeConverter;
	@Mock
	private DefinitionService definitionService;
	@Mock
	private Statistics statistics;
	@Mock
	private SemanticConfiguration semanticConfiguration;
	@Mock
	private SemanticSearchConfigurations configurations;
	@Mock
	private AuthorityService authorityService;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private SemanticDefinitionService semanticDefinitionService;
	@Mock
	private CodelistService codelistService;

	private Map<String, Function<String, Value>> valueProducers = new HashMap<>();

	@BeforeMethod
	public void beforeMethod() throws RepositoryException, MalformedQueryException {
		MockitoAnnotations.initMocks(this);
		when(namespaceRegistryService.buildFullUri(anyString())).then(a -> a.getArgumentAt(0, String.class));
		when(namespaceRegistryService.getShortUri(any(IRI.class)))
				.then(a -> String.valueOf(a.getArgumentAt(0, Object.class)));
		when(definitionService.getDataTypeDefinition("instance")).then(a -> {
			DataType type = new DataType();
			type.setName(a.getArgumentAt(0, String.class));
			type.setJavaClassName(ObjectInstance.class.getName());
			return type;
		});
		TypeConverterUtil.setTypeConverter(typeConverter);
		when(typeConverter.convert(Class.class, ObjectInstance.class.getName())).thenReturn(ObjectInstance.class);
		TypeConverterUtil.setTypeConverter(typeConverter);
		when(statistics.createTimeStatistics(any(), anyString())).then(a -> new TimeTracker());
		when(semanticConfiguration.getFtsIndexName()).thenReturn(new ConfigurationPropertyMock<>("FTS"));
		when(configurations.getForbiddenRoleUri())
				.thenReturn(new ConfigurationPropertyMock<>("conc:SecurityRoleTypes-Forbidden"));
		when(configurations.getWriteRoleUri())
				.thenReturn(new ConfigurationPropertyMock<>("conc:SecurityRoleTypes-Read-Write"));
		when(configurations.getListOfCaseInsensitiveProperties())
				.thenReturn(new ConfigurationPropertyMock<>("dcterms:title"));
		when(configurations.getIgnoreInstanceTypes()).thenReturn(new ConfigurationPropertyMock<>(
				"sectioninstance, classinstance, commoninstance, topicinstance, commentinstance, user, group"));
		EmfUser user = new EmfUser();
		user.setId("emf:test");
		when(securityContext.getEffectiveAuthentication()).thenReturn(user);
		SPARQLQueryHelper.setNamespaceRegistryService(namespaceRegistryService);
		SPARQLQueryHelper.setCaseInsenitiveOrderByList("");
		valueProducers.clear();
		searchEngine.init();
	}

	@Test(dataProvider = "testPaginationProvider")
	public void test_pagination(Integer page, Integer pageSize, Integer total, Boolean inParallel, Boolean sortInDb)
			throws QueryEvaluationException, RepositoryException, MalformedQueryException {
		int aPage = page.intValue();
		int aPageSize = pageSize.intValue();

		SearchArguments<Instance> arguments = createArgs(aPage, aPageSize);

		valueProducers.put("prop1", name -> SimpleValueFactory.getInstance().createLiteral(name + RANDOM.nextInt()));
		valueProducers.put("prop2", name -> generateDate());

		when(repositoryConnection.prepareTupleQuery(eq(QueryLanguage.SPARQL), anyString()))
				.then(a -> new TupleQueryStub(a.getArgumentAt(1, String.class), Arrays.asList("prop1", "prop2")));

		setSearchInParallel(inParallel.booleanValue());
		setSortInDb(sortInDb.booleanValue());

		searchEngine.search(Instance.class, arguments);

		List<Instance> result = arguments.getResult();
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertEquals(result.size(), aPageSize);
		assertEquals(arguments.getTotalItems(), total.intValue());
		assertEquals(result.get(0).getId(), buildUri(1 + (aPage - 1) * aPageSize));
		assertEquals(result.get(result.size() - 1).getId(), buildUri(aPageSize * aPage));
	}

	@Test
	public void testSorting() {
		Sorter sorter = new Sorter("emf:modifiedBy", false);
		Comparator<Instance> propertyComparator = SemanticSearchEngine.createPropertyComparator(sorter);

		Instance instance1 = new EmfInstance();
		instance1.setId("1");
		instance1.add(SORT_VARIABLE_SUFFIX, "emf:xavier");

		Instance instance2 = new EmfInstance();
		instance2.setId("2");
		instance2.add(SORT_VARIABLE_SUFFIX, "emf:someone");

		Instance instance3 = new EmfInstance();
		instance3.setId("3");
		instance3.add(SORT_VARIABLE_SUFFIX, "emf:admin");

		Instance instance4 = new EmfInstance();
		instance4.setId("4");
		instance4.add(SORT_VARIABLE_SUFFIX, "emf:System");

		List<Instance> instances = Arrays.asList(instance1, instance2, instance3, instance4);
		List<Instance> sortedInstances = instances.stream().sorted(propertyComparator).collect(Collectors.toList());

		Assert.assertEquals(sortedInstances.get(0).getId(), instance1.getId());
		Assert.assertEquals(sortedInstances.get(1).getId(), instance4.getId());
		Assert.assertEquals(sortedInstances.get(2).getId(), instance2.getId());
		Assert.assertEquals(sortedInstances.get(3).getId(), instance3.getId());
	}

	@Test
	public void testComparableSorting() {
		Sorter sorter = new Sorter("emf:isActive", false);
		Comparator<Instance> propertyComparator = SemanticSearchEngine.createPropertyComparator(sorter);

		Instance instance1 = new EmfInstance();
		instance1.setId("1");
		instance1.add(SORT_VARIABLE_SUFFIX, Boolean.TRUE);

		Instance instance2 = new EmfInstance();
		instance2.setId("2");
		instance2.add(SORT_VARIABLE_SUFFIX, Boolean.TRUE);

		Instance instance3 = new EmfInstance();
		instance3.setId("3");
		instance3.add(SORT_VARIABLE_SUFFIX, Boolean.FALSE);

		Instance instance4 = new EmfInstance();
		instance4.setId("4");
		instance4.add(SORT_VARIABLE_SUFFIX, Boolean.TRUE);

		List<Instance> instances = Arrays.asList(instance1, instance2, instance3, instance4);
		List<Instance> sortedInstances = instances.stream().sorted(propertyComparator).collect(Collectors.toList());

		Assert.assertEquals(sortedInstances.get(0).getId(), instance1.getId());
		Assert.assertEquals(sortedInstances.get(1).getId(), instance2.getId());
		Assert.assertEquals(sortedInstances.get(2).getId(), instance4.getId());
		Assert.assertEquals(sortedInstances.get(3).getId(), instance3.getId());
	}

	@Test
	public void testNullSorting() {
		Sorter sorter = new Sorter("emf:isActive", false);
		Comparator<Instance> propertyComparator = SemanticSearchEngine.createPropertyComparator(sorter);

		Instance instance1 = new EmfInstance();
		instance1.setId("1");
		instance1.add(SORT_VARIABLE_SUFFIX, Boolean.TRUE);

		Instance instance2 = new EmfInstance();
		instance2.setId("2");
		instance2.add(SORT_VARIABLE_SUFFIX, null);

		Instance instance3 = new EmfInstance();
		instance3.setId("3");
		instance3.add(SORT_VARIABLE_SUFFIX, Boolean.FALSE);

		Instance instance4 = new EmfInstance();
		instance4.setId("4");
		instance4.add(SORT_VARIABLE_SUFFIX, null);

		List<Instance> instances = Arrays.asList(instance1, instance2, instance3, instance4);
		List<Instance> sortedInstances = instances.stream().sorted(propertyComparator).collect(Collectors.toList());

		// Null values should be last
		Assert.assertEquals(sortedInstances.get(0).getId(), instance1.getId());
		Assert.assertEquals(sortedInstances.get(1).getId(), instance3.getId());
		Assert.assertEquals(sortedInstances.get(2).getId(), instance2.getId());
		Assert.assertEquals(sortedInstances.get(3).getId(), instance4.getId());

		sorter = new Sorter("emf:isActive", true);
		propertyComparator = SemanticSearchEngine.createPropertyComparator(sorter);
		sortedInstances = instances.stream().sorted(propertyComparator).collect(Collectors.toList());

		// Null values should be last again
		Assert.assertEquals(sortedInstances.get(0).getId(), instance3.getId());
		Assert.assertEquals(sortedInstances.get(1).getId(), instance1.getId());
		Assert.assertEquals(sortedInstances.get(2).getId(), instance2.getId());
		Assert.assertEquals(sortedInstances.get(3).getId(), instance4.getId());
	}

	@Test
	public void testSortingByCodelistValue() {
		SearchArguments<Instance> arguments = new SearchArguments<>();
		arguments.setMaxSize(4);
		arguments.setStringQuery("someQuery");

		arguments.addSorter(new Sorter("emf:status", true, Arrays.asList(2)));

		Map<String, CodeValue> statuses = CollectionUtils.createHashMap(4);
		CodeValue codeValue = new CodeValue();
		codeValue.setCodelist(2);
		codeValue.setIdentifier("0");
		codeValue.getOrCreateProperties().put("description", "Created");
		statuses.put("0", codeValue);

		codeValue = new CodeValue();
		codeValue.setCodelist(2);
		codeValue.setIdentifier("1");
		codeValue.getOrCreateProperties().put("description", "Started");
		statuses.put("1", codeValue);

		codeValue = new CodeValue();
		codeValue.setCodelist(2);
		codeValue.setIdentifier("2");
		codeValue.getOrCreateProperties().put("description", "Initial");
		statuses.put("2", codeValue);

		codeValue = new CodeValue();
		codeValue.setCodelist(2);
		codeValue.setIdentifier("3");
		codeValue.getOrCreateProperties().put("description", "Completed");
		statuses.put("3", codeValue);

		valueProducers.put("sort",
				name -> SimpleValueFactory.getInstance().createLiteral("" + (int) (Math.random() * 4)));

		when(repositoryConnection.prepareTupleQuery(eq(QueryLanguage.SPARQL), anyString()))
				.then(a -> new TupleQueryStub(a.getArgumentAt(1, String.class), Arrays.asList("sort")));

		when(codelistService.getCodeValues(Matchers.eq(2))).then(a -> statuses);

		when(codelistService.getDescription(any()))
				.then(a -> a.getArgumentAt(0, CodeValue.class).getProperties().get("description"));

		setSearchInParallel(false);
		setSortInDb(false);

		searchEngine.search(Instance.class, arguments);

		Assert.assertEquals(arguments.getResult().size(), 4);
		List<Instance> result = arguments.getResult();

		List<String> resultSortValues = CollectionUtils.transformToList(result, instance -> instance.get("sort").toString());
		List<String> sortedResult = new ArrayList<>(resultSortValues);
		Collections.sort(sortedResult);

		Assert.assertEquals(resultSortValues, sortedResult);
	}

	private static Value generateDate() {
		return SimpleValueFactory.getInstance().createLiteral(RANDOM.nextLong());
	}

	@SuppressWarnings("static-method")
	@DataProvider(name = "testPaginationProvider", parallel = false)
	public Object[][] testPaginationProvider() {
		return new Object[][] { { 1, 25, 1000, true, true }, { 2, 25, 1000, true, true }, { 3, 33, 1000, true, true },
				{ 5, 10, 1000, true, true }, { 5, 25, 1000, true, true }, { 11, 100, 1000, true, true },

				{ 1, 25, 1000, true, false }, { 2, 25, 1000, true, false }, { 3, 33, 1000, true, false },
				{ 5, 10, 1000, true, false }, { 5, 25, 1000, true, false }, { 11, 100, 1000, true, false },

				{ 1, 25, 1000, false, true }, { 2, 25, 1000, false, true }, { 3, 33, 1000, false, true },
				{ 5, 10, 1000, false, true }, { 5, 25, 1000, false, true }, { 11, 100, 1000, false, true },

				{ 1, 25, 1000, false, false }, { 2, 25, 1000, false, false }, { 3, 33, 1000, false, false },
				{ 5, 10, 1000, false, false }, { 5, 25, 1000, false, false }, { 11, 100, 1000, false, false } };
	}

	private static SearchArguments<Instance> createArgs(int page, int pageSize) {
		SearchArguments<Instance> arguments = new SearchArguments<>();
		arguments.setStringQuery("someQuery");
		arguments.setPageNumber(page);
		arguments.setPageSize(pageSize);
		arguments.addSorter(Sorter.ascendingSorter("sortField"));
		return arguments;
	}

	void setSearchInParallel(boolean searchParalell) {
		when(configurations.getProcessResultsInParallel()).thenReturn(new ConfigurationPropertyMock<>(searchParalell));
	}

	void setSortInDb(boolean sortInDb) {
		when(configurations.getSortResultsInGdb()).thenReturn(new ConfigurationPropertyMock<>(sortInDb));
	}

	private BindingSet generateResult(int current, List<String> bindingNames) {
		MapBindingSet bindingSet = new MapBindingSet();
		for (String name : bindingNames) {
			bindingSet.addBinding(name, valueProducers.get(name).apply(name));
		}
		bindingSet.addBinding("instance", SimpleValueFactory.getInstance().createIRI(buildUri(current)));
		bindingSet.addBinding("instanceType", SimpleValueFactory.getInstance().createLiteral("instance"));
		return bindingSet;
	}

	private static String buildUri(int current) {
		return "emf:uri-" + current;
	}

	/**
	 * The Class TupleQueryStub.
	 */
	private class TupleQueryStub extends AbstractQuery implements TupleQuery {
		private String query;
		private List<String> bindingNames;
		private Pattern LIMIT = Pattern.compile("(?<= LIMIT )(\\d+)",
				Pattern.CANON_EQ | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
		private Pattern OFFSET = Pattern.compile("(?<= OFFSET )(\\d+)",
				Pattern.CANON_EQ | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

		/**
		 * Instantiates a new tuple query stub.
		 *
		 * @param query
		 *            the query
		 * @param bindingNames
		 *            the binding names
		 */
		TupleQueryStub(String query, List<String> bindingNames) {
			this.query = query;
			this.bindingNames = bindingNames;
		}

		@Override
		public TupleQueryResult evaluate() throws QueryEvaluationException {
			return new TupleQueryResultStub(calculateElements(query), calculateStart(query), bindingNames);
		}

		private int calculateElements(String q) {
			Matcher matcher = LIMIT.matcher(q);
			int limit = 0;
			if (matcher.find()) {
				limit = Integer.parseInt(matcher.group(1));
			}
			return limit;
		}

		private int calculateStart(String q) {
			Matcher matcher = OFFSET.matcher(q);
			int offset = 0;
			if (matcher.find()) {
				offset = Integer.parseInt(matcher.group(1));
			}
			return offset;
		}

		@Override
		public void evaluate(TupleQueryResultHandler handler)
				throws QueryEvaluationException, TupleQueryResultHandlerException {
			// nothing to do
		}
	}

	/**
	 * The Class TupleQueryResultStub.
	 */
	private class TupleQueryResultStub implements TupleQueryResult {

		private int index = 0;
		private final int max;
		private final List<String> bindingNames;
		private int initial;

		/**
		 * Instantiates a new tuple query result stub.
		 *
		 * @param elements
		 *            the elements
		 * @param initial
		 *            the initial
		 * @param bindingNames
		 *            the binding names
		 */
		TupleQueryResultStub(int elements, int initial, List<String> bindingNames) {
			max = elements;
			this.initial = initial;
			this.bindingNames = bindingNames;
		}

		@Override
		public void close() throws QueryEvaluationException {
			// empty
		}

		@Override
		public boolean hasNext() throws QueryEvaluationException {
			return index < max;
		}

		@Override
		public BindingSet next() throws QueryEvaluationException {
			++index;
			return generateResult(++initial, bindingNames);
		}

		@Override
		public void remove() throws QueryEvaluationException {
			Assert.fail("Remove should not be called!");
		}

		@Override
		public List<String> getBindingNames() throws QueryEvaluationException {
			return bindingNames;
		}
	}
}
