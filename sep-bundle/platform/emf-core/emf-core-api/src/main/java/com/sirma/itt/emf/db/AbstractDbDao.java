package com.sirma.itt.emf.db;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;

/**
 * Common base {@link DbDao} implementation.
 * 
 * @author BBonev
 */
public abstract class AbstractDbDao implements DbDao, Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -2773174438020653656L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public <S extends Serializable, E extends Entity<S>> E saveOrUpdateInNewTx(E entity) {
		return saveOrUpdate(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public <S extends Serializable, E extends Entity<S>> E saveOrUpdateInNewTx(E entity, E oldEntity) {
		return saveOrUpdate(entity, oldEntity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public <E extends Pair<String, Object>> int executeUpdateInNewTx(String namedQuery,
			List<E> params) {
		return executeUpdate(namedQuery, params);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public <S extends Serializable, E extends Entity<S>> void deleteInNewTx(Class<E> clazz,
			Serializable entityId) {
		delete(clazz, entityId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <E> E invokeInTx(Callable<E> callable) {
		try {
			return callable.call();
		} catch (Exception e) {
			throw new EmfRuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public <E> E invokeInNewTx(Callable<E> callable) {
		try {
			return callable.call();
		} catch (Exception e) {
			throw new EmfRuntimeException(e);
		}
	}

}