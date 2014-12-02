package com.sirma.itt.pm.event;

import com.sirma.itt.emf.event.instance.InstancePersistedEvent;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * Event fired when {@link ProjectInstance} has been persisted.
 * 
 * @author BBonev
 */
@Documentation("Event fired when {@link ProjectInstance} has been persisted.")
public class ProjectPersistedEvent extends InstancePersistedEvent<ProjectInstance> {

	/**
	 * Instantiates a new project persisted event.
	 * 
	 * @param instance
	 *            the instance
	 * @param old
	 *            the old
	 * @param operationId
	 *            the operation id
	 */
	public ProjectPersistedEvent(ProjectInstance instance, ProjectInstance old, String operationId) {
		super(instance, old, operationId);
	}

}
