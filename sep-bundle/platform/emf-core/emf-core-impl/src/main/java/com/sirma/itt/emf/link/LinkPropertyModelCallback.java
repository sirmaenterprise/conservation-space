package com.sirma.itt.emf.link;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.sirma.itt.emf.domain.ObjectTypes;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.link.entity.LinkEntity;
import com.sirma.itt.emf.properties.DefaultPropertyModelCallback;

/**
 * Property model callback that can handle link properties.
 * 
 * @author BBonev
 */
@InstanceType(type = ObjectTypes.LINK)
public class LinkPropertyModelCallback extends DefaultPropertyModelCallback {

	@Override
	public Set<Class<?>> getSupportedObjects() {
		return new HashSet<Class<?>>(Arrays.asList(LinkInstance.class, LinkReference.class,
				LinkEntity.class));
	}
}
