package com.sirma.itt.seip.instance;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.sirma.itt.seip.instance.properties.EntityType;
import com.sirma.itt.seip.instance.properties.EntityTypeProvider;
import com.sirma.itt.seip.instance.properties.EntityTypeProviderExtension;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.PluginUtil;

/**
 * Default implementation for the entity type provider
 *
 * @author BBonev
 */
public class EntityTypeProviderImpl implements EntityTypeProvider {

	/** The extensions. */
	@Inject
	@ExtensionPoint(EntityTypeProviderExtension.TARGET_NAME)
	private Iterable<EntityTypeProviderExtension> extensions;

	/** The mapping. */
	private Map<Class, EntityTypeProviderExtension> mapping;

	/**
	 * Initialize extensions.
	 */
	@PostConstruct
	public void initializeExtensions() {
		mapping = PluginUtil.parseSupportedObjects(extensions, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityType getEntityType(Class<?> entityClass) {
		EntityTypeProviderExtension extension = mapping.get(entityClass);
		if (extension != null) {
			return extension.getEntityType(entityClass);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityType getEntityType(Object entityInstance) {
		if (entityInstance == null) {
			return null;
		}
		EntityTypeProviderExtension extension = mapping.get(entityInstance.getClass());
		if (extension != null) {
			return extension.getEntityType(entityInstance);
		}
		return null;
	}

}
