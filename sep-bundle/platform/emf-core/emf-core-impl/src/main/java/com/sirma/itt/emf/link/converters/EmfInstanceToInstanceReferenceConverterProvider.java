package com.sirma.itt.emf.link.converters;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.entity.LinkSourceId;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.security.model.EmfGroup;
import com.sirma.itt.emf.security.model.EmfUser;

/**
 * Adds instance reference conversion for users and any other instances from EMF module.
 * 
 * @author BBonev
 */
@ApplicationScoped
public class EmfInstanceToInstanceReferenceConverterProvider extends
		AbstractInstanceToInstanceReferenceConverterProvider {

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
