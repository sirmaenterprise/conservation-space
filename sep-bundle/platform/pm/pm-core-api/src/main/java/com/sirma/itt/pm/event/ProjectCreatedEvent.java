package com.sirma.itt.pm.event;

import com.sirma.itt.emf.event.AbstractInstanceEvent;
import com.sirma.itt.emf.event.HandledEvent;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * Event fired when {@link ProjectInstance} has been created and saved for the first time. The event
 * is fired after project creation. If the instance is modified in the event observer the flag
 * handled should be set to <code>true</code> so the changes are merged.
 * 
 * @author BBonev
 */
@Documentation("Event fired when {@link ProjectInstance} has been created and saved for the first time."
		+ " The event is fired after project creation. If the instance is modified in the event observer the flag"
		+ " handled should be set to <code>true</code> so the changes are merged.")
public class ProjectCreatedEvent extends AbstractInstanceEvent<ProjectInstance> implements
		HandledEvent {

	/** The handled. */
	private boolean handled;

	/**
	 * Instantiates a new project created event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public ProjectCreatedEvent(ProjectInstance instance) {
		super(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isHandled() {
		return handled;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setHandled(boolean handled) {
		this.handled = handled;
	}

}
