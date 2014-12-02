package com.sirma.itt.pm.instance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sirma.itt.emf.entity.EmfEntityIdType;
import com.sirma.itt.emf.instance.EntityType;
import com.sirma.itt.emf.instance.EntityTypeProviderExtension;
import com.sirma.itt.emf.instance.model.EntityTypeImpl;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.pm.domain.entity.ProjectEntity;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * Default Pm extension for entity type provider.
 *
 * @author BBonev
 */
@Extension(target = EntityTypeProviderExtension.TARGET_NAME, order = 30)
public class PmEntityTypeProviderExtension implements EntityTypeProviderExtension {

	/** The Constant ALLOWED_CLASSES. */
	private static final List<Class<?>> ALLOWED_CLASSES = new ArrayList<Class<?>>(Arrays.asList(
			ProjectInstance.class, ProjectEntity.class));

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
		if (object.equals(ProjectInstance.class) || object.equals(ProjectEntity.class)) {
			return new EntityTypeImpl(50, "project");
		}
		return EmfEntityIdType.UNKNOWN;
	}

}
