package com.sirma.itt.pm.event;

import com.sirma.itt.emf.event.instance.InstanceChangeEvent;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * Event fired before saving a {@link ProjectInstance} to DMS/DB.
 * 
 * @author BBonev
 */
@Documentation("Event fired before saving a {@link ProjectInstance} to DMS/DB.")
public class ProjectChangeEvent extends InstanceChangeEvent<ProjectInstance> {

	/**
	 * Instantiates a new project change event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public ProjectChangeEvent(ProjectInstance instance) {
		super(instance);
	}

}
