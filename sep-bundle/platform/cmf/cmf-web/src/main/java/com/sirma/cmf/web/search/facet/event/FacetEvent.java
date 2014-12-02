package com.sirma.cmf.web.search.facet.event;

import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * FacetEvent is fired when some facet filter is selected by the user.
 * 
 * @author svelikov
 */
@Documentation("FacetEvent is fired when some facet filter is selected by the user.")
public class FacetEvent implements EmfEvent {

	/** The active filter name. */
	private String activeFilterName;

	/**
	 * Instantiates a new facet event.
	 * 
	 * @param activeFilterName
	 *            the active filter name
	 */
	public FacetEvent(String activeFilterName) {
		this.activeFilterName = activeFilterName;
	}

	/**
	 * Getter method for activeFilterName.
	 * 
	 * @return the activeFilterName
	 */
	public String getActiveFilterName() {
		return activeFilterName;
	}

	/**
	 * Setter method for activeFilterName.
	 * 
	 * @param activeFilterName
	 *            the activeFilterName to set
	 */
	public void setActiveFilterName(String activeFilterName) {
		this.activeFilterName = activeFilterName;
	}

}
