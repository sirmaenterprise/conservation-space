package com.sirma.itt.emf.semantic.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.StringPair;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.PropertyInstance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchFilter;
import com.sirma.itt.seip.domain.search.SearchFilterConfig;
import com.sirma.itt.seip.domain.search.Sorter;
import com.sirma.itt.seip.instance.CommonInstance;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.search.NamedQueries;
import com.sirma.itt.seip.search.NamedQueries.Filters;
import com.sirma.itt.seip.search.NamedQueries.Params;
import com.sirma.itt.seip.search.SearchServiceFilterExtension;
import com.sirma.itt.semantic.queries.QueryBuilder;
import com.sirma.itt.semantic.search.SemanticQueries;

/**
 * Search filter extension to create filters for semantic queries.
 *
 * @author kirq4e
 */
@ApplicationScoped
@Extension(target = SearchServiceFilterExtension.TARGET_NAME, order = 60)
public class SemanticSearchServiceFilterExtension implements SearchServiceFilterExtension {

	/** The Constant SUPPORTED_OBJECTS. */
	private static final List<Class> SUPPORTED_OBJECTS = new ArrayList<>(
			Arrays.asList(Instance.class, ClassInstance.class, PropertyInstance.class, CommonInstance.class));

	private static final Logger LOGGER = LoggerFactory.getLogger(SemanticSearchServiceFilterExtension.class);

	/** The query builder. */
	@Inject
	private QueryBuilder queryBuilder;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Class> getSupportedObjects() {
		return SUPPORTED_OBJECTS;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <S extends SearchArguments<?>> S buildSearchArguments(String filterName, Context<String, Object> context) {

		SemanticQueries query = SemanticQueries.getValueByName(filterName);

		SearchArguments<Instance> arguments = new SearchArguments<>();
		if (query == null) {

			if (NamedQueries.CHECK_EXISTING_INSTANCE.equals(filterName)) {
				Collection object = context.getIfSameType(Params.URIS, Collection.class);
				boolean includeDeleted = context.getIfSameType(Params.INCLUDE_DELETED, Boolean.class, Boolean.FALSE);
				return (S) checkExistingInstances(object, includeDeleted);
			}
			arguments.setStringQuery(filterName); // this is strange >.>
		} else {
			String stringQuery = applyQueryParameters(query.getQuery(), query.getParameters(), context);
			LOGGER.trace("Build query\n{}", stringQuery);
			arguments.setStringQuery(stringQuery);
		}

		arguments.setQueryName(filterName);
		return (S) arguments;
	}

	/**
	 * Apply sorter.
	 *
	 * @param stringQuery
	 *            the string query
	 * @param context
	 *            the context
	 * @return the string
	 */
	private static String applySorter(String stringQuery, Context<String, Object> context) {
		if (context == null || context.isEmpty()) {
			return stringQuery;
		}
		String localQuery = stringQuery;
		Sorter sorter = context.getIfSameType("sort", Sorter.class);
		if (sorter == null || StringUtils.isBlank(sorter.getSortField())) {
			return stringQuery;
		}

		localQuery += " order by " + (sorter.isAscendingOrder() ? "ASC" : "DESC") + "(?" + sorter.getSortField() + ")";
		return localQuery;
	}

	/**
	 * Apply query parameters.
	 *
	 * @param query
	 *            the query
	 * @param parameters
	 *            the parameters
	 * @param context
	 *            the context
	 * @return the string
	 */
	private static String applyQueryParameters(String query, List<StringPair> parameters,
			Context<String, Object> context) {
		if (parameters == null || parameters.isEmpty() || context == null || context.isEmpty()) {
			return query;
		}
		String localQuery = applySorter(query, context);

		for (StringPair param : parameters) {
			String value = context.getIfSameType(param.getFirst(), String.class);
			if (value == null) {
				value = param.getSecond();
			}
			localQuery = localQuery.replaceAll("\\{" + param.getFirst() + "\\}", value);
		}

		Integer limit = context.getIfSameType("limit", Integer.class);
		if (limit != null && limit.intValue() > 0) {
			localQuery += " LIMIT " + limit;
		}
		return localQuery;
	}

	/**
	 * Creates query for checking if the list of URIs exits in the semantic repository.
	 *
	 * @param <S>
	 *            SearchArguments type
	 * @param uris
	 *            List of URIs
	 * @return Query for searching if the URIs from the input parameter exist in the repository
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <S extends SearchArguments<?>> S checkExistingInstances(Collection uris, boolean includeDeleted) {
		SearchArguments<?> args = new SearchArguments<Instance>();
		if (uris != null && !uris.isEmpty()) {
			args.setSkipCount(0);
			args.setPageSize(100);

			List<Pair<String, Object>> parameters = new ArrayList<>();
			parameters.add(new Pair<String, Object>(Params.URIS, uris));
			String filterName = includeDeleted ? Filters.SKIP : Filters.IS_NOT_DELETED;
			String query = queryBuilder.buildQueryByName(NamedQueries.CHECK_EXISTING_INSTANCE, parameters, filterName);

			args.setStringQuery(query);
			return (S) args;
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SearchFilterConfig getFilterConfiguration(String placeHolder) {
		return new SearchFilterConfig(new LinkedList<SearchFilter>(), new LinkedList<SearchFilter>());
	}

	@Override
	public <S extends SearchArguments<?>> S buildSearchArguments(SearchFilter filter, Context<String, Object> context) {
		if (filter != null) {
			return buildSearchArguments(filter.getValue(), context);
		}
		return null;
	}

}
