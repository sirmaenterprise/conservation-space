package com.sirma.itt.seip.db;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import com.sirma.itt.seip.db.VirtualDb.DbType;

/**
 * Producer for {@link DbDao} combinations based on {@link VirtualDb}
 *
 * @author BBonev
 */
@ApplicationScoped
public class VirtualDbDaoProducer {

	@Inject
	@RelationalDb
	private Instance<DbDao> relationalDao;

	@Inject
	@SemanticDb
	private Instance<DbDao> semanticDao;

	@Inject
	@VirtualDb
	private Instance<ChainingDbDao> virtualDao;

	/**
	 * Produce semantic and virtual dao as secondary
	 *
	 * @return the db dao wrapper
	 */
	@Produces
	@VirtualDb(DbType.SEMANTIC)
	public DbDao produceSemanticAndVirtualDao() {
		return new DbDaoWrapper(semanticDao.get(), virtualDao.get());
	}

	/**
	 * Produce relational and virtual dao as secondary
	 *
	 * @return the db dao wrapper
	 */
	@Produces
	@VirtualDb(DbType.RELATIONAL)
	public DbDao produceRelationalAndVirtualDao() {
		return new DbDaoWrapper(relationalDao.get(), virtualDao.get());
	}
}
