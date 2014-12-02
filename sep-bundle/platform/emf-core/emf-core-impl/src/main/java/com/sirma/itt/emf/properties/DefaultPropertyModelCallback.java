package com.sirma.itt.emf.properties;

import java.util.Collections;
import java.util.Set;

import com.sirma.itt.emf.domain.ObjectTypes;
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;

/**
 * Default property model callback for handling instance properties. The
 * callback handles only single level model.
 *
 * @author BBonev
 */
@InstanceType(type = ObjectTypes.DEFAULT)
public class DefaultPropertyModelCallback extends BasePropertyModelCallback<Instance> {

	@Override
	public boolean canHandle(Object model) {
		if (model instanceof Entity) {
			return getEntityTypeIdentifier((Entity<?>) model) != 0;
		}
		return false;
	}

	@Override
	public Set<Class<?>> getSupportedObjects() {
		return Collections.emptySet();
	}

}
