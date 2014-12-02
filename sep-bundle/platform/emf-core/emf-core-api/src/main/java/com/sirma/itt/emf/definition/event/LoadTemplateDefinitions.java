package com.sirma.itt.emf.definition.event;

import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event that initiates template definition loading. The event could be marked as forced to ignore
 * the disabled configuration loading.
 * 
 * @author BBonev
 */
@Documentation("Event that initiates template definition loading. The event could be marked as forced to ignore the disabled configuration loading.")
public class LoadTemplateDefinitions implements EmfEvent {

	/** The forced. */
	private final boolean forced;

	/**
	 * Instantiates a new load template definitions.
	 */
	public LoadTemplateDefinitions() {
		this(false);
	}

	/**
	 * Instantiates a new load template definitions.
	 * 
	 * @param forced
	 *            the forced
	 */
	public LoadTemplateDefinitions(boolean forced) {
		this.forced = forced;
	}

	/**
	 * Getter method for forced.
	 * 
	 * @return the forced
	 */
	public boolean isForced() {
		return forced;
	}
}
