package com.sirma.itt.pm.schedule.converter;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.entity.LinkSourceId;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.link.converters.AbstractInstanceToInstanceReferenceConverterProvider;
import com.sirma.itt.pm.schedule.model.ScheduleEntry;

/**
 * Converter provider that handles conversions from PMS module instances to {@link LinkSourceId}
 * objects
 * 
 * @author BBonev
 */
@ApplicationScoped
public class PmsInstanceToLinkSourceConverterProvider extends
		AbstractInstanceToInstanceReferenceConverterProvider {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(TypeConverter converter) {
		addEntityConverter(converter, ScheduleEntry.class, LinkSourceId.class);

		addEntityConverter(converter, ScheduleEntry.class, InstanceReference.class);
	}

}
