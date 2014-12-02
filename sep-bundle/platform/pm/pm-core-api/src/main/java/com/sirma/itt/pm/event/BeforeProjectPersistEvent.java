package com.sirma.itt.pm.event;

import com.sirma.itt.emf.event.instance.BeforeInstancePersistEvent;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * Event fired before first persist of a {@link ProjectInstance}
 * 
 * @author BBonev
 */
@SuppressWarnings("unchecked")
@Documentation("Event fired before first persist of a {@link ProjectInstance}")
public class BeforeProjectPersistEvent extends
		BeforeInstancePersistEvent<ProjectInstance, AfterProjectPersistEvent> {

	/**
	 * Instantiates a new before project persist event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public BeforeProjectPersistEvent(ProjectInstance instance) {
		super(instance);
	}

	@Override
	protected AfterProjectPersistEvent createNextEvent() {
		return new AfterProjectPersistEvent(getInstance());
	}

}
