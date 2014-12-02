package com.sirma.itt.cmf.services;

import com.sirma.itt.cmf.beans.model.FolderInstance;
import com.sirma.itt.emf.definition.model.GenericDefinition;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.state.operation.Operation;

/**
 * Specific instance service for {@link FolderInstance}s.
 * 
 * @author BBonev
 */
public interface FolderService extends InstanceService<FolderInstance, GenericDefinition> {
	/**
	 * {@inheritDoc}
	 */
	@Override
	FolderInstance save(FolderInstance instance, Operation operation);
}
