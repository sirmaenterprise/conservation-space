package com.sirma.itt.seip.instance.properties;

import com.sirma.itt.seip.plugin.SupportablePlugin;

/**
 * Entity type provider extension.
 *
 * @author BBonev
 */
public interface EntityTypeProviderExtension extends SupportablePlugin {

	/** The target name. */
	String TARGET_NAME = "EntityTypeProvider";

	/**
	 * Gets the entity type.
	 *
	 * @param object
	 *            the object
	 * @return the entity type
	 */
	EntityType getEntityType(Object object);

	/**
	 * Gets the entity type.
	 *
	 * @param object
	 *            the object
	 * @return the entity type
	 */
	EntityType getEntityType(Class<?> object);
}
