package com.sirma.itt.emf.resources.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sirma.itt.emf.entity.EmfEntityIdType;
import com.sirma.itt.emf.instance.EntityType;
import com.sirma.itt.emf.instance.EntityTypeProviderExtension;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.security.model.EmfGroup;
import com.sirma.itt.emf.security.model.EmfUser;

/**
 * Default Emf extension for entity type provider.
 * 
 * @author BBonev
 */
@Extension(target = EntityTypeProviderExtension.TARGET_NAME, order = 11)
public class ResourceTypeProviderExtension implements EntityTypeProviderExtension {

	/** The Constant ALLOWED_CLASSES. */
	private static final List<Class<?>> ALLOWED_CLASSES = new ArrayList<Class<?>>(Arrays.asList(
			EmfUser.class, EmfGroup.class));

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
		return EmfEntityIdType.getType(object);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityType getEntityType(Class<?> object) {
		if (object.equals(EmfUser.class)) {
			return EmfEntityIdType.USER;
		} else if (object.equals(EmfGroup.class)) {
			return EmfEntityIdType.GROUP;
		}
		return EmfEntityIdType.UNKNOWN;
	}

}
