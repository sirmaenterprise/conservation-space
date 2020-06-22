package com.sirma.itt.seip.rule.providers;

import static com.sirma.itt.seip.rule.model.RuleQueryConfig.PROPERTY_MAPPING;
import static com.sirma.itt.seip.rule.model.RuleQueryConfig.QUERY;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.concurrent.TaskExecutor;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.instance.InstanceTypes;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.dao.ServiceRegistry;
import com.sirma.itt.seip.rule.model.OnDuplicateConfig;
import com.sirma.itt.seip.rule.model.PropertyMapping;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.testutil.EmfTest;

/**
 * Test for {@link QueryProvider}
 *
 * @author BBonev
 */
@Test
public class QueryProviderTest extends EmfTest {

	private static final String EXPRESSION = "${get([from])}";
	private static final String QUERY_NAME = "queryName";
	SearchArguments<Instance> arguments;
	@Mock
	private ExpressionsManager expressionsManager;
	@Mock
	private SearchService searchService;
	@Mock
	private InstanceService instanceService;
	@Mock
	private InstanceTypes instanceTypes;

	@InjectMocks
	private QueryProvider queryProvider;

	@Override
	@BeforeMethod
	@SuppressWarnings("unchecked")
	public void beforeMethod() {
		super.beforeMethod();
		arguments = new SearchArguments<>();
		when(searchService.getFilter(anyString(), any(Class.class), any(Context.class))).thenReturn(arguments);
		doAnswer(a -> {
			SearchArguments argument = a.getArgumentAt(1, SearchArguments.class);
			argument.setResult(Collections.emptyList());
			return null;
		}).when(searchService).search(any(Class.class), any(SearchArguments.class));
	}

	/**
	 * test for successful configuration
	 */
	@Test
	public void testSuccessConfiguration() {
		Context<String, Object> context = new Context<>();
		context.put(QUERY, QUERY_NAME);
		AssertJUnit.assertTrue(queryProvider.configure(context));
	}

	/**
	 * Test for failing configuration
	 */
	@Test
	public void testFailConfiguration() {
		Context<String, Object> context = new Context<>();
		AssertJUnit.assertFalse(queryProvider.configure(context));
	}

	/**
	 * test non lazy query method without property mapping
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testNonLazyExecution_noParams() {
		Context<String, Object> context = new Context<>();
		context.put(QUERY, QUERY_NAME);
		queryProvider.configure(context);

		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");
		instance.setProperties(new HashMap<String, Serializable>());

		EmfInstance result = new EmfInstance();
		result.setId("emf:result");
		arguments.setResult(Arrays.asList(result));

		Collection<Instance> collection = queryProvider.resolveDependencies(instance);

		AssertJUnit.assertNotNull(collection);
		AssertJUnit.assertFalse(collection.isEmpty());
		AssertJUnit.assertEquals(collection.iterator().next(), result);

		verify(searchService).getFilter(eq(QUERY_NAME), any(Class.class), any(Context.class));
		verify(searchService).searchAndLoad(any(Class.class), any(SearchArguments.class));
	}

	/**
	 * test non lazy query method with required property mapping and the value is present
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testNonLazyExecution_withParams_required_present() {
		Context<String, Object> context = new Context<>();
		context.put(QUERY, QUERY_NAME);
		context.put(PROPERTY_MAPPING, Collections.singletonList(
				PropertyMapping.toMap(new PropertyMapping("from", "to", true, new OnDuplicateConfig()))));
		queryProvider.configure(context);

		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");
		instance.setProperties(new HashMap<String, Serializable>());
		instance.getProperties().put("from", "value");

		EmfInstance result = new EmfInstance();
		result.setId("emf:result");
		arguments.setResult(Arrays.<Instance> asList(result));

		Collection<Instance> collection = queryProvider.resolveDependencies(instance);

		Context<String, Object> searchContext = new Context<>();
		searchContext.put("to", "value");
		verify(searchService).getFilter(eq(QUERY_NAME), any(Class.class), eq(searchContext));
		verify(searchService).searchAndLoad(any(Class.class), any(SearchArguments.class));

		AssertJUnit.assertNotNull(collection);
		AssertJUnit.assertFalse(collection.isEmpty());
		AssertJUnit.assertEquals(collection.iterator().next(), result);
	}

	/**
	 * test non lazy query method with required property mapping and the value is missing
	 */
	@Test(expectedExceptions = EmfRuntimeException.class)
	public void testNonLazyExecution_withParams_required_missing() {
		Context<String, Object> context = new Context<>();
		context.put(QUERY, QUERY_NAME);
		context.put(PROPERTY_MAPPING, Collections.singletonList(
				PropertyMapping.toMap(new PropertyMapping("from", "to", true, new OnDuplicateConfig()))));
		queryProvider.configure(context);

		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");
		instance.setProperties(new HashMap<String, Serializable>());

		EmfInstance result = new EmfInstance();
		result.setId("emf:result");
		arguments.setResult(Arrays.<Instance> asList(result));

		queryProvider.resolveDependencies(instance);
	}

