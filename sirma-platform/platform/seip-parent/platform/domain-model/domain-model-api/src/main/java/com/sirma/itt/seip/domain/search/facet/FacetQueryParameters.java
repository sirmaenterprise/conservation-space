package com.sirma.itt.seip.domain.search.facet;

import com.sirma.itt.seip.domain.search.SearchRequest;

/**
 * Properties related to the facets.
 *
 * @author Mihail Radkov
 * @since 1.10.1
 */
public interface FacetQueryParameters {
	/**
	 * Constant for property in {@link SearchRequest} defining if the search should include facets.
	 */
	String REQUEST_FACET = "facet";

	/**
	 * Constant to get the facet arguments from {@link SearchRequest}.
	 */
	String REQUEST_FACET_ARGUMENTS = "facetArguments[]";

	/**
	 * Constant for parameter in {@link SearchRequest} to define if the facet configurations have to be ignored and all
	 * fields to be faceted, no matter if they have a configuration or not.
	 */
	String REQUEST_IGNORE_FACET_CONFIGURATION = "ignoreFacetConfiguration";

	/**
	 * Indicator for the facets that must be loaded provided in {@link SearchRequest}.
	 */
	String REQUEST_FACET_FIELD = "facetField[]";

	/**
	 * Constant for a facet with no value.
	 */
	String NO_VALUE = "NO_VALUE";

	/**
	 * Constant used to separate facet field and selected facet value in the provided {@link #REQUEST_FACET_ARGUMENTS}.
	 * Example: "status:APPROVED".
	 */
	char ARGUMENT_SEPARATOR = ':';

	/**
	 * Constant used to separate selected date facet values in the provided {@link #REQUEST_FACET_ARGUMENTS}. Example:
	 * "createdOn:*;*".
	 */
	String DATE_SEPARATOR = ";";

	/**
	 * Constant for unspecified date used when building date queries.
	 */
	String DATE_UNSPECIFIED = "*";
}
