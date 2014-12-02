package com.sirma.itt.objects.converter;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.entity.LinkSourceId;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.link.converters.AbstractInstanceToInstanceReferenceConverterProvider;
import com.sirma.itt.objects.domain.model.ObjectInstance;

/**
 * Adds {@link ObjectInstance} to {@link InstanceReference} conversions.
 * 
 * @author BBonev
 */
@ApplicationScoped
public class ObjectInstanceToInstanceReferenceConverterProvider extends
		AbstractInstanceToInstanceReferenceConverterProvider {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(TypeConverter converter) {
		addEntityConverter(converter, ObjectInstance.class, InstanceReference.class);
		addEntityConverter(converter, ObjectInstance.class, LinkSourceId.class);
	}

}