	/**
	 * test non lazy query method with optional property mapping and the value is missing
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testNonLazyExecution_withParams_optional_missing() {
		Context<String, Object> context = new Context<>();
		context.put(QUERY, QUERY_NAME);
		context.put(PROPERTY_MAPPING, Collections.singletonList(
				PropertyMapping.toMap(new PropertyMapping("from", "to", false, new OnDuplicateConfig()))));
		queryProvider.configure(context);

		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");
		instance.setProperties(new HashMap<String, Serializable>());

		EmfInstance result = new EmfInstance();
		result.setId("emf:result");
		arguments.setResult(Arrays.<Instance> asList(result));

		Collection<Instance> collection = queryProvider.resolveDependencies(instance);

		Context<String, Object> searchContext = new Context<>();
		verify(searchService).getFilter(eq(QUERY_NAME), any(Class.class), eq(searchContext));
		verify(searchService).searchAndLoad(any(Class.class), any(SearchArguments.class));

		AssertJUnit.assertNotNull(collection);
		AssertJUnit.assertFalse(collection.isEmpty());
		AssertJUnit.assertEquals(collection.iterator().next(), result);
	}

	/**
	 * test non lazy query method with optional property mapping and the value is present
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testNonLazyExecution_withParams_optional_present() {
		Context<String, Object> context = new Context<>();
		context.put(QUERY, QUERY_NAME);
		context.put(PROPERTY_MAPPING, Collections.singletonList(
				PropertyMapping.toMap(new PropertyMapping("from", "to", false, new OnDuplicateConfig()))));
		queryProvider.configure(context);

		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");
		instance.setProperties(new HashMap<String, Serializable>());
		instance.getProperties().put("from", "value");

		EmfInstance result = new EmfInstance();
		result.setId("emf:result");
		arguments.setResult(Arrays.<Instance> asList(result));

		Collection<Instance> collection = queryProvider.resolveDependencies(instance);

		Context<String, Object> searchContext = new Context<>();
		searchContext.put("to", "value");
		verify(searchService).getFilter(eq(QUERY_NAME), any(Class.class), eq(searchContext));
		verify(searchService).searchAndLoad(any(Class.class), any(SearchArguments.class));

		AssertJUnit.assertNotNull(collection);
		AssertJUnit.assertFalse(collection.isEmpty());
		AssertJUnit.assertEquals(collection.iterator().next(), result);
	}

	/**
	 * test non lazy query method with mapping where from is an expression
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testNonLazyExecution_withExpressionParams() {
		String expression = EXPRESSION;

		Context<String, Object> context = new Context<>();
		context.put(QUERY, QUERY_NAME);
		context.put(PROPERTY_MAPPING, Collections.singletonList(
				PropertyMapping.toMap(new PropertyMapping(expression, "to", true, new OnDuplicateConfig()))));
		queryProvider.configure(context);

		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");
		instance.setProperties(new HashMap<String, Serializable>());
		instance.getProperties().put("from", "value");

		EmfInstance result = new EmfInstance();
		result.setId("emf:result");
		arguments.setResult(Arrays.<Instance> asList(result));

		when(expressionsManager.isExpression(expression)).thenReturn(true);
		when(expressionsManager.evaluateRule(eq(expression), eq(String.class), any(ExpressionContext.class)))
				.thenReturn("value");

		Collection<Instance> collection = queryProvider.resolveDependencies(instance);

		Context<String, Object> searchContext = new Context<>();
		searchContext.put("to", "value");
		verify(searchService).getFilter(eq(QUERY_NAME), any(Class.class), eq(searchContext));
		verify(searchService).searchAndLoad(any(Class.class), any(SearchArguments.class));

		AssertJUnit.assertNotNull(collection);
		AssertJUnit.assertFalse(collection.isEmpty());
		AssertJUnit.assertEquals(collection.iterator().next(), result);
	}

	/**
	 * test non lazy query method with mapping where from is an expression but the expression is not supported, and the
	 * property is required
	 */
	@Test(expectedExceptions = EmfRuntimeException.class)
	public void testNonLazyExecution_withExpressionParams_invalidExpression_requiredMapping() {
		String expression = EXPRESSION;

		Context<String, Object> context = new Context<>();
		context.put(QUERY, QUERY_NAME);
		context.put(PROPERTY_MAPPING, Collections.singletonList(
				PropertyMapping.toMap(new PropertyMapping(expression, "to", true, new OnDuplicateConfig()))));
		queryProvider.configure(context);

		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");
		instance.setProperties(new HashMap<String, Serializable>());
		instance.getProperties().put("from", "value");

		EmfInstance result = new EmfInstance();
		result.setId("emf:result");
		arguments.setResult(Arrays.<Instance> asList(result));

		when(expressionsManager.isExpression(expression)).thenReturn(true);
		when(expressionsManager.evaluateRule(eq(expression), eq(String.class), any(ExpressionContext.class)))
				.thenReturn(expression);

		queryProvider.resolveDependencies(instance);
	}

