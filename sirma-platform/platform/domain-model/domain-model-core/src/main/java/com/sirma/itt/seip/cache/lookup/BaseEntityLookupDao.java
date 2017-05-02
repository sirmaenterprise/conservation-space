package com.sirma.itt.seip.cache.lookup;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.cache.lookup.EntityLookupCallbackDAOAdaptor;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.db.exceptions.DatabaseException;

/**
 * Base class that realizes the common lookup adapter that works for basic entity cache implementation and provides a
 * way to implement easy secondary key lookups.
 *
 * @param <E>
 *            the cached entity element type
 * @param <S>
 *            the secondary key type
 * @param <P>
 *            the primary key type
 * @author BBonev
 */
public abstract class BaseEntityLookupDao<E extends Entity, S extends Serializable, P extends Serializable>
		extends EntityLookupCallbackDAOAdaptor<P, E, S> {
	/**
	 * {@inheritDoc}
	 */
	@Override
	public S getValueKey(E value) {
		if (value == null) {
			return null;
		}
		return getValueKeyInternal(value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Pair<P, E> findByValue(E value) {
		if (value == null) {
			return null;
		}
		S key = getValueKey(value);
		if (key == null) {
			return null;
		}
		E entity = null;
		Object result = fetchEntityByValue(key);
		if (result instanceof Collection) {
			List<E> list = (List<E>) result;
			if (CollectionUtils.isEmpty(list)) {
				return null;
			}
			if (list.size() > 1 && getLogger() != null) {
				getLogger().warn("More then one {} found for secondary key: {}", getEntityClass(), key);
			}
			entity = list.get(0);
		} else if (result instanceof Entity) {
			entity = (E) result;
		}
		if (entity == null) {
			return null;
		}
		return new Pair<>((P) entity.getId(), entity);
	}

	/**
	 * Gets the logger. Optional method if not implemented then nothing will be logged
	 *
	 * @return the logger
	 */
	protected Logger getLogger() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Pair<P, E> findByKey(P key) {
		try {
			E entity = (E) getDbDao().find(getEntityClass(), key);
			if (entity == null) {
				return null;
			}
			return new Pair<>(key, entity);
		} catch (DatabaseException e) {
			Logger logger = getLogger();
			if (logger != null) {
				logger.trace("Could not find the entity for type {} for key {}", getEntityClass(), key, e);
			}
			// not found in the DB
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Pair<P, E> createValue(E value) {
		E update = (E) getDbDao().saveOrUpdate(value);
		return new Pair<>((P) update.getId(), update);
	}

	/**
	 * Gets the entity class.
	 *
	 * @return the entity class
	 */
	protected abstract Class<E> getEntityClass();

	/**
	 * Deletes a database entity identified by the given primary key and the current defined entity class.
	 *
	 * @param key
	 *            the key
	 * @return the int
	 */
	@Override
	public int deleteByKey(P key) {
		return getDbDao().delete(getEntityClass(), key);
	}

	/**
	 * Calls the {@link DbDao#saveOrUpdate(Entity)} method with the provided entity.
	 * <p>
	 * <b>NOTE:</b> The method could NOT be used for saving entity for the first time. For that purpose use getOrCreate
	 * method of the cache.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return the int
	 */
	@SuppressWarnings("unchecked")
	@Override
	public int updateValue(P key, E value) {
		if (value == null) {
			return 0;
		}
		getDbDao().saveOrUpdate(value);
		return 1;
	}

	/**
	 * Gets the db dao.
	 *
	 * @return the db dao
	 */
	protected abstract DbDao getDbDao();

	/**
	 * Gets the value key for the given cache value.
	 *
	 * @param value
	 *            the value, will never be <code>null</code>
	 * @return the value key internal
	 */
	protected abstract S getValueKeyInternal(E value);

	/**
	 * Fetch entities by value key.
	 *
	 * @param key
	 *            the key
	 * @return the list of found entities or more than one or the found entity if only one.
	 */
	protected abstract Object fetchEntityByValue(S key);
}
