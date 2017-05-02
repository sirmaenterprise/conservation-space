package com.sirma.itt.semantic.queries;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.sirma.itt.seip.Named;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * Callback used when building dynamic semantic queries. The callback provides information about the query that is going
 * to be build using it. If the query does not have any iterable arguments the methods {@link #getStart()} and
 * {@link #getEnd()} will be the only one called.
 *
 * @author BBonev
 */
public interface QueryBuilderCallback extends Named, Plugin {
	String PLUGIN_NAME = "QueryBuilderCallback";
	String UNION = "\n\t UNION";

	/**
	 * Gets the start of the query.
	 *
	 * @param filters
	 *            the filters to add if any
	 * @param projections
	 *            list to add to the query
	 * @return the start of a query. Should not be null.
	 */
	String getStart(List<Function<String, String>> filters, Collection<String> projections);

	/**
	 * Gets the end of the query
	 *
	 * @return the end of the query if needed. If not return empty string but not null.
	 */
	String getEnd();

	/**
	 * Single value query. intermediate query to be build using the given argument.
	 *
	 * @param object
	 *            a single value from collection of main parameter
	 * @param params
	 *            the complete mapping of the requested parameters
	 * @param filters
	 *            the filters to add if any
	 * @return the string of the query to be joined using UNION. If returned empty string or <code>null</code> it will
	 *         be skipped
	 */
	String singleValue(Serializable object, Map<String, Object> params, List<Function<String, String>> filters);

	/**
	 * Name of the parameter that is collection of vales.
	 *
	 * @return the string
	 */
	String collectionParamName();

	/**
	 * The parameter names that are expected to be passed when building the query.
	 *
	 * @return the sets of parameter names, including the {@link #collectionParamName()}
	 */
	Set<String> paramNames();

	/**
	 * Returns the separator between the single elements. UNION by default or a custom separator
	 * 
	 * @return The separator between the single elements
	 */
	default String getSeparator() {
		return UNION;
	}
}