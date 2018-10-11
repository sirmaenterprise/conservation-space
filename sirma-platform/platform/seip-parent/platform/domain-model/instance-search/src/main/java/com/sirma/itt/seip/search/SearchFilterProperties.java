package com.sirma.itt.seip.search;

import com.sirma.itt.seip.domain.search.Query;

/**
 * Constants used when building filters to pass the filters arguments.
 *
 * @author BBonev
 */
public interface SearchFilterProperties {

	/**
	 * The instance context. Used when the filter should search in a specific context identified by the given instance.
	 * The search key should be passed via {@link #INSTANCE_CONTEXT_KEY} value.
	 *
	 * @see #INSTANCE_CONTEXT_KEY
	 */
	String INSTANCE_CONTEXT = "context";

	/**
	 * The instance context key. Used with {@link #INSTANCE_CONTEXT} to specify the property on witch is specified the
	 * context identifier so that the given context is search on. Used with {@link #INSTANCE_CONTEXT} to pass the value
	 * to search for.
	 *
	 * @see #INSTANCE_CONTEXT
	 */
	String INSTANCE_CONTEXT_KEY = "contextKey";

	/**
	 * The date range query. Used then the filter search should be limited in a specific date interval passed via
	 * {@link Query} object.
	 */
	String DATE_RANGE_QUERY = "dateRangeQuery";

	/** The user id. Used when the filter should limit the results by the specified user identifier. */
	String USER_ID = "user";

	/**
	 * The include owner. Used then the filter should include in the results data for the current user.
	 */
	String INCLUDE_OWNER = "includeOwner";

	/** The filter. Used to pass an additional text filter argument to the created filter. */
	String FILTER = "filter";
}
