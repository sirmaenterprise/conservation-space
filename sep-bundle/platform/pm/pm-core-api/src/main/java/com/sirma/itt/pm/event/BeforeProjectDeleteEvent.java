package com.sirma.itt.pm.event;

import com.sirma.itt.emf.event.instance.BeforeInstanceDeleteEvent;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * Event fired before deletion of a {@link ProjectInstance}.
 * 
 * @author BBonev
 */
@SuppressWarnings("unchecked")
@Documentation("Event fired before deletion of a {@link ProjectInstance}.")
public class BeforeProjectDeleteEvent extends
		BeforeInstanceDeleteEvent<ProjectInstance, AfterProjectDeleteEvent> {

	/**
	 * Instantiates a new before project persist event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public BeforeProjectDeleteEvent(ProjectInstance instance) {
		super(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AfterProjectDeleteEvent createNextEvent() {
		return new AfterProjectDeleteEvent(getInstance());
	}

}
