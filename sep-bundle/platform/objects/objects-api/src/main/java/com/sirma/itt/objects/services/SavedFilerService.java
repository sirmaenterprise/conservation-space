package com.sirma.itt.objects.services;

import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.objects.domain.definitions.ObjectDefinition;
import com.sirma.itt.objects.domain.model.SavedFilter;

/**
 * Specific service for managing {@link SavedFilter}s.
 * 
 * @author BBonev
 */
public interface SavedFilerService extends InstanceService<SavedFilter, ObjectDefinition> {

	@Override
	public SavedFilter save(SavedFilter instance, Operation operation);

}
