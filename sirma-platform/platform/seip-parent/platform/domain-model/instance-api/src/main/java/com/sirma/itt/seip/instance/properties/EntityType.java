package com.sirma.itt.seip.instance.properties;

/**
 * Defines entity type identifier. The value returned by {@link #getTypeId()} will be used when saving properties to
 * distinguish the different beans. The value returned by {@link #getName()} is a short name of the entity.
 *
 * @author BBonev
 */
public interface EntityType {

	/**
	 * Gets the type id.
	 *
	 * @return the type id
	 */
	int getTypeId();

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	String getName();
}
