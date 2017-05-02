package com.sirma.itt.seip.search.facet;

import com.sirma.itt.seip.Resettable;
import com.sirma.itt.seip.domain.search.facet.FacetConfiguration;

/**
 * Provides {@link FacetConfiguration} objects.
 *
 * @author nvelkov
 */
public interface FacetConfigurationProvider extends Resettable {

	/**
	 * Get the {@link FacetConfiguration} by it's facet name.
	 *
	 * @param facetName
	 *            the name of the facet
	 * @return the facet configuration
	 */
	FacetConfiguration getFacetConfigField(String facetName);
}
