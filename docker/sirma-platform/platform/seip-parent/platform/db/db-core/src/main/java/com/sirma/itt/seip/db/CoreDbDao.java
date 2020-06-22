package com.sirma.itt.seip.db;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.sirma.itt.seip.db.CoreDb;
import com.sirma.itt.seip.db.DbDao;

/**
 * Implementation of the service {@link DbDao}. Providing operations for working with core relational database.
 *
 * @author BBonev
 */
@CoreDb
@ApplicationScoped
public class CoreDbDao extends BaseRelationalDbDao {

	private static final long serialVersionUID = 3988721858807948044L;

	/** The entity manager. */
	@PersistenceContext(unitName = PersistenceUnits.CORE)
	private transient EntityManager entityManager;

	/**
	 * Gets the entity manager.
	 *
	 * @return the entity manager
	 */
	@Override
	protected EntityManager getEntityManager() {
		return entityManager;
	}
}
