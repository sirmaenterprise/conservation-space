package com.sirma.itt.pm.event;

import com.sirma.itt.emf.event.instance.BeforeInstanceCancelEvent;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * Event fired before cancellation of a {@link ProjectInstance}.
 * 
 * @author BBonev
 */
@SuppressWarnings("unchecked")
@Documentation("Event fired before cancellation of a {@link ProjectInstance}.")
public class BeforeProjectCancelEvent extends
		BeforeInstanceCancelEvent<ProjectInstance, AfterProjectCancelEvent> {

	/**
	 * Instantiates a new before project cancel event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public BeforeProjectCancelEvent(ProjectInstance instance) {
		super(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AfterProjectCancelEvent createNextEvent() {
		return new AfterProjectCancelEvent(getInstance());
	}

}
