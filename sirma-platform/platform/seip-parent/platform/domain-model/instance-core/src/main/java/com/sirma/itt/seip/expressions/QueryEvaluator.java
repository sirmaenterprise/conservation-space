package com.sirma.itt.seip.expressions;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchInstance;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.time.TimeTracker;

/**
 * Evaluator that can execute queries defined in definitions. The evaluator can return the count of executed query
 * (default) or the result list returned from the search engine. Additional conversion is required if result should be
 * used in other expressions.
 *
 * @author BBonev
 */
@Singleton
public class QueryEvaluator extends BaseEvaluator {

	private static final long serialVersionUID = -740292862322282260L;

	private static final Logger LOGGER = LoggerFactory.getLogger(QueryEvaluator.class);

	private static final Pattern FIELD_PATTERN = Pattern
			.compile(EXPRESSION_START + "\\{query\\(([\\w-:/]+)\\s*,?\\s*(.*)?\\)(\\.count|\\.result)?\\}");

	private static final Pattern PROPERTY_PATTERN = Pattern.compile("(\\w+)=(.+)");

	private static final Pattern SPLIT_PATTERN = Pattern.compile("\\s*,\\s*");

	private static final Context<String, Object> EMPTY_CONTEXT = new Context<>(1);

	@Inject
	private SearchService searchService;

	@Override
	protected Pattern getPattern() {
		return FIELD_PATTERN;
	}

	@Override
	public String getExpressionId() {
		return "query";
	}

	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context, Serializable... values) {
		String queryName = matcher.group(1);
		String params = matcher.group(2);
		String count = matcher.group(3);

		// try to build query arguments, we expect to have undefined query dialect or filter builder
		// configuration
		SearchArguments<SearchInstance> arguments = searchService.getFilter(queryName, SearchInstance.class,
				buildContext(params));
		if (arguments == null) {
			LOGGER.warn("Could not build query arguments for query name {}.", queryName);
			return null;
		}
		// by default we will return only the count-a if not specified otherwise
		boolean isCountOnly = StringUtils.isBlank(count) || ".count".equals(count);
		arguments.setCountOnly(isCountOnly);

		TimeTracker tracker = TimeTracker.createAndStart();
		if (isCountOnly) {
			searchService.search(Instance.class, arguments);
			LOGGER.debug("Execution of query {} from expression took {} ms", queryName, tracker.stop());
			return arguments.getTotalItems();
		}

		searchService.searchAndLoad(Instance.class, arguments);
		LOGGER.debug("Execution of query {} from expression took {} ms", queryName, tracker.stop());
		return (Serializable) arguments.getResult();
	}

	/**
	 * Builds the context for parameters.
	 *
	 * @param params
	 *            the provided parameters
	 * @return the context
	 */
	protected Context<String, Object> buildContext(String params) {
		if (StringUtils.isBlank(params)) {
			return EMPTY_CONTEXT;
		}
		String[] propertyPair = SPLIT_PATTERN.split(params);
		Context<String, Object> context = new Context<>(propertyPair.length);
		for (String pair : propertyPair) {
			Matcher matcher = PROPERTY_PATTERN.matcher(pair);
			if (matcher.matches()) {
				context.put(matcher.group(1), matcher.group(2));
			}
		}
		return context;
	}

}
