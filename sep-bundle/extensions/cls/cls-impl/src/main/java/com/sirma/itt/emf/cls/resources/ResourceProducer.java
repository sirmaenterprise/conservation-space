package com.sirma.itt.emf.cls.resources;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import com.sirma.itt.emf.db.DbDao;

/**
 * Class for declaring resources.
 * 
 * @author Mihail Radkov
 */
@ApplicationScoped
public class ResourceProducer {

	/**
	 * When injecting an entity manager this will be returned. The unit's name must match the one
	 * declared in persistence.xml
	 */
	@PersistenceContext(unitName = DbDao.PERSISTENCE_UNIT_NAME, type = PersistenceContextType.TRANSACTION)
	@Produces
	private EntityManager em;
}
