package com.sirma.itt.seip.rule.preconditions;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sirma.itt.emf.rule.RuleContext;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.rule.model.RuleQueryConfig;
import com.sirma.itt.seip.rule.preconditions.QueryPrecondition;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.testutil.EmfTest;

/**
 * @author BBonev
 */
@Test
public class QueryPreconditionTest extends EmfTest {

	@Mock
	ExpressionsManager expressionsManager;
	@Mock
	SearchService searchService;
	@Spy
	TypeConverter typeConverter = createTypeConverter();

	@InjectMocks
	QueryPrecondition precondition;

	SearchArguments<Instance> args;

	@Override
	@BeforeMethod
	@SuppressWarnings("unchecked")
	public void beforeMethod() {
		precondition = new QueryPrecondition();
		super.beforeMethod();

		args = new SearchArguments<>();
		when(searchService.getFilter(eq("queryId"), any(Class.class), any(Context.class))).thenReturn(args);
	}

	@Test(dataProvider = "withQueryResultProvider")
	public void testQuery_withQueryResult(String fiendName, String value, String resultName, String resultValue,
			String operator, boolean expectedResult) {
		assertTrue(precondition.configure(buildConfig(null, operator, fiendName, value)));
		EmfInstance testInstance = new EmfInstance();
		testInstance.setId("emf:instance");
		testInstance.setProperties(new HashMap<String, Serializable>());

		EmfInstance result = new EmfInstance();
		result.setId("emf:instance");
		result.setProperties(new HashMap<String, Serializable>());
		if (resultName != null) {
			result.getProperties().put(resultName, resultValue);
		}

		args.setTotalItems(1);
		args.setResult(Collections.<Instance> singletonList(result));

		assertEquals(precondition.checkPreconditions(RuleContext.create(testInstance, testInstance, null)),
				expectedResult);

	}

	@Test(dataProvider = "withOperatorProvider")
	public void testQuery_withOperator(Integer configValue, Integer expectedValue, String operation,
			boolean expectedResult) {
		assertTrue(precondition.configure(buildConfig(configValue.intValue(), operation, null, null)));
		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");

		args.setTotalItems(expectedValue.intValue());

		assertEquals(precondition.checkPreconditions(RuleContext.create(instance, instance, null)), expectedResult);
	}

	@DataProvider(name = "withOperatorProvider")
	public Object[][] withOperatorProvider() {
		return new Object[][] { { 0, 0, "=", true }, { 0, 1, "=", false }, { 0, 1, ">", true }, { 1, 0, ">", false },
				{ 0, 1, ">=", true }, { 0, 1, "<>", true } };
	}

	@DataProvider(name = "withQueryResultProvider")
	public Object[][] withQueryResultProvider() {
		return new Object[][] { { "fieldName", "value", "fieldName", "value", null, true },
				{ "fieldName", "value", "fieldName", "value2", null, false },
				{ "fieldName", "value", "fieldName", "value2", "=", false },
				{ "fieldName", "value", "fieldName", "value2", "==", false },
				{ "fieldName", "value", "fieldName", null, null, false },
				{ "fieldName", "value", null, null, null, false },
				{ "fieldName", "value", "fieldName", "value2", "!=", true },
				{ "fieldName", "value", "fieldName", "value2", "<>", true },
				{ "fieldName", "value", "fieldName", "value", "!=", false },
				{ "fieldName", "value", "fieldName", "value", ">", false },
				{ "fieldName", "value", "fieldName", "value2", ">", false } };
	}

	/**
	 * Builds the config.
	 *
	 * @param count
	 *            the count
	 * @param operator
	 *            the operator
	 * @param projectField
	 *            the project field
	 * @param value
	 *            the value
	 * @return the context
	 */
	Context<String, Object> buildConfig(Integer count, String operator, String projectField, Object value) {
		RuleQueryConfig config = new RuleQueryConfig("queryId", Collections.EMPTY_LIST, false);
		Context<String, Object> context = new Context<>(RuleQueryConfig.toMap(config));
		Map<String, Object> expected = new HashMap<>();
		expected.put(QueryPrecondition.EXPECTED_COUNT, count);
		expected.put(QueryPrecondition.EXPECTED_OPERATOR, operator);
		expected.put(QueryPrecondition.EXPECTED_PROJECTED_NAME, projectField);
		expected.put(QueryPrecondition.EXPECTED_VALUE, value);
		context.put(QueryPrecondition.EXPECTED, expected);
		return context;
	}

}
