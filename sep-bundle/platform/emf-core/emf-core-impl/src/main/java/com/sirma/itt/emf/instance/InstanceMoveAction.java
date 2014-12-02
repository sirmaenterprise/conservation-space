package com.sirma.itt.emf.instance;

import com.sirma.itt.emf.instance.model.Instance;

/**
 * Defines interface for instance move operation. Implementation should tell whether it can handle
 * specific object type.
 * 
 * @author svelikov
 */
public interface InstanceMoveAction {

	/**
	 * If current implementation can handle instance of given type.
	 * 
	 * @param type
	 *            the type
	 * @return true, if successful
	 */
	boolean canHandle(Class<?> type);

	/**
	 * Implementation of move operation.
	 * 
	 * @param instanceToMove
	 *            the instance to move
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @return true, if successful
	 */
	boolean move(Instance instanceToMove, Instance source, Instance target);
}
