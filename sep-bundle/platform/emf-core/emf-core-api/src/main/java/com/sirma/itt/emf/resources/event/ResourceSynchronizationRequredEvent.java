package com.sirma.itt.emf.resources.event;

import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when resource synchronization is required. The user could issue forced
 * synchronization if needed.
 * 
 * @author BBonev
 */
@Documentation("Event fired when resource synchronization is required. The user could issue forced synchronization if needed.")
public class ResourceSynchronizationRequredEvent implements EmfEvent {

	/** The forced. */
	private final boolean forced;

	/**
	 * Instantiates a new resource synchronization event.
	 * 
	 * @param forced
	 *            the forced
	 */
	public ResourceSynchronizationRequredEvent(boolean forced) {
		this.forced = forced;
	}

	/**
	 * Checks if is forced.
	 * 
	 * @return true, if is forced
	 */
	public boolean isForced() {
		return forced;
	}

}
