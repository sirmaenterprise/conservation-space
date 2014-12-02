package com.sirma.itt.objects.services;

import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.objects.domain.definitions.ObjectDefinition;
import com.sirma.itt.objects.domain.model.ObjectInstance;

/**
 * Specific service for managing {@link ObjectInstance}s.
 * 
 * @author BBonev
 */
public interface ObjectService extends InstanceService<ObjectInstance, ObjectDefinition> {

	@Override
	public ObjectInstance save(ObjectInstance instance, Operation operation);

	/**
	 * Moves the given object from the given parent to the given destination. If the source is
	 * <code>null</code> the object will only be attached to the new parent. If destination is
	 * <code>null</code> the object will be detached only. If both are <code>null</code> the method
	 * does nothing!
	 * 
	 * @param objectInstance
	 *            the object instance
	 * @param source
	 *            the source
	 * @param destination
	 *            the destination
	 * @return true, if successful
	 */
	public boolean move(ObjectInstance objectInstance, Instance source, Instance destination);

}
