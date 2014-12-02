package com.sirma.itt.pm.event;

import com.sirma.itt.emf.event.AbstractInstanceEvent;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * Event fired before {@link ProjectInstance} to be saved to database.
 * 
 * @author BBonev
 */
@Documentation("Event fired before {@link ProjectInstance} to be saved to database.")
public class ProjectSaveEvent extends AbstractInstanceEvent<ProjectInstance> {

	/**
	 * Instantiates a new project save event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public ProjectSaveEvent(ProjectInstance instance) {
		super(instance);
	}

}
