package com.sirma.itt.pm.event;

import com.sirma.itt.emf.event.instance.InstanceOpenEvent;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * Event fired before project to be visualized on screen.
 * 
 * @author svelikov
 */
@Documentation("Event fired before project to be visualized on screen.")
public class ProjectOpenEvent extends InstanceOpenEvent<ProjectInstance> {

	/**
	 * Instantiates a new project open event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public ProjectOpenEvent(ProjectInstance instance) {
		super(instance);
	}

}
