package com.sirma.itt.emf.evaluation;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.search.SearchService;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.SearchFilterConfig;
import com.sirma.itt.emf.search.model.SearchInstance;
import com.sirma.itt.emf.time.TimeTracker;

/**
 * Evaluator that can execute queries defined in definitions. The evaluator can return the count of
 * executed query (default) or the result list returned from the search engine. Additional
 * conversion is required if result should be used in other expressions.
 * 
 * @author BBonev
 */
@ApplicationScoped
public class QueryEvaluator extends BaseEvaluator {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -740292862322282260L;
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(QueryEvaluator.class);

	/** The Constant FIELD_PATTERN. */
	private static final Pattern FIELD_PATTERN = Pattern.compile(EXPRESSION_START
			+ "\\{query\\(([\\w-:/]+)\\s*,?\\s*(.*)?\\)(\\.count|\\.result)?\\}");
	/** The Constant PROPERTY_PATTERN. */
	private static final Pattern PROPERTY_PATTERN = Pattern.compile("(\\w+)=(.+)");
	/** The Constant SPLIT_PATTERN. */
	private static final Pattern SPLIT_PATTERN = Pattern.compile("\\s*,\\s*");
	/** The Constant EMPTY_CONTEXT. */
	private static final Context<String, Object> EMPTY_CONTEXT = new Context<String, Object>(1);

	/** The search service. */
	@Inject
	private SearchService searchService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Pattern getPattern() {
		return FIELD_PATTERN;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context,
			Serializable... values) {
		String queryName = matcher.group(1);
		String params = matcher.group(2);
		String count = matcher.group(3);

		SearchFilterConfig config = searchService.getFilterConfiguration(queryName,
				SearchInstance.class);
		// filter not found - definition not found or the query itself.
		if ((config == null) || config.getFilters().isEmpty()) {
			LOGGER.warn("Query definition {} not found! Nothing will be executed.", queryName);
			return null;
		}
		// try to build query arguments, we expect to have undefined query dialect or filter builder
		// configuration
		SearchArguments<Instance> arguments = searchService.buildSearchArguments(config
				.getFilters().get(0), SearchInstance.class, buildContext(params));
		if (arguments == null) {
			LOGGER.warn("Could not build query arguments for query name {}.", queryName);
			return null;
		}
		// by default we will return only the count-a if not specified otherwise
		boolean isCountOnly = StringUtils.isNullOrEmpty(count) || ".count".equals(count);
		arguments.setCountOnly(isCountOnly);

		TimeTracker tracker = TimeTracker.createAndStart();
		searchService.search(Instance.class, arguments);
		LOGGER.debug("Execution of query {} from expression took {} ms", queryName, tracker.stop());
		if (isCountOnly) {
			return arguments.getTotalItems();
		}
		return (Serializable) arguments.getResult();
	}

	/**
	 * Builds the context for parameters.
	 * 
	 * @param params
	 *            the params
	 * @return the context
	 */
	protected Context<String, Object> buildContext(String params) {
		if (StringUtils.isNullOrEmpty(params)) {
			return EMPTY_CONTEXT;
		}
		String[] propertyPair = SPLIT_PATTERN.split(params);
		Context<String, Object> context = new Context<String, Object>(propertyPair.length);
		for (String pair : propertyPair) {
			Matcher matcher = PROPERTY_PATTERN.matcher(pair);
			if (matcher.matches()) {
				context.put(matcher.group(1), matcher.group(2));
			}
		}
		return context;
	}

}
