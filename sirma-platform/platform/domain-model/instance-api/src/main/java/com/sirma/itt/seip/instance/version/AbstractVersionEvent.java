package com.sirma.itt.seip.instance.version;

import com.sirma.itt.seip.event.EmfEvent;

/**
 * Base event class for events that are fired when processing instance versions.
 *
 * @author A. Kunchev
 */
public abstract class AbstractVersionEvent implements EmfEvent {

	private VersionContext context;

	/**
	 * Instantiates a new abstract version event.
	 *
	 * @param context
	 *            contains data required for version creation
	 */
	public AbstractVersionEvent(VersionContext context) {
		this.context = context;
	}

	public VersionContext getContext() {
		return context;
	}

}
