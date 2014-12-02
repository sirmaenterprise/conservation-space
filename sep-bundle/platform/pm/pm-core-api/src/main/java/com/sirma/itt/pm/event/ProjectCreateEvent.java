package com.sirma.itt.pm.event;

import com.sirma.itt.emf.event.instance.InstanceCreateEvent;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * Event fired when new {@link ProjectInstance} is created and his properties has been populated just before the instance is displayed to the user.
 *
 * @author BBonev
 */
@Documentation("Event fired when new {@link ProjectInstance} is created and his properties has been populated just before the instance is displayed to the user.")
public class ProjectCreateEvent extends InstanceCreateEvent<ProjectInstance> {

	/**
	 * Instantiates a new project create event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public ProjectCreateEvent(ProjectInstance instance) {
		super(instance);
	}

}
