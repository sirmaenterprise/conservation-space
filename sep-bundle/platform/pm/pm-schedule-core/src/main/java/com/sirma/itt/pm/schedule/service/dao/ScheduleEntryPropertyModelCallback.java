package com.sirma.itt.pm.schedule.service.dao;

import java.io.Serializable;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.properties.BasePropertyModelCallback;
import com.sirma.itt.emf.properties.dao.PropertyModelCallback;
import com.sirma.itt.emf.properties.entity.EntityId;
import com.sirma.itt.emf.properties.model.PropertyModelKey;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.pm.schedule.domain.ObjectTypesPms;
import com.sirma.itt.pm.schedule.model.ScheduleEntry;
import com.sirma.itt.pm.schedule.model.ScheduleEntryEntity;

/**
 * Adds support for schedule entry to be able to save properties. <br>
 * REVIEW: in stead of property model key overriding of the {@link ScheduleEntry} we could also
 * generate 2 model keys for the entry and the actual instance
 * 
 * @author BBonev
 */
@ApplicationScoped
@InstanceType(type = ObjectTypesPms.SCHEDULE_ENTRY)
public class ScheduleEntryPropertyModelCallback extends BasePropertyModelCallback<ScheduleEntry>
		implements PropertyModelCallback<ScheduleEntry> {

	/** The Constant SUPPORTED_OBJECTS. */
	private static final Set<Class<?>> SUPPORTED_OBJECTS;

	static {
		SUPPORTED_OBJECTS = CollectionUtils.createHashSet(5);
		SUPPORTED_OBJECTS.add(ScheduleEntry.class);
		SUPPORTED_OBJECTS.add(ScheduleEntryEntity.class);
	}

	@Inject
	private TypeConverter converter;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Class<?>> getSupportedObjects() {
		return SUPPORTED_OBJECTS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canHandle(Object model) {
		return (model instanceof ScheduleEntry) || (model instanceof ScheduleEntryEntity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PropertyModelKey createModelKey(Entity<?> baseEntity, Long revision) {
		// refactored to support properties proxy
		PropertyModelKey modelKey = super.createModelKey(baseEntity, revision);
		Serializable actualId = null;
		InstanceReference reference = null;
		Class<?> actualClass = null;
		if (baseEntity instanceof ScheduleEntry) {
			reference = ((ScheduleEntry) baseEntity).getInstanceReference();
			actualClass = ((ScheduleEntry) baseEntity).getActualInstanceClass();
			actualId = ((ScheduleEntry) baseEntity).getActualInstanceId();
		} else if (baseEntity instanceof ScheduleEntryEntity) {
			reference = ((ScheduleEntryEntity) baseEntity).getActualInstance();
			actualId = ((ScheduleEntryEntity) baseEntity).getActualInstanceId();
		}
		Serializable id = null;
		Integer typeId = null;
		// if the actual instance is created then we save the properties as the actual instance only
		if ((actualId != null)
				|| ((reference != null) && StringUtils.isNotNullOrEmpty(reference.getIdentifier()))) {
			EntityId key = (EntityId) modelKey;
			id = actualId;
			if (id == null) {
				id = reference.getIdentifier();
			}
			if (actualClass == null) {
				actualClass = converter.convert(Class.class, reference);
			}
			typeId = typeProvider.getEntityType(actualClass).getTypeId();

			// if we have both properties we set them
			if ((id != null) && (typeId != null)) {
				key.setBeanId(id.toString());
				key.setBeanType(typeId);
			}
		}

		return modelKey;
	}

}
