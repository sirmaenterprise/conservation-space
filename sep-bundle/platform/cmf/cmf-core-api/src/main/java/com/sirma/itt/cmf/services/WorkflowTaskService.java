package com.sirma.itt.cmf.services;

import com.sirma.itt.cmf.beans.definitions.TaskDefinitionRef;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.state.operation.Operation;

/**
 * Service to represent the concrete service for workflow task instances.
 * 
 * @author BBonev
 */
public interface WorkflowTaskService extends InstanceService<TaskInstance, TaskDefinitionRef> {

	@Override
	TaskInstance save(TaskInstance instance, Operation operation);
}
