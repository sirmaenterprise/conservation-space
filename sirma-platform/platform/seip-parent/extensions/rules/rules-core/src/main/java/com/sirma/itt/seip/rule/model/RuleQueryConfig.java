package com.sirma.itt.seip.rule.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchArguments.QueryResultPermissionFilter;
import com.sirma.itt.seip.domain.search.SearchInstance;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Configuration object for query executions in rule definitions.
 *
 * @author BBonev
 */
public class RuleQueryConfig {

	public static final String PROPERTY_MAPPING = PropertyMapping.NAME;
	public static final String USE_PERMISSION_FILTERING = "usePermissionFiltering";
	public static final String QUERY = "query";

	private final String queryString;
	private final Collection<PropertyMapping> propertyMapping;
	private final QueryResultPermissionFilter usePermissionFiltering;

	/**
	 * Instantiates a new rule query config.
	 *
	 * @param query
	 *            the query
	 * @param propertyMapping
	 *            the property mapping
	 * @param usePermissionFiltering
	 *            the use permission filtering
	 */
	public RuleQueryConfig(String query, Collection<PropertyMapping> propertyMapping, Boolean usePermissionFiltering) {
		this.queryString = query;
		this.propertyMapping = propertyMapping == null ? Collections.emptyList() : propertyMapping;
		if (usePermissionFiltering == null || usePermissionFiltering == Boolean.FALSE) {
			this.usePermissionFiltering = QueryResultPermissionFilter.NONE;
		} else {
			this.usePermissionFiltering = QueryResultPermissionFilter.READ;
		}
	}

	/**
	 * Parses the given configuration to build {@link RuleQueryConfig} instance
	 *
	 * @param configuration
	 *            the configuration
	 * @return the rule query configuration or <code>null</code> if the required configuration properties are not set
	 */
	public static RuleQueryConfig parse(Context<String, Object> configuration) {
		String query = configuration.getIfSameType(QUERY, String.class);
		Collection<PropertyMapping> propertyMapping = PropertyMapping
				.parse(configuration.getIfSameType(PROPERTY_MAPPING, Collection.class));
		Boolean usePermissionFiltering = configuration.getIfSameType(USE_PERMISSION_FILTERING, Boolean.class,
				Boolean.FALSE);
		if (StringUtils.isBlank(query)) {
			return null;
		}
		return new RuleQueryConfig(query, propertyMapping, usePermissionFiltering);
	}

	/**
	 * Converts the given {@link RuleQueryConfig} to map configuration that is valid for {@link #parse(Context)} method
	 *
	 * @param config
	 *            the config
	 * @return the map
	 */
	public static Map<String, Object> toMap(RuleQueryConfig config) {
		Map<String, Object> map = new HashMap<>();
		map.put(QUERY, config.getQuery());
		map.put(USE_PERMISSION_FILTERING, config.usePermissionFiltering);
		Collection<Map<String, Object>> mappingCollection = new LinkedList<>();
		for (PropertyMapping propertyMapping : config.propertyMapping) {
			CollectionUtils.addNonNullValue(mappingCollection, PropertyMapping.toMap(propertyMapping));
		}
		return map;
	}

	/**
	 * Getter method for query.
	 *
	 * @return the query
	 */
	public String getQuery() {
		return queryString;
	}

	/**
	 * Builds the search arguments.
	 *
	 * @param source
	 *            the source
	 * @param searchService
	 *            the search service
	 * @param expressionsManager
	 *            the expressions manager
	 * @return the search arguments<? extends instance>
	 */
	public SearchArguments<? extends Instance> buildSearchArguments(Instance source, SearchService searchService,
			ExpressionsManager expressionsManager) {
		return searchService.getFilter(queryString, SearchInstance.class, buildParameters(source, expressionsManager));
	}

	/**
	 * Before query execute.
	 *
	 * @param <E>
	 *            the element type
	 * @param arguments
	 *            the arguments
	 * @return the search arguments
	 */
	public <E extends Instance> SearchArguments<E> beforeQueryExecute(SearchArguments<E> arguments) {
		arguments.setPermissionsType(usePermissionFiltering);

		// ensure disabled faceting
		arguments.setFaceted(false);
		return arguments;
	}

	/**
	 * Builds the parameters.
	 *
	 * @param source
	 *            the source
	 * @return the context
	 */
	private Context<String, Object> buildParameters(Instance source, ExpressionsManager expressionsManager) {
		if (CollectionUtils.isEmpty(propertyMapping)) {
			return Context.emptyContext();
		}
		Context<String, Object> context = new Context<>(propertyMapping.size());
		ExpressionContext expressionContext = expressionsManager.createDefaultContext(source, null, null);
		for (PropertyMapping mapping : propertyMapping) {
			String fromKey = mapping.getFrom();
			Object value = evaluatePossibleExpression(fromKey, expressionContext, expressionsManager);
			if (value == null) {
				value = source.get(fromKey);
			}
			String toKey = mapping.getTo();
			if (!CollectionUtils.addNonNullValue(context, toKey, value) && mapping.isRequired()) {
				// if there is a mapping and that mapping resolves to null value
				// we cannot allow to execute the query because it could mean
				// that the query will
				// return results for wrong conditions
				throw new EmfRuntimeException("Cannot run query due to missing source value for property " + fromKey
						+ " from  instance id=" + source.getId() + " when running query with name " + queryString);
			}
		}
		return context;
	}

	/**
	 * Evaluate possible expression.
	 *
	 * @param fromKey
	 *            the from key
	 * @param expressionContext
	 *            the expression context
	 * @return the object
	 */
	private static Object evaluatePossibleExpression(String fromKey, ExpressionContext expressionContext,
			ExpressionsManager expressionsManager) {
		Object value = null;
		if (expressionsManager.isExpression(fromKey)) {
			value = expressionsManager.evaluateRule(fromKey, String.class, expressionContext);
			// it was not an expression
			if (EqualsHelper.nullSafeEquals(fromKey, value)) {
				value = null;
			}
		}
		return value;
	}
}
