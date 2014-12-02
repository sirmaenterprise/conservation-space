package com.sirma.itt.pm.services.impl.dao;

import java.io.Serializable;

import javax.inject.Inject;

import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.instance.dao.BaseInstanceDaoImpl;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.model.Instance;

/**
 * Base class for instance DAO implementations for PM module.
 * 
 * @param <T>
 *            the instance type
 * @param <C>
 *            the entity type
 * @param <P>
 *            the primary key type
 * @param <K>
 *            the secondary key type
 * @param <D>
 *            the definition type
 * @author BBonev
 */
public abstract class BasePmInstanceDaoImpl<T extends Instance, C extends Entity<P>, P extends Serializable, K extends Serializable, D extends DefinitionModel>
		extends BaseInstanceDaoImpl<T, C, P, K, D> implements InstanceDao<T> {
	/** The specific base instance dao for PM module. */
	@Inject
	private DbDao dbDao;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DbDao getDbDao() {
		return dbDao;
	}
}
