package com.sirma.itt.seip.db;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Implementation of the service {@link DbDao}. Providing base operations for working with relational database. If the
 * persistence context should be changed the class could be extended and the method {@link #getEntityManager()}
 * overridden to return the proper persistence context.
 *
 * @author BBonev
 */
@ApplicationScoped
public class DbDaoImpl extends BaseRelationalDbDao {

	private static final long serialVersionUID = 3988721858807948044L;

	/** The entity manager. */
	@PersistenceContext(unitName = PersistenceUnits.PRIMARY)
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

	/**
	 * {@link DbDao} producer for {@link RelationalDb} qualifier.
	 *
	 * @return the db dao
	 */
	@Produces
	@RelationalDb
	@ApplicationScoped
	DbDao produceAnnotated() {
		return this;
	}
}
