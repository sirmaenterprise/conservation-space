package com.sirma.cmf.web.search.facet;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import com.sirma.cmf.web.search.SearchActionBase;
import com.sirma.cmf.web.search.facet.event.SearchFilterUpdateEvent;
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.search.model.SearchArguments;

/**
 * Action class backing the facet search component.
 * 
 * @param <E>
 *            the element type
 * @param <A>
 *            the generic type
 * @author svelikov
 */
public abstract class FacetSearchAction<E extends Entity, A extends SearchArguments<E>> extends
		SearchActionBase<E, A> {

	/** The search filter update event. */
	@Inject
	@Any
	protected Event<SearchFilterUpdateEvent> searchFilterUpdateEvent;

	/**
	 * Creates the filter.
	 * 
	 * @param filterName
	 *            the filter name
	 * @return the facet search filter
	 */
	protected FacetSearchFilter createFilter(String filterName) {
		FacetSearchFilter filter = new FacetSearchFilter(filterName);
		filter.setLabelProvider(labelProvider);
		return filter;
	}

	/**
	 * Creates the filter.
	 * 
	 * @param filterName
	 *            the filter name
	 * @param disabled
	 *            the disabled
	 * @param rendered
	 *            the rendered
	 * @return the facet search filter
	 */
	protected FacetSearchFilter createFilter(String filterName, boolean disabled, boolean rendered) {

		FacetSearchFilter filter = new FacetSearchFilter(filterName, disabled, rendered);
		filter.setLabelProvider(labelProvider);

		return filter;
	}

}
