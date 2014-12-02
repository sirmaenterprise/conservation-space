package com.sirma.cmf.web.search.facet.event;

import java.util.List;

import com.sirma.cmf.web.search.facet.FacetSearchFilter;
import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * SearchFilterUpdateEvent if fired during ui building when particular search filters list is built.
 * The search filters can be retrieved from the event object and can be modified in observer.
 * 
 * @author svelikov
 */
@Documentation("SearchFilterUpdateEvent if fired during ui building when particular search filters list is built. The search filters can be retrieved from the event object and can be modified in observer.")
public class SearchFilterUpdateEvent implements EmfEvent {

	private List<FacetSearchFilter> facetSearchFilters;

	/**
	 * Getter method for facetSearchFilters.
	 * 
	 * @return the facetSearchFilters
	 */
	public List<FacetSearchFilter> getFacetSearchFilters() {
		return facetSearchFilters;
	}

	/**
	 * Setter method for facetSearchFilters.
	 * 
	 * @param facetSearchFilters
	 *            the facetSearchFilters to set
	 */
	public void setFacetSearchFilters(List<FacetSearchFilter> facetSearchFilters) {
		this.facetSearchFilters = facetSearchFilters;
	}

}
