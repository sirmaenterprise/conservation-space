package com.sirma.itt.seip.instance.dao;

import java.io.Serializable;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.instance.dao.InstanceLoadCallback;

/**
 * Callback implementation that handles search by DMS id.
 *
 * @author BBonev
 */
public abstract class BaseSecondaryIdEntityLoadCallback implements InstanceLoadCallback {

	private final DbDao dbDao;

	/**
	 * Instantiates a new dms id instance load callback.
	 *
	 * @param dbDao
	 *            the db dao
	 */
	public BaseSecondaryIdEntityLoadCallback(DbDao dbDao) {
		this.dbDao = dbDao;
	}

	@Override
	public Entity<? extends Serializable> getFromCacheById(Serializable id,
			EntityLookupCache<Serializable, Entity<? extends Serializable>, Serializable> cache) {
		if (cache == null) {
			return null;
		}
		Serializable key = cache.getKey(id);
		if (key != null) {
			return cache.getValue(key);
		}
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Entity<? extends Serializable> lookupById(Serializable id,
			EntityLookupCache<Serializable, Entity<? extends Serializable>, Serializable> cache) {
		if (cache == null) {
			return (Entity<? extends Serializable>) fetchByKey(id);
		}
		Pair<Serializable, Entity<? extends Serializable>> pair = cache.getByValue(createEntityFromId(id));
		if (pair == null) {
			return null;
		}
		return pair.getSecond();
	}

	@Override
	public void addEntityToCache(Entity<? extends Serializable> entity,
			EntityLookupCache<Serializable, Entity<? extends Serializable>, Serializable> cache) {
		if (cache != null && entity != null) {
			cache.setValue(entity.getId(), entity);
		}
	}

	/**
	 * Getter method for dbDao.
	 *
	 * @return the dbDao
	 */
	protected DbDao getDbDao() {
		return dbDao;
	}

	/**
	 * Creates the entity and set the id. This could be used for search by value key if needed.
	 *
	 * @param id
	 *            the id
	 * @return the entity<? extends serializable>
	 */
	protected abstract Entity<? extends Serializable> createEntityFromId(Serializable id);

}