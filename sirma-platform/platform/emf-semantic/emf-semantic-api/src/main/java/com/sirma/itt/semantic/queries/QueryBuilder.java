/*
 *
 */
package com.sirma.itt.semantic.queries;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.Pair;

/**
 * SPARQL named query builder.
 *
 * @author BBonev
 */
public interface QueryBuilder {

	/**
	 * Builds predefined SPARQL query by given name
	 *
	 * @param <E>
	 *            the query parameters element type
	 * @param name
	 *            the query name
	 * @param params
	 *            the query parameters
	 * @return the the query or {@code null} in case no named query is defined for given name
	 */
	<E extends Pair<String, Object>> String buildQueryByName(String name, List<E> params);

	/**
	 * Builds predefined SPARQL query by given name.
	 *
	 * @param <E>
	 *            the query parameters element type
	 * @param name
	 *            the query name
	 * @param params
	 *            the query parameters
	 * @param filterNames
	 *            the filters to append to the query
	 * @return the the query or {@code null} in case no named query is defined for given name
	 */
	@SuppressWarnings("unchecked")
	default <E extends Pair<String, Object>> String buildQueryByName(String name, List<E> params,
			String... filterNames) {
		return buildQuery(new QueryBuildRequest(name)
				.addParameters((Collection<Pair<String, Object>>) params)
					.addFilters(Arrays.asList(filterNames)));
	}

	/**
	 * Builds a query using the data from the given build request
	 *
	 * @param buildRequest
	 *            the build request object to use for query building
	 * @return the the query or {@code null} in case no named query is defined for given name
	 */
	String buildQuery(QueryBuildRequest buildRequest);

	/**
	 * Builds the filter that consists of all specified filters
	 *
	 * @param filters
	 *            the filters to include
	 * @return the new filter function
	 */
	@SuppressWarnings("unchecked")
	default Function<String, String> buildComposite(Function<String, String>... filters) {
		return var -> Stream
				.of(filters)
					.map(f -> f.apply(var))
					.filter(StringUtils::isNoneBlank)
					.collect(Collectors.joining());
	}

	/**
	 * Builds a composite filter that consists from the specified filters and separated by the given delimiter. The
	 * filters will be applied in order and separated by the given delimiter
	 *
	 * @param delimiter
	 *            the delimiter to use
	 * @param filters
	 *            the filters to include
	 * @return the new filter function
	 */
	@SuppressWarnings("unchecked")
	default Function<String, String> buildComposite(String delimiter, Function<String, String>... filters) {
		return var -> Stream
				.of(filters)
					.map(f -> f.apply(var))
					.filter(StringUtils::isNoneBlank)
					.map(statement -> " { " + statement + " } ")
					.collect(Collectors.joining(delimiter, "", "."));
	}

	/**
	 * Builds a composite filter that consists from the specified filters and separated by UNION delimiter.
	 *
	 * @param filters
	 *            the filters to include
	 * @return the new filter function
	 */
	@SuppressWarnings("unchecked")
	default Function<String, String> buildUnion(Function<String, String>... filters) {
		return buildComposite("UNION", filters);
	}

	/**
	 * Builds a dynamic filter for single uri
	 *
	 * @param predicate
	 *            the predicate to use for matching the given value
	 * @param uri
	 *            the uri to look for
	 * @return a function that returns a statement that matches the given predicate and uri
	 */
	default Function<String, String> buildUriFilter(String predicate, String uri) {
		if (StringUtils.isBlank(uri)) {
			return var -> "";
		}
		final String value;
		if (!uri.startsWith("<") && uri.contains("http")) {
			value = "<" + uri + ">";
		} else {
			value = uri;
		}
		return var -> var + " " + predicate + " " + value + ".";
	}

	/**
	 * Builds a dynamic filter for multiple uries
	 *
	 * @param predicate
	 *            the predicate to use for matching the given value
	 * @param uries
	 *            the uries to look for
	 * @return a function that returns a statement that matches the given predicate and uries
	 */
	default Function<String, String> buildUriesFilter(String predicate, Collection<String> uries) {
		if (uries.size() == 1) {
			return buildValueFilter(predicate, uries.iterator().next());
		}
		return var -> uries
				.stream()
					.map(object -> buildUriFilter(predicate, object).apply(var))
					.filter(StringUtils::isNotBlank)
					.map(statement -> " { " + statement + " } ")
					.collect(Collectors.joining("UNION", "", "."));
	}

	/**
	 * Builds a dynamic filter for a variable
	 *
	 * @param predicate
	 *            the predicate to use for matching the given value
	 * @param variable
	 *            the variable name
	 * @return a function that returns a statement that matches the given predicate and variable
	 */
	default Function<String, String> buildViariableFilter(String predicate, String variable) {
		if (StringUtils.isBlank(variable)) {
			return var -> "";
		}
		String varName = variable.startsWith("?") ? variable : "?" + variable;
		return var -> var + " " + predicate + " " + varName + ".";
	}

	/**
	 * Builds a dynamic filter for single string value
	 *
	 * @param predicate
	 *            the predicate to use for matching the given value
	 * @param object
	 *            the object to look for
	 * @return a function that returns a statement that matches the given predicate and object
	 */
	default Function<String, String> buildValueFilter(String predicate, String object) {
		if (StringUtils.isBlank(object)) {
			return var -> "";
		}
		return var -> var + " " + predicate + " \"" + object + "\". ";
	}

	/**
	 * Builds a dynamic filter for single string value
	 *
	 * @param predicate
	 *            the predicate to use for matching the given value
	 * @param object
	 *            the object to look for
	 * @return a function that returns a statement that matches the given predicate and object
	 */
	default Function<String, String> buildValueFilter(String predicate, Object object) {
		return buildValueFilter(predicate, Objects.toString(object, null));
	}

	/**
	 * Builds a dynamic filter for multiple string values
	 *
	 * @param predicate
	 *            the predicate to use for matching the given value
	 * @param objects
	 *            the objects to look for
	 * @return a function that returns a statement that matches the given predicate and objects
	 */
	default Function<String, String> buildStringValuesFilter(String predicate, Collection<String> objects) {
		if (objects.size() == 1) {
			return buildValueFilter(predicate, objects.iterator().next());
		}
		return var -> objects
				.stream()
					.map(object -> buildValueFilter(predicate, object).apply(var))
					.filter(StringUtils::isNotBlank)
					.collect(Collectors.joining("UNION", "", "."));
	}

	/**
	 * Builds a dynamic filter for multiple values
	 *
	 * @param predicate
	 *            the predicate to use for matching the given value
	 * @param objects
	 *            the objects to look for
	 * @return a function that returns a statement that matches the given predicate and objects
	 */
	default Function<String, String> buildValuesFilter(String predicate, Collection<Object> objects) {
		if (objects.size() == 1) {
			return buildValueFilter(predicate, objects.iterator().next());
		}
		return var -> objects
				.stream()
					.map(object -> buildValueFilter(predicate, object).apply(var))
					.filter(StringUtils::isNoneBlank)
					.map(statement -> " { " + statement + " } ")
					.collect(Collectors.joining("UNION", "", "."));
	}
}