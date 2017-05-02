package com.sirma.itt.seip.search.facet;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.SearchableProperty;
import com.sirma.itt.seip.domain.search.facet.Facet;
import com.sirma.itt.seip.domain.search.facet.FacetConfiguration;

/**
 * Helps to prepare and transform arguments for the faceted search.
 *
 * @author Mihail Radkov
 * @since 1.10.1
 */
public interface FacetArgumentTransformer {

	/**
	 * <p>
	 * Based on the provided {@link SearchRequest}, retrieves the according {@link SearchableProperty} and converts them
	 * to {@link Facet}s which are put in the given {@link SearchArguments} for later use in the {@link SearchEngine}s.
	 * </p>
	 * <p>
	 * Additionally, it puts the default RDF {@link Facet} that represents the main object types in the system.
	 * </p>
	 * <p>
	 * <b>NOTE</b>: To facet by a certain property, it must have a {@link FacetConfiguration}.
	 * </p>
	 *
	 * @param request
	 *            - the provided search request
	 * @param searchArgs
	 *            - the provided search arguments to be
	 */
	<E extends Instance, S extends SearchArguments<E>> void populateFacets(SearchRequest request, S searchArgs);

	/**
	 * Extracts the facet arguments from the search request and maps them to the corresponding facets in the search
	 * arguments.
	 *
	 * @param request
	 *            - the search request
	 * @param searchArgs
	 *            - the search arguments
	 */
	<E extends Instance, S extends SearchArguments<E>> void preserveFacetSelections(SearchRequest request,
			S searchArgs);

	/**
	 * Merges any additional filter queries from the search request into the search arguments. Used when providing
	 * filter queries from the web (the reporting widget for example). <br />
	 * <b>Note</b>: The filters must be like "field:value" to be merged.
	 *
	 * @param request
	 *            - the search request
	 * @param searchArgs
	 *            - the search arguments
	 */
	<E extends Instance, S extends SearchArguments<E>> void mergeAdditionalFilterQueries(SearchRequest request,
			S searchArgs);

	/**
	 * Prepares facet dates arguments. If no provided values exists in the request, default ones will be put in the
	 * search arguments from {@link FacetConfigurationProperties}.
	 *
	 * @param request
	 *            - the search request
	 * @param arguments
	 *            - the search arguments
	 */
	<E extends Instance, S extends SearchArguments<E>> void prepareDateArguments(SearchRequest request, S arguments);

	/**
	 * Converts the provided {@link SearchableProperty} to a {@link Facet}. Creates new facet and copies the data from
	 * the property.
	 *
	 * @param property
	 *            - the provided property
	 * @return - the facet
	 */
	Facet searchablePropertyToFacet(SearchableProperty property);
}
