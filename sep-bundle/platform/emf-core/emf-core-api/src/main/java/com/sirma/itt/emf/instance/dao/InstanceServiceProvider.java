package com.sirma.itt.emf.instance.dao;

import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.instance.model.Instance;

/**
 * Provider that adds an option to fetch an instance service based on a handling class.
 * 
 * @author BBonev
 */
public interface InstanceServiceProvider extends InstanceService<Instance, DefinitionModel> {

	/**
	 * Gets the service instance based on a instance class implementation.
	 * 
	 * @param <I>
	 *            the required instance type
	 * @param <D>
	 *            the the definition that is linked with the instance type
	 * @param instanceClass
	 *            the instance class
	 * @return the service implementation or <code>null</code> if the instance class is not
	 *         supported
	 */
	<I extends Instance, D extends DefinitionModel> InstanceService<I, D> getService(
			Class<I> instanceClass);
}
