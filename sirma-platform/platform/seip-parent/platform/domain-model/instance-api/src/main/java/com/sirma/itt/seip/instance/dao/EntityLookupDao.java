package com.sirma.itt.seip.instance.dao;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * @author BBonev
 */
public class EntityLookupDao extends EntityLookupCallbackDAOAdaptor<Serializable, Entity<Serializable>, Serializable> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private InstancePersistCallback callback;

	/**
	 * Instantiates a new entity lookup dao.
	 *
	 * @param callback
	 *            the callback
	 */
	public EntityLookupDao(InstancePersistCallback callback) {
		this.callback = callback;
	}

	@Override
	public Serializable getValueKey(Entity<Serializable> value) {
		if (value == null) {
			return null;
		}
		return callback.getSecondaryIdLoadHandler().getId(value);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Pair<Serializable, Entity<Serializable>> findByValue(Entity<Serializable> value) {
		if (value == null) {
			return null;
		}
		Serializable key = getValueKey(value);
		if (key == null) {
			return null;
		}
		Entity<Serializable> entity = null;
		Object result = fetchEntityByValue(key);
		if (result instanceof List) {
			List<Entity<Serializable>> list = (List<Entity<Serializable>>) result;
			if (CollectionUtils.isEmpty(list)) {
				return null;
			}
			if (list.size() > 1) {
				LOGGER.warn("More then one {} found for secondary key: {}", callback.getSupportedEntityClass(), key);
			}
			entity = list.get(0);
		} else if (result instanceof Entity) {
			entity = (Entity<Serializable>) result;
		}
		if (entity == null) {
			return null;
		}
		return new Pair<>(entity.getId(), entity);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Pair<Serializable, Entity<Serializable>> findByKey(Serializable key) {
		try {
			Object entity = callback.getPrimaryIdLoadHandler().fetchByKey(key);
			if (entity instanceof Entity) {
				return new Pair<>(key, (Entity<Serializable>) entity);
			}
		} catch (DatabaseException e) {
			LOGGER.trace("Could not find the entity for type {} for key {}", callback.getSupportedEntityClass(), key,
					e);
			// not found in the DB
		}
		return null;
	}

	@Override
	public Pair<Serializable, Entity<Serializable>> createValue(Entity<Serializable> value) {
		return callback.persist(value);
	}

	/**
	 * Deletes a database entity identified by the given primary key and the current defined entity class.
	 *
	 * @param key
	 *            the key
	 * @return the int
	 */
	@Override
	public int deleteByKey(Serializable key) {
		return callback.delete(key);
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
	@Override
	public int updateValue(Serializable key, Entity<Serializable> value) {
		if (value == null) {
			return 0;
		}
		callback.persist(value);
		return 1;
	}

	/**
	 * Fetch entities by value key.
	 *
	 * @param key
	 *            the key
	 * @return the list of found entities or more than one or the found entity if only one.
	 */
	protected Object fetchEntityByValue(Serializable key) {
		return callback.getSecondaryIdLoadHandler().fetchByKey(key);
	}
}
