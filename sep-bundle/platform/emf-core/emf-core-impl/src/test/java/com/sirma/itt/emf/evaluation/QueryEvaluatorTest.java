package com.sirma.itt.emf.evaluation;

import java.util.Arrays;
import java.util.List;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.search.SearchDialects;
import com.sirma.itt.emf.search.SearchService;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.SearchFilter;
import com.sirma.itt.emf.search.model.SearchFilterConfig;
import com.sirma.itt.emf.search.model.SearchInstance;

/**
 * The Class QueryEvaluatorTest.
 * 
 * @author BBonev
 */
@Test
public class QueryEvaluatorTest extends BaseEvaluatorTest {

	/** The search service. */
	private SearchService searchService;

	/**
	 * Test query without params.
	 */
	public void testQueryWithoutParams() {
		ExpressionsManager manager = createManager();
		SearchFilter filter = new SearchFilter("testQuery", "label", "", null);
		SearchFilterConfig filterConfig = new SearchFilterConfig(Arrays.asList(filter), null);
		Mockito.when(
				searchService.getFilterConfiguration("queryDef/queryName", SearchInstance.class))
				.thenReturn(filterConfig);
		SearchArguments<Instance> arguments = new SearchArguments<>();
		arguments.setStringQuery("testQuery");
		arguments.setDialect(SearchDialects.SOLR);
		arguments.setTotalItems(10);
		Mockito.when(
				searchService.buildSearchArguments(Mockito.eq(filter),
						Mockito.eq(SearchInstance.class), Mockito.any(Context.class))).thenReturn(
				arguments);
		Integer evaluated = manager.evaluate("${query(queryDef/queryName)}", Integer.class);
		Assert.assertEquals(evaluated, Integer.valueOf(10));
		Mockito.verify(searchService).search(Instance.class, arguments);
	}

	/**
	 * Test query with params.
	 */
	public void testQueryWithParams() {
		ExpressionsManager manager = createManager();
		SearchFilter filter = new SearchFilter("testQuery", "label", "", null);
		SearchFilterConfig filterConfig = new SearchFilterConfig(Arrays.asList(filter), null);
		Mockito.when(
				searchService.getFilterConfiguration("queryDef/queryName", SearchInstance.class))
				.thenReturn(filterConfig);
		SearchArguments<Instance> arguments = new SearchArguments<>();
		arguments.setStringQuery("testQuery");
		arguments.setDialect(SearchDialects.SOLR);
		arguments.setTotalItems(10);
		Context<String, Object> context = new Context<String, Object>(3);
		context.put("param1", "emf:test-test1");
		context.put("param2", "workspace://test/test");
		context.put("param3", "3333");
		Mockito.when(searchService.buildSearchArguments(filter, SearchInstance.class, context))
				.thenReturn((SearchArguments) arguments);
		Integer evaluated = manager
				.evaluate(
						"${query(queryDef/queryName, param1=emf:test-test1, param2=workspace://test/test, param3=3333)}",
						Integer.class);
		Assert.assertEquals(evaluated, Integer.valueOf(10));
		Mockito.verify(searchService).search(Instance.class, arguments);
	}

	/**
	 * Test with undefined query.
	 */
	public void testWithUndefinedQuery() {
		ExpressionsManager manager = createManager();
		Integer evaluated = manager
				.evaluate(
						"${query(queryDef/queryName, param1=emf:test-test1, param2=workspace://test/test, param3=3333)}",
						Integer.class);
		Assert.assertNull(evaluated);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<ExpressionEvaluator> initializeEvaluators(ExpressionEvaluatorManager manager,
			TypeConverter converter) {
		List<ExpressionEvaluator> list = super.initializeEvaluators(manager, converter);
		ExpressionEvaluator evaluator = new QueryEvaluator();
		searchService = Mockito.mock(SearchService.class);
		ReflectionUtils.setField(evaluator, "searchService", searchService);
		list.add(initEvaluator(evaluator, manager, converter));
		return list;
	}
}
