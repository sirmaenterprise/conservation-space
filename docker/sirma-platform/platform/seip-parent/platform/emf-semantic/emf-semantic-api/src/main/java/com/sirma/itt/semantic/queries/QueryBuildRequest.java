package com.sirma.itt.semantic.queries;

import static com.sirma.itt.seip.collections.CollectionUtils.addIf;
import static com.sirma.itt.seip.collections.CollectionUtils.addNonNullValue;
import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.Pair;

/**
 * Request object for building a query via {@link QueryBuilder}
 *
 * @author BBonev
 */
public class QueryBuildRequest {

	private final String queryName;
	private final Set<String> projections = new LinkedHashSet<>();
	private final List<Pair<String, Object>> parameters = new LinkedList<>();
	private final List<String> filterNames = new LinkedList<>();
	private final List<Function<String, String>> filters = new LinkedList<>();

	/**
	 * Instantiates a new query build request for the given named query
	 *
	 * @param queryName
	 *            the query name
	 */
	public QueryBuildRequest(String queryName) {
		this.queryName = Objects.requireNonNull(queryName, "Query name is required");
	}

	/**
	 * Adds a single parameter for query building.
	 *
	 * @param name
	 *            the name of the parameter
	 * @param value
	 *            the value of the parameter
	 * @return the query build request
	 */
	public QueryBuildRequest addParameter(String name, Object value) {
		if (name != null && value != null) {
			parameters.add(new Pair<>(name, value));
		}
		return this;
	}

	/**
	 * Adds the given parameters to be used for query building
	 *
	 * @param params
	 *            the parameters to add
	 * @return the query build request
	 */
	public QueryBuildRequest addParameters(Collection<Pair<String, Object>> params) {
		if (isEmpty(params)) {
			return this;
		}
		parameters.addAll(params);
		return this;
	}

	/**
	 * Adds named filter to be applied to the query
	 *
	 * @param filter
	 *            the filter
	 * @return the query build request
	 */
	public QueryBuildRequest addFilter(String filter) {
		addNonNullValue(filterNames, filter);
		return this;
	}

	/**
	 * Adds a dynamic filter to be applied during filter building
	 *
	 * @param filter
	 *            the filter
	 * @return the query build request
	 */
	public QueryBuildRequest addFilter(Function<String, String> filter) {
		addNonNullValue(filters, filter);
		return this;
	}

	/**
	 * Adds named filters to be applied to the result query
	 *
	 * @param filtersToAdd
	 *            the filters to add
	 * @return the query build request
	 */
	public QueryBuildRequest addFilters(Collection<String> filtersToAdd) {
		if (isEmpty(filtersToAdd)) {
			return this;
		}
		for (String filter : filtersToAdd) {
			addNonNullValue(filterNames, filter);
		}
		return this;
	}

	/**
	 * Adds a projection to the result query
	 *
	 * @param projection
	 *            the projection
	 * @return the query build request
	 */
	public QueryBuildRequest addProjection(String projection) {
		addIf(projections, projection, StringUtils::isNoneBlank);
		return this;
	}

	/**
	 * Gets the projection names that should be added to the query
	 *
	 * @return the collection of projections if any or empty collection if no additional projections are added
	 */
	public Collection<String> getProjections() {
		return projections;
	}

	/**
	 * Gets the query name.
	 *
	 * @return the queryName
	 */
	public String getQueryName() {
		return queryName;
	}

	/**
	 * Gets the parameters.
	 *
	 * @return the parameters
	 */
	public List<Pair<String, Object>> getParameters() {
		return parameters;
	}

	/**
	 * Gets the specified parameters
	 *
	 * @param paramNames
	 *            the parameter names to retrieve
	 * @return the parameters mapping
	 */
	public Map<String, Object> getParameters(Set<String> paramNames) {
		return parameters.stream().filter(param -> paramNames.contains(param.getFirst())).collect(Pair.toMap());
	}

	/**
	 * Checks if there are any parameters defined.
	 *
	 * @return true, if there is at least one parameter defined
	 */
	public boolean hasParameters() {
		return !parameters.isEmpty();
	}

	/**
	 * Gets the filter names.
	 *
	 * @return the filterNames
	 */
	public List<String> getFilterNames() {
		return filterNames;
	}

	/**
	 * Gets the filters.
	 *
	 * @return the filters
	 */
	public List<Function<String, String>> getFilters() {
		return filters;
	}

	/**
	 * Gets all dynamic and named filters. The named filters will be resolved using the provided filter resolver
	 *
	 * @param filterResolver
	 *            the filter resolver to use to identify the named filters
	 * @return the all filters
	 */
	public List<Function<String, String>> getAllFilters(Function<String, Function<String, String>> filterResolver) {
		List<Function<String, String>> allFilters = new ArrayList<>(filterNames.size() + filters.size());
		allFilters.addAll(filters);
		allFilters
				.addAll(filterNames.stream().map(filterResolver).filter(Objects::nonNull).collect(Collectors.toList()));
		return allFilters;
	}

}
