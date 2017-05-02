package com.sirma.itt.seip.resources;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.convert.AbstractInstanceToInstanceReferenceConverterProvider;
import com.sirma.itt.seip.model.LinkSourceId;
import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.EmfUser;

/**
 * Adds instance reference conversion for users and any other instances from EMF
 * module.
 *
 * @author BBonev
 */
@ApplicationScoped
public class ResourceInstanceToInstanceReferenceConverterProvider
		extends AbstractInstanceToInstanceReferenceConverterProvider {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(TypeConverter converter) {
		addEntityConverter(converter, EmfUser.class, LinkSourceId.class);
		addEntityConverter(converter, EmfUser.class, InstanceReference.class);
		addEntityConverter(converter, EmfGroup.class, LinkSourceId.class);
		addEntityConverter(converter, EmfGroup.class, InstanceReference.class);
	}

}
