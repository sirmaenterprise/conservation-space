package com.sirma.itt.pm.util.datatype;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.entity.LinkSourceId;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.link.converters.AbstractInstanceToInstanceReferenceConverterProvider;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * Converter provider that handles conversions from PM module instances to {@link LinkSourceId}
 * objects
 * 
 * @author BBonev
 */
@ApplicationScoped
public class PmInstanceToLinkSourceConverterProvider extends
		AbstractInstanceToInstanceReferenceConverterProvider {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(TypeConverter converter) {
		addEntityConverter(converter, ProjectInstance.class, LinkSourceId.class);

		addEntityConverter(converter, ProjectInstance.class, InstanceReference.class);
	}
}