	/**
	 * Test lazy execution_no params.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testLazyExecution_noParams() {
		Context<String, Object> context = new Context<>();
		context.put(QUERY, QUERY_NAME);
		queryProvider.configure(context);

		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");
		instance.setProperties(new HashMap<String, Serializable>());

		final EmfInstance result = new EmfInstance();
		result.setId("emf:result");
		doAnswer(invocation -> {
			arguments.setResult(Arrays.<Instance> asList(result));
			return null;
		}).when(searchService).search(Instance.class, arguments);

		Iterator<Instance> collection = queryProvider.resolveDependenciesLazily(instance);

		AssertJUnit.assertNotNull(collection);
		AssertJUnit.assertTrue(collection.hasNext());
		AssertJUnit.assertEquals(collection.next(), result);
		AssertJUnit.assertFalse(collection.hasNext());

		verify(searchService).getFilter(eq(QUERY_NAME), any(Class.class), any(Context.class));
		verify(searchService).search(any(Class.class), any(SearchArguments.class));
	}

	/**
	 * Test lazy execution_no data_no params.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testLazyExecution_noData_noParams() {
		Context<String, Object> context = new Context<>();
		context.put(QUERY, QUERY_NAME);
		queryProvider.configure(context);

		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");
		instance.setProperties(new HashMap<String, Serializable>());

		arguments.setResult(Collections.emptyList());

		Iterator<Instance> collection = queryProvider.resolveDependenciesLazily(instance);

		AssertJUnit.assertNotNull(collection);
		AssertJUnit.assertFalse(collection.hasNext());

		verify(searchService).getFilter(eq(QUERY_NAME), any(Class.class), any(Context.class));
		verify(searchService).search(any(Class.class), any(SearchArguments.class));
	}

	/**
	 * Test lazy execution_exact result_no params.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testLazyExecution_exactResult_noParams() {
		Context<String, Object> context = new Context<>();
		context.put(QUERY, QUERY_NAME);
		context.put(QueryProvider.BATCH_LOAD_SIZE, 5);
		queryProvider.configure(context);

		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");
		instance.setProperties(new HashMap<String, Serializable>());

		final EmfInstance result1 = new EmfInstance();
		result1.setId("emf:result1");
		final EmfInstance result2 = new EmfInstance();
		result2.setId("emf:result2");
		final EmfInstance result3 = new EmfInstance();
		result3.setId("emf:result3");
		final EmfInstance result4 = new EmfInstance();
		result4.setId("emf:result4");
		final EmfInstance result5 = new EmfInstance();
		result5.setId("emf:result5");

		doAnswer(invocation -> {
			arguments.setResult(Arrays.<Instance> asList(result1, result2, result3, result4, result5));
			return null;
		}).when(searchService).search(Instance.class, arguments);

		Iterator<Instance> collection = queryProvider.resolveDependenciesLazily(instance);

		assertNotNull(collection);

		assertTrue(collection.hasNext());
		assertEquals(collection.next(), result1);
		assertEquals(collection.next(), result2);
		assertEquals(collection.next(), result3);
		assertEquals(collection.next(), result4);
		assertEquals(collection.next(), result5);
		doAnswer(invocation -> {
			arguments.setResult(Collections.<Instance> emptyList());
			return null;
		}).when(searchService).search(Instance.class, arguments);

		assertFalse(collection.hasNext());

		verify(searchService).getFilter(eq(QUERY_NAME), any(Class.class), any(Context.class));
		verify(searchService, times(2)).search(any(Class.class), any(SearchArguments.class));
	}

	/**
	 * Test lazy execution_more results_no params.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testLazyExecution_moreResults_noParams() {
		Context<String, Object> context = new Context<>();
		context.put(QUERY, QUERY_NAME);
		context.put(QueryProvider.BATCH_LOAD_SIZE, 3);
		queryProvider.configure(context);

		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");
		instance.setProperties(new HashMap<String, Serializable>());

		final EmfInstance result1 = new EmfInstance();
		result1.setId("emf:result1");
		final EmfInstance result2 = new EmfInstance();
		result2.setId("emf:result2");
		final EmfInstance result3 = new EmfInstance();
		result3.setId("emf:result3");
		final EmfInstance result4 = new EmfInstance();
		result4.setId("emf:result4");
		final EmfInstance result5 = new EmfInstance();
		result5.setId("emf:result5");

		doAnswer(invocation -> {
			arguments.setResult(Arrays.<Instance> asList(result1, result2, result3));
			return null;
		}).when(searchService).search(Instance.class, arguments);

		Iterator<Instance> collection = queryProvider.resolveDependenciesLazily(instance);

		AssertJUnit.assertNotNull(collection);
		AssertJUnit.assertTrue(collection.hasNext());
		AssertJUnit.assertEquals(collection.next(), result1);
		AssertJUnit.assertEquals(collection.next(), result2);
		AssertJUnit.assertEquals(collection.next(), result3);
		doAnswer(invocation -> {
			arguments.setResult(Arrays.<Instance> asList(result4, result5));
			return null;
		}).when(searchService).search(Instance.class, arguments);

		AssertJUnit.assertEquals(collection.next(), result4);
		AssertJUnit.assertEquals(collection.next(), result5);

		doAnswer(invocation -> {
			arguments.setResult(Collections.<Instance> emptyList());
			return null;
		}).when(searchService).search(Instance.class, arguments);

		AssertJUnit.assertFalse(collection.hasNext());

		verify(searchService).getFilter(eq(QUERY_NAME), any(Class.class), any(Context.class));
		verify(searchService, times(2)).search(any(Class.class), any(SearchArguments.class));
	}

}
