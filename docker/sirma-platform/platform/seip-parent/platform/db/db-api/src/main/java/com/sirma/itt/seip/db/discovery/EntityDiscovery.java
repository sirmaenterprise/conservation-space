package com.sirma.itt.seip.db.discovery;

import java.util.Collection;

/**
 * Provide list of entities from the class path that should be added to the persistence provider. To bind entity to
 * particular persistence unit the {@link PersistenceUnitBinding} could be used
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 2017-04-11
 */
public interface EntityDiscovery {

	/**
	 * Provides classes that are should be bound to the given persistence unit
	 *
	 * @param persistenceUnit to fetch the classes for
	 * @return a set of classes entities that belong to the given persistence unit
	 */
	Collection<Class<?>> getEntities(String persistenceUnit);
}
