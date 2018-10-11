/**
 *
 */
package com.sirma.itt.seip.search.facet;

import java.util.Collection;

import com.sirma.itt.seip.domain.search.facet.Facet;
import com.sirma.itt.seip.domain.search.facet.FacetConfiguration;

/**
 * Service for sorting facets and their values. The sorting depends on valid {@link FacetConfiguration} in the provided
 * {@link Facet}s.
 *
 * @author Mihail Radkov
 * @since 1.10.1
 */
public interface FacetSortService {

	/**
	 * Sorts the provided {@link Collection} of {@link Facet}. To be sorted, the facets must have
	 * {@link FacetConfiguration} with valid data.
	 *
	 * @param facets
	 *            the facets to sort
	 * @return a new {@link Collection} with the sorted facets and their sorted values
	 */
	Collection<Facet> sort(Collection<Facet> facets);
}
