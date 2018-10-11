package com.sirma.itt.seip.db;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.exceptions.DatabaseException;

/**
 * Common base {@link DbDao} implementation.
 *
 * @author BBonev
 */
public abstract class AbstractDbDao implements DbDao, Serializable {

	private static final long serialVersionUID = 1977474603203342081L;

	@Inject
	protected DatabaseIdManager idManager;

	@Override
	@Transactional(TxType.REQUIRES_NEW)
	public <E extends Entity<? extends Serializable>> E saveOrUpdateInNewTx(E entity) {
		return saveOrUpdate(entity);
	}

	@Override
	@Transactional(TxType.REQUIRES_NEW)
	public <E extends Pair<String, Object>> int executeUpdateInNewTx(String namedQuery, List<E> params) {
		return executeUpdate(namedQuery, params);
	}

	@Override
	public <E extends Entity<? extends Serializable>> int delete(Class<E> clazz, Serializable entityId,
			boolean softDelete) {
		return deleteInternal(clazz, entityId, softDelete);
	}

	/**
	 * Delete internal.
	 *
	 * @param <E>
	 *            the element type
	 * @param clazz
	 *            the clazz
	 * @param entityId
	 *            the entity id
	 * @param softDelete
	 *            if the operation should be soft delete
	 * @return the number of deleted items
	 */
	protected abstract <E extends Entity<? extends Serializable>> int deleteInternal(Class<E> clazz,
			Serializable entityId, boolean softDelete);

	@Override
	public <R, E extends Pair<String, Object>> List<R> fetch(String query, List<E> params, int skip, int limit) {
		throw new DatabaseException("Not implemented abstract method!");
	}

	@Override
	public <R, E extends Pair<String, Object>> List<R> fetchWithNamed(String query, List<E> params, int skip,
			int limit) {
		throw new DatabaseException("Not implemented abstract method!");
	}
}
