package com.sirma.itt.pm.schedule.event;

import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.pm.schedule.model.ScheduleDependency;

/**
 * Event fired when schedule dependency is being deleted.
 * 
 * @author BBonev
 */
@Documentation("Event fired when schedule dependency is being deleted.")
public class ScheduleDependencyDeletedEvent implements EmfEvent {

	/** The dependency. */
	private final ScheduleDependency dependency;

	/**
	 * Instantiates a new schedule dependency deleted event.
	 * 
	 * @param dependency
	 *            the dependency
	 */
	public ScheduleDependencyDeletedEvent(ScheduleDependency dependency) {
		this.dependency = dependency;
	}

	/**
	 * Gets the dependency that is deleted
	 * 
	 * @return the dependency
	 */
	public ScheduleDependency getDependency() {
		return dependency;
	}

}
