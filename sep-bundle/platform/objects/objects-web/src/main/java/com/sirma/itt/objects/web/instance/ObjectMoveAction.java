package com.sirma.itt.objects.web.instance;

import javax.inject.Inject;

import com.sirma.itt.emf.instance.InstanceMoveAction;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.objects.domain.model.ObjectInstance;
import com.sirma.itt.objects.services.ObjectService;

/**
 * Implementation of move operation for object instance.
 * 
 * @author svelikov
 */
public class ObjectMoveAction implements InstanceMoveAction {

	@Inject
	private ObjectService objectService;

	@Override
	public boolean canHandle(Class<?> type) {
		return type.isAssignableFrom(ObjectInstance.class);
	}

	@Override
	public boolean move(Instance instanceToMove, Instance source, Instance target) {
		boolean moved = objectService.move((ObjectInstance) instanceToMove, source, target);
		return moved;
	}

}
