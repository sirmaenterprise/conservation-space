package com.sirma.itt.emf.definition.event;

import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event object fired to initialize all definition loading. The handler should start template
 * definition loading first before top level definition loading. The event could be marked as forced
 * to ignore the disabled configuration loading.
 * 
 * @author BBonev
 */
@Documentation("Event object fired to initialize all definition loading. "
		+ "The handler should start template definition loading first before top level definition loading. "
		+ "The event could be marked as forced to ignore the disabled configuration loading.")
public class LoadAllDefinitions implements EmfEvent {

	/** The forced. */
	private final boolean forced;

	/**
	 * Instantiates a new load all definitions.
	 */
	public LoadAllDefinitions() {
		this(false);
	}

	/**
	 * Instantiates a new load all definitions.
	 * 
	 * @param forced
	 *            the forced
	 */
	public LoadAllDefinitions(boolean forced) {
		this.forced = forced;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "LoadAllDefinitions";
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
