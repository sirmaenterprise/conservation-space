package com.sirma.itt.seip.expressions;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchDialects;
import com.sirma.itt.seip.domain.search.SearchInstance;
import com.sirma.itt.seip.expressions.ExpressionEvaluator;
import com.sirma.itt.seip.search.SearchService;

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
	@SuppressWarnings("unchecked")
	public void testQueryWithoutParams() {
		ExpressionsManager manager = createManager();
		SearchArguments<Instance> arguments = new SearchArguments<>();
		arguments.setStringQuery("testQuery");
		arguments.setDialect(SearchDialects.SOLR);
		arguments.setTotalItems(10);
		when(searchService.getFilter(eq("queryDef/queryName"), eq(SearchInstance.class), any(Context.class)))
				.thenReturn(arguments);
		Integer evaluated = manager.evaluate("${query(queryDef/queryName)}", Integer.class);
		Assert.assertEquals(evaluated, Integer.valueOf(10));
		verify(searchService).search(Instance.class, arguments);
	}

	/**
	 * Test query with params.
	 */
	public void testQueryWithParams() {
		ExpressionsManager manager = createManager();
		SearchArguments<Instance> arguments = new SearchArguments<>();
		arguments.setStringQuery("testQuery");
		arguments.setDialect(SearchDialects.SOLR);
		arguments.setTotalItems(10);
		Context<String, Object> context = new Context<>(3);
		context.put("param1", "emf:test-test1");
		context.put("param2", "workspace://test/test");
		context.put("param3", "3333");
		when(searchService.getFilter("queryDef/queryName", SearchInstance.class, context)).thenReturn(arguments);
		Integer evaluated = manager.evaluate(
				"${query(queryDef/queryName, param1=emf:test-test1, param2=workspace://test/test, param3=3333)}",
				Integer.class);
		Assert.assertEquals(evaluated, Integer.valueOf(10));
		verify(searchService).search(Instance.class, arguments);
	}

	/**
	 * Test with undefined query.
	 */
	public void testWithUndefinedQuery() {
		ExpressionsManager manager = createManager();
		Integer evaluated = manager.evaluate(
				"${query(queryDef/queryName, param1=emf:test-test1, param2=workspace://test/test, param3=3333)}",
				Integer.class);
		Assert.assertNull(evaluated);
	}

	@Override
	protected List<ExpressionEvaluator> initializeEvaluators(ExpressionsManager manager,
			TypeConverter converter) {
		List<ExpressionEvaluator> list = super.initializeEvaluators(manager, converter);
		ExpressionEvaluator evaluator = new QueryEvaluator();
		searchService = Mockito.mock(SearchService.class);
		ReflectionUtils.setFieldValue(evaluator, "searchService", searchService);
		list.add(initEvaluator(evaluator, manager, converter));
		return list;
	}
}
