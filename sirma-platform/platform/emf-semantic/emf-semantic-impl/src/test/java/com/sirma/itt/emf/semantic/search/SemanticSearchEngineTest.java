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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.NumericLiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.impl.AbstractQuery;
import org.openrdf.query.impl.MapBindingSet;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sirma.itt.emf.semantic.SemanticSearchConfigurations;
import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.definition.DictionaryService;
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
import com.sirma.itt.semantic.TransactionalRepositoryConnection;
import com.sirma.itt.semantic.configuration.SemanticConfiguration;

/**
 * @author BBonev
 */
public class SemanticSearchEngineTest {

	private static final Random RANDOM = new SecureRandom();
	@InjectMocks
	SemanticSearchEngine searchEngine;
	@Mock
	private TransactionalRepositoryConnection repositoryConnection;
	@Mock
	NamespaceRegistryService namespaceRegistryService;
	@Spy
	SecurityContextManager securityContextManager = new SecurityContextManagerFake();
	@Mock
	TypeConverter typeConverter;
	@Mock
	DictionaryService dictionaryService;
	@Mock
	Statistics statistics;
	@Mock
	SemanticConfiguration semanticConfiguration;
	@Mock
	SemanticSearchConfigurations configurations;
	@Mock
	AuthorityService authorityService;
	@Mock
	SecurityContext securityContext;

	@Spy
	private InstanceProxyMock<RepositoryConnection> connectionProxy = new InstanceProxyMock<>(null);

	private Map<String, Function<String, Value>> valueProducers = new HashMap<>();

	@BeforeMethod
	public void beforeMethod() throws RepositoryException, MalformedQueryException {
		MockitoAnnotations.initMocks(this);
		connectionProxy.set(repositoryConnection);
		when(namespaceRegistryService.buildFullUri(anyString())).then(a -> a.getArgumentAt(0, String.class));
		when(namespaceRegistryService.getShortUri(any(URI.class)))
				.then(a -> String.valueOf(a.getArgumentAt(0, Object.class)));
		when(dictionaryService.getDataTypeDefinition("instance")).then(a -> {
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
		when(configurations.getForbiddenRoleUri()).thenReturn(new ConfigurationPropertyMock<>("conc:SecurityRoleTypes-Forbidden"));
		when(configurations.getWriteRoleUri()).thenReturn(new ConfigurationPropertyMock<>("conc:SecurityRoleTypes-Read-Write"));
		when(configurations.getListOfCaseInsensitiveProperties()).thenReturn(new ConfigurationPropertyMock<>("dcterms:title"));
		when(configurations.getIgnoreInstanceTypes()).thenReturn(new ConfigurationPropertyMock<>("sectioninstance, classinstance, commoninstance, topicinstance, commentinstance, user, group"));
		EmfUser user = new EmfUser();
		user.setId("emf:test");
		when(securityContext.getEffectiveAuthentication()).thenReturn(user);
		SPARQLQueryHelper.setNamespaceRegistryService(namespaceRegistryService);
		SPARQLQueryHelper.setCaseInsenitiveOrderByList("");
		valueProducers.clear();
		searchEngine.init();

	}

	/**
	 * Test search pagination.
	 *
	 * @param page
	 *            the page
	 * @param pageSize
	 *            the page size
	 * @param total
	 *            the total
	 * @param sortField
	 *            the sort field
	 * @param inParallel
	 *            the in parallel
	 * @param sortInDb
	 *            the sort in db
	 * @throws QueryEvaluationException
	 *             the query evaluation exception
	 * @throws RepositoryException
	 *             the repository exception
	 * @throws MalformedQueryException
	 *             the malformed query exception
	 */
	@Test(dataProvider = "testPaginationProvider")
	public void test_pagination(Integer page, Integer pageSize, Integer total, String sortField, Boolean inParallel,
			Boolean sortInDb) throws QueryEvaluationException, RepositoryException, MalformedQueryException {
		int aPage = page.intValue();
		int aPageSize = pageSize.intValue();

		SearchArguments<Instance> arguments = createArgs(aPage, aPageSize, sortField);

		valueProducers.put("prop1", name -> new LiteralImpl(name + RANDOM.nextInt()));
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

	private static Value generateDate() {
		return new NumericLiteralImpl(RANDOM.nextLong());
	}

	@SuppressWarnings("static-method")
	@DataProvider(name = "testPaginationProvider", parallel = false)
	public Object[][] testPaginationProvider() {
		return new Object[][] { { 1, 25, 1000, "prop1", true, true }, { 2, 25, 1000, "prop2", true, true },
				{ 3, 33, 1000, "prop1", true, true }, { 5, 10, 1000, "prop2", true, true },
				{ 5, 25, 1000, "prop2", true, true }, { 11, 100, 1000, "prop2", true, true },

				{ 1, 25, 1000, "prop1", true, false }, { 2, 25, 1000, "prop2", true, false },
				{ 3, 33, 1000, "prop1", true, false }, { 5, 10, 1000, "prop2", true, false },
				{ 5, 25, 1000, "prop2", true, false }, { 11, 100, 1000, "prop2", true, false },

				{ 1, 25, 1000, "prop1", false, true }, { 2, 25, 1000, "prop2", false, true },
				{ 3, 33, 1000, "prop1", false, true }, { 5, 10, 1000, "prop2", false, true },
				{ 5, 25, 1000, "prop2", false, true }, { 11, 100, 1000, "prop2", false, true },

				{ 1, 25, 1000, "prop1", false, false }, { 2, 25, 1000, "prop2", false, false },
				{ 3, 33, 1000, "prop1", false, false }, { 5, 10, 1000, "prop2", false, false },
				{ 5, 25, 1000, "prop2", false, false }, { 11, 100, 1000, "prop2", false, false } };
	}

	private static SearchArguments<Instance> createArgs(int page, int pageSize, String sortField) {
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
		bindingSet.addBinding("instance", new URIImpl(buildUri(current)));
		bindingSet.addBinding("instanceType", new LiteralImpl("instance"));
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
