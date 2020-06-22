package com.sirma.itt.seip.instance.dao;

import java.io.Serializable;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Converter that is used on instance persist to convert the instance to entity
 *
 * @author BBonev
 */
@FunctionalInterface
public interface EntityConverter {

	/**
	 * Convert instance to entity
	 *
	 * @param instance
	 *            the instance to convert
	 * @return the converted entity
	 */
	Entity<? extends Serializable> convertToEntity(Instance instance);

}
