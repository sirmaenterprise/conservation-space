package com.sirma.itt.emf.audit.db;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.sirma.itt.seip.annotation.NoOperation;
import com.sirma.itt.seip.db.BaseRelationalDbDao;
import com.sirma.itt.seip.db.DbDao;

/**
 * {@link DbDao} that provides access to the audit database.
 * 
 * @author BBonev
 */
@NoOperation
@ApplicationScoped
public class AuditDbDao extends BaseRelationalDbDao {

	private static final long serialVersionUID = -728046937519893979L;

	@PersistenceContext(unitName = "seip-auditlog")
	private EntityManager em;

	@Override
	protected EntityManager getEntityManager() {
		return em;
	}

	/**
	 * {@link DbDao} producer for {@link AuditDb} qualifier.
	 *
	 * @return the db dao
	 */
	@AuditDb
	@Produces
	@ApplicationScoped
	DbDao produceAnnotated() {
		return this;
	}
}
