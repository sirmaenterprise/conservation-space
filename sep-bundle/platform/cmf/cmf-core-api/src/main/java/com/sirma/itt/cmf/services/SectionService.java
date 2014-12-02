package com.sirma.itt.cmf.services;

import com.sirma.itt.cmf.beans.definitions.SectionDefinition;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.state.operation.Operation;

/**
 * Specific instance service for {@link SectionInstance}s.
 * 
 * @author BBonev
 */
public interface SectionService extends InstanceService<SectionInstance, SectionDefinition> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	SectionInstance save(SectionInstance instance, Operation operation);

}
