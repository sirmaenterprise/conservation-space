package com.sirma.itt.seip.instance.properties;

import java.util.Collections;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.domain.instance.PropertyModel;
import com.sirma.itt.seip.instance.dao.InstanceType;

/**
 * Default property model callback for handling instance properties. The callback handles only single level model.
 *
 * @author BBonev
 */
@ApplicationScoped
@InstanceType(type = "default")
public class DefaultPropertyModelCallback extends BasePropertyModelCallback<PropertyModel>
		implements PropertyModelCallback<PropertyModel> {

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
