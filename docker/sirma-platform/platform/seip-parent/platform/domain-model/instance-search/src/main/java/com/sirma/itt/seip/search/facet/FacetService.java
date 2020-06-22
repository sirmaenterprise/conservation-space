package com.sirma.itt.seip.search.facet;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.facet.FacetConfiguration;

/**
 * Service for calculating facets based on input {@link SearchArguments}.
 */
public interface FacetService {

	/**
	 * Prepares the search arguments for a faceted search. Gets possible facet fields based on the selected object types
	 * & sub types.
	 *
	 * @param request
	 *            - the request from which the arguments are retrieved
	 * @param searchArgs
	 *            - the search arguments to prepare
	 */
	<E extends Instance, S extends SearchArguments<E>> void prepareArguments(SearchRequest request, S searchArgs);

	/**
	 * Performs faceting and return results into the {@link SearchArguments} parameter.
	 *
	 * @param <E>
	 *            the searched object type
	 * @param <S>
	 *            the build predefined filter arguments type
	 * @param arguments
	 *            search arguments to execute
	 */
	<E extends Instance, S extends SearchArguments<E>> void facet(S arguments);

	/**
	 * Filter the object facets by removing the section objects, applying permissions and adding breadcrumb headers to
	 * the returned facet values.
	 *
	 * @param <E>
	 *            the searched object type
	 * @param <S>
	 *            the build predefined filter arguments type
	 * @param arguments
	 *            search arguments to execute
	 */
	<E extends Instance, S extends SearchArguments<E>> void filterObjectFacets(S arguments);

	/**
	 * Assigns labels to the provided facets depending on their properties (code list, Solr type etc.).
	 *
	 * @param <E>
	 *            the searched object type
	 * @param <S>
	 *            the build predefined filter arguments type
	 * @param arguments
	 *            the search arguments containing the facets
	 */
	<E extends Instance, S extends SearchArguments<E>> void assignLabels(S arguments);

	/**
	 * Sorts the provided facets depending on their {@link FacetConfiguration}.
	 *
	 * @param <E>
	 *            the searched object type
	 * @param <S>
	 *            the build predefined filter arguments type
	 * @param arguments
	 *            the search arguments containing the facets
	 */
	<E extends Instance, S extends SearchArguments<E>> void sort(S arguments);

	/**
	 * Get all available facets depending on the {@link SearchRequest}. A semantic search will be performed based on the
	 * request for the retrieval of the ids and instance types of the results so the available facets can be retrieved
	 * based on those results.
	 *
	 * @param request
	 *            the search request
	 * @return the search arguments containing the facets
	 */
	<E extends Instance, S extends SearchArguments<E>> S getAvailableFacets(SearchRequest request);
}