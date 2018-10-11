package com.sirma.itt.seip.instance.properties;

/**
 * Provider of entity types by class or object instance. The provider depends on {@link EntityTypeProviderExtension}
 * extension to do his work.
 *
 * @author BBonev
 */
public interface EntityTypeProvider {

	/**
	 * Gets the entity type.
	 *
	 * @param entityClass
	 *            the entity class
	 * @return the entity type
	 */
	EntityType getEntityType(Class<?> entityClass);

	/**
	 * Gets the entity type.
	 *
	 * @param entityInstance
	 *            the entity instance
	 * @return the entity type
	 */
	EntityType getEntityType(Object entityInstance);
}
