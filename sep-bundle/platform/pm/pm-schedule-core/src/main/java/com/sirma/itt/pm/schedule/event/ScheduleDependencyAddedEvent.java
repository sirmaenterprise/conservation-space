package com.sirma.itt.pm.schedule.event;

import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.pm.schedule.model.ScheduleDependency;

/**
 * Event fired when new scheduleDependency is added between 2 schedule entries.
 * 
 * @author BBonev
 */
@Documentation("Event fired when new scheduleDependency is added between 2 schedule entries.")
public class ScheduleDependencyAddedEvent implements EmfEvent {

	/** The dependency. */
	private final ScheduleDependency dependency;

	/**
	 * Instantiates a new schedule dependency added event.
	 * 
	 * @param dependency
	 *            the dependency
	 */
	public ScheduleDependencyAddedEvent(ScheduleDependency dependency) {
		this.dependency = dependency;
	}

	/**
	 * Getter method for dependency.
	 * 
	 * @return the dependency
	 */
	public ScheduleDependency getDependency() {
		return dependency;
	}

}
