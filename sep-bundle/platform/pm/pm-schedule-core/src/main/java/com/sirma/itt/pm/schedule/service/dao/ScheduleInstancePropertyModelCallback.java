package com.sirma.itt.pm.schedule.service.dao;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.properties.BasePropertyModelCallback;
import com.sirma.itt.emf.properties.dao.PropertyModelCallback;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.pm.schedule.domain.ObjectTypesPms;
import com.sirma.itt.pm.schedule.model.ScheduleEntity;
import com.sirma.itt.pm.schedule.model.ScheduleInstance;

/**
 * Adds support for schedule instances to to be able to save properties.
 * 
 * @author BBonev
 */
@ApplicationScoped
@InstanceType(type = ObjectTypesPms.SCHEDULE)
public class ScheduleInstancePropertyModelCallback extends
		BasePropertyModelCallback<ScheduleInstance> implements
		PropertyModelCallback<ScheduleInstance> {

	/** The Constant SUPPORTED_OBJECTS. */
	private static final Set<Class<?>> SUPPORTED_OBJECTS;

	static {
		SUPPORTED_OBJECTS = CollectionUtils.createHashSet(5);
		SUPPORTED_OBJECTS.add(ScheduleInstance.class);
		SUPPORTED_OBJECTS.add(ScheduleEntity.class);
	}

	@Override
	public Set<Class<?>> getSupportedObjects() {
		return SUPPORTED_OBJECTS;
	}

	@Override
	public boolean canHandle(Object model) {
		return (model instanceof ScheduleInstance) || (model instanceof ScheduleEntity);
	}

	@Override
	protected int getEntityTypeIdentifier(Entity<?> entity) {
		return super.getEntityTypeIdentifier(entity);
	}
}
