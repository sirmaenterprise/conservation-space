package com.sirma.itt.pm.schedule.instance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sirma.itt.emf.entity.EmfEntityIdType;
import com.sirma.itt.emf.instance.EntityType;
import com.sirma.itt.emf.instance.EntityTypeProviderExtension;
import com.sirma.itt.emf.instance.model.EntityTypeImpl;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.pm.schedule.model.ScheduleEntity;
import com.sirma.itt.pm.schedule.model.ScheduleEntry;
import com.sirma.itt.pm.schedule.model.ScheduleEntryEntity;
import com.sirma.itt.pm.schedule.model.ScheduleInstance;

/**
 * Default Pm schedule extension for entity type provider.
 * 
 * @author BBonev
 */
@Documentation("Extension that provides schedule/schedule entry {@link com.sirma.itt.emf.instance.EntityType} provider")
@Extension(target = EntityTypeProviderExtension.TARGET_NAME, order = 40)
public class PmScheduleEntityTypeProviderExtension implements EntityTypeProviderExtension {

	/** The Constant ALLOWED_CLASSES. */
	private static final List<Class<?>> ALLOWED_CLASSES = new ArrayList<Class<?>>(Arrays.asList(
			ScheduleInstance.class, ScheduleEntity.class, ScheduleEntry.class,
			ScheduleEntryEntity.class));

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Class<?>> getSupportedObjects() {
		return ALLOWED_CLASSES;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityType getEntityType(Object object) {
		return getEntityType(object.getClass());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityType getEntityType(Class<?> object) {
		if (object.equals(ScheduleInstance.class) || object.equals(ScheduleEntity.class)) {
			return new EntityTypeImpl(51, "schedule");
		} else if (object.equals(ScheduleEntry.class) || object.equals(ScheduleEntryEntity.class)) {
			return new EntityTypeImpl(52, "schedule_entry");
		}
		return EmfEntityIdType.UNKNOWN;
	}

}
