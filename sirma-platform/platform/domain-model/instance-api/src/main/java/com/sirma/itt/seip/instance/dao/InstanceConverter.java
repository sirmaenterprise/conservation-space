package com.sirma.itt.seip.instance.dao;

import java.io.Serializable;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Converter that is used on instance loading to convert the entity to instance
 *
 * @author BBonev
 */
@FunctionalInterface
public interface InstanceConverter {

	/**
	 * Convert entity to instance
	 *
	 * @param entity
	 *            the entity to convert
	 * @return the converted instance
	 */
	Instance convertToInstance(Entity<? extends Serializable> entity);

}
