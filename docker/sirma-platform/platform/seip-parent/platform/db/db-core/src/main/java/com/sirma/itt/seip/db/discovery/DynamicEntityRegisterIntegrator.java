package com.sirma.itt.seip.db.discovery;

import static com.sirma.itt.seip.collections.CollectionUtils.isNotEmpty;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.function.Predicate;

import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dynamically register entity classes to hibernate. The entity classes are located via {@link EntityDiscovery} instance
 *
 * @since 2017-04-11
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 */
public class DynamicEntityRegisterIntegrator implements Integrator {

	static final String HIBERNATE_EJB_PERSISTENCE_UNIT_NAME = "hibernate.ejb.persistenceUnitName";
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final EntityDiscovery entityDiscovery;

	/**
	 * Instantiate integrator with default {@link FastClasspathScannerEntityDiscovery#INSTANCE}
	 */
	public DynamicEntityRegisterIntegrator() {
		this(FastClasspathScannerEntityDiscovery.INSTANCE);
	}

	/**
	 * Instantiate integrator with the provided {@link EntityDiscovery}
	 *
	 * @param discovery
	 *            the discovery to use
	 */
	public DynamicEntityRegisterIntegrator(EntityDiscovery discovery) {
		entityDiscovery = discovery;
	}

	@Override
	@SuppressWarnings("boxing")
	public void integrate(Configuration configuration, SessionFactoryImplementor sessionFactory,
			SessionFactoryServiceRegistry serviceRegistry) {
		String persistenceUnit = configuration.getProperties().getProperty(HIBERNATE_EJB_PERSISTENCE_UNIT_NAME);
		Collection<Class<?>> entities = entityDiscovery.getEntities(persistenceUnit);
		LOGGER.info("Registering to persistence unit {} entities: {}", persistenceUnit, entities.size());
		LOGGER.trace("Registering to persistence unit {} entities: {}", persistenceUnit, entities);
		// no need to scan if nothing is changed
		if (isNotEmpty(entities)) {
			// first we add the found classes
			// then we trigger scanning of these classes, otherwise the JPA cannot find entities and the named queries
			// but we skip already registered classes as they break the configurations if they have named queries, as
			// they are not allowed to be duplicated
			entities.stream().filter(notRegisteredEntity(configuration)).forEach(configuration::addAnnotatedClass);
			configuration.buildMappings();
		}
	}

	private static Predicate<Class<?>> notRegisteredEntity(Configuration configuration) {
		return entity -> configuration.getClassMapping(entity.getName()) == null;
	}

	@Override
	public void integrate(MetadataImplementor metadata, SessionFactoryImplementor sessionFactory,
			SessionFactoryServiceRegistry serviceRegistry) {
		// nothing to do here
	}

	@Override
	public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
		// nothing to do here
	}

}
