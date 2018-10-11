package com.sirma.itt.seip.rule.preconditions;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.rule.BaseDynamicInstanceRule;
import com.sirma.itt.emf.rule.RuleContext;
import com.sirma.itt.emf.rule.RulePrecondition;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.rule.model.RuleQueryConfig;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Precondition to execute custom queries checks.
 *
 * @author BBonev
 */
@Named(QueryPrecondition.QUERY_CHECK_NAME)
public class QueryPrecondition extends BaseDynamicInstanceRule implements RulePrecondition {

	private static final String DEFAULT_OPERATOR = "=";

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public static final String QUERY_CHECK_NAME = "queryCheck";

	public static final String EXPECTED = "expected";
	public static final String EXPECTED_VALUE = "value";
	public static final String EXPECTED_PROJECTED_NAME = "projectionName";
	public static final String EXPECTED_COUNT = "resultCount";
	public static final String EXPECTED_OPERATOR = "compareOperator";

	@Inject
	private ExpressionsManager expressionsManager;
	@Inject
	private SearchService searchService;
	@Inject
	private TypeConverter typeConverter;

	private RuleQueryConfig queryConfig;
	private String projectionName;
	private String value;
	private Integer expectedCount = 0;
	private String operator = DEFAULT_OPERATOR;

	@Override
	@SuppressWarnings("unchecked")
	public boolean configure(Context<String, Object> configuration) {
		if (!super.configure(configuration)) {
			return false;
		}

		queryConfig = RuleQueryConfig.parse(configuration);
		if (queryConfig == null) {
			return false;
		}

		Map<String, Object> expected = configuration.getIfSameType(EXPECTED, Map.class);
		if (expected != null) {
			Context<String, Object> context = new Context<>(expected);
			projectionName = context.getIfSameType(EXPECTED_PROJECTED_NAME, String.class);
			value = context.getIfSameType(EXPECTED_VALUE, String.class);
			expectedCount = context.getIfSameType(EXPECTED_COUNT, Integer.class, 0);
			operator = context.getIfSameType(EXPECTED_OPERATOR, String.class, DEFAULT_OPERATOR);
		}
		return true;
	}

	@Override
	public String getName() {
		return QUERY_CHECK_NAME;
	}

	@Override
	public String getPrimaryOperation() {
		return QUERY_CHECK_NAME;
	}

	@Override
	public boolean isAsyncSupported() {
		return true;
	}

	@Override
	public boolean checkPreconditions(RuleContext processingContext) {
		Instance instance = processingContext.getTriggerInstance();
		SearchArguments<? extends Instance> arguments = queryConfig.buildSearchArguments(instance, searchService,
				expressionsManager);
		if (arguments == null) {
			LOGGER.warn("Query check could not manage to build correct arguments for query {}", queryConfig.getQuery());
			return false;
		}
		searchService.search(Instance.class, queryConfig.beforeQueryExecute(arguments));
		return verifyResult(arguments);
	}

	private boolean verifyResult(SearchArguments<? extends Instance> arguments) {
		if (StringUtils.isNotBlank(value) && StringUtils.isNotBlank(projectionName)
				&& CollectionUtils.isNotEmpty(arguments.getResult())) {
			return checkProperty(arguments.getResult());
		}
		int totalItems = arguments.getTotalItems();
		return compareWithExpectedCount(totalItems);
	}

	private boolean compareWithExpectedCount(int foundCount) {
		switch (operator) {
			case "=":
			case "==":
				return foundCount == expectedCount;
			case "!=":
			case "<>":
				return foundCount != expectedCount;
			case ">":
				return foundCount > expectedCount;
			case "<":
				return foundCount < expectedCount;
			case ">=":
				return foundCount >= expectedCount;
			case "<=":
				return foundCount <= expectedCount;
			default:
				LOGGER.warn("Invalid  operator {} for count compare", operator);
				return false;
		}
	}

	private boolean checkProperty(List<? extends Instance> result) {
		for (Instance instance : result) {
			Serializable serializable = instance.getProperties().get(projectionName);
			if (serializable != null) {
				Serializable converted = typeConverter.tryConvert(serializable.getClass(), value);
				if (converted != null && compareQueryValueWithExpected(serializable, converted)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean compareQueryValueWithExpected(Serializable found, Serializable expected) {
		switch (operator) {
			case "=":
			case "==":
				return EqualsHelper.nullSafeEquals(found, expected);
			case "!=":
			case "<>":
				return !EqualsHelper.nullSafeEquals(found, expected);
			default:
				LOGGER.warn("Invalid  operator {} for value compare", operator);
				return false;
		}
	}
}
