package com.sirma.itt.pm.services;

import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.pm.domain.definitions.ProjectDefinition;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * Service for creating and managing project instances.
 *
 * @author BBonev
 */
public interface ProjectService extends InstanceService<ProjectInstance, ProjectDefinition> {

	@Override
	ProjectInstance save(ProjectInstance instance, Operation operation);

	@Override
	void delete(ProjectInstance instance, Operation operation, boolean permanent);

	@Override
	ProjectInstance cancel(ProjectInstance instance);
}
