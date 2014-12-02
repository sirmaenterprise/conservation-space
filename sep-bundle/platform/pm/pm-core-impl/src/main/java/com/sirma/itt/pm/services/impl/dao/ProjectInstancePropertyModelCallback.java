package com.sirma.itt.pm.services.impl.dao;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.properties.BasePropertyModelCallback;
import com.sirma.itt.emf.properties.dao.PropertyModelCallback;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.pm.domain.ObjectTypesPm;
import com.sirma.itt.pm.domain.entity.ProjectEntity;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * Adds support for project instances to to be able to save properties.
 * 
 * @author BBonev
 */
@ApplicationScoped
@InstanceType(type = ObjectTypesPm.PROJECT)
public class ProjectInstancePropertyModelCallback extends
		BasePropertyModelCallback<ProjectInstance> implements
		PropertyModelCallback<ProjectInstance> {

	/** The Constant SUPPORTED_OBJECTS. */
	private static final Set<Class<?>> SUPPORTED_OBJECTS;

	static {
		SUPPORTED_OBJECTS = CollectionUtils.createHashSet(5);
		SUPPORTED_OBJECTS.add(ProjectInstance.class);
		SUPPORTED_OBJECTS.add(ProjectEntity.class);
	}
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
		return model instanceof ProjectInstance;
	}

}
