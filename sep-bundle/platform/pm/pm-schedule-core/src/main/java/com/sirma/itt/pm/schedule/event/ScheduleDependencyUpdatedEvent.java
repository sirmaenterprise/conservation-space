package com.sirma.itt.pm.schedule.event;

import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.pm.schedule.model.ScheduleDependency;

/**
 * Event fired when schedule dependency is being updated. The most cases this is when the duration is changed.
 *
 * @author BBonev
 */
@Documentation("Event fired when schedule dependency is being updated. The most cases this is when the duration is changed.")
public class ScheduleDependencyUpdatedEvent implements EmfEvent {
	/** The dependency. */
	private final ScheduleDependency oldDependency;

	/** The new dependency. */
	private final ScheduleDependency newDependency;

	/**
	 * Instantiates a new schedule dependency updated event.
	 *
	 * @param oldDependency the old dependency
	 * @param newDependency the new dependency
	 */
	public ScheduleDependencyUpdatedEvent(ScheduleDependency oldDependency,
			ScheduleDependency newDependency) {
		this.oldDependency = oldDependency;
		this.newDependency = newDependency;
	}

	/**
	 * Getter method for oldDependency.
	 *
	 * @return the oldDependency
	 */
	public ScheduleDependency getOldDependency() {
		return oldDependency;
	}

	/**
	 * Getter method for newDependency.
	 *
	 * @return the newDependency
	 */
	public ScheduleDependency getNewDependency() {
		return newDependency;
	}

}
