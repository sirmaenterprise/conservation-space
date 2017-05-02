package com.sirma.itt.seip.instance.dao;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.db.DbDao;

/**
 * Callback implementation that handle search by primary db id.
 *
 * @author BBonev
 */
public abstract class BasePrimaryIdEntityLoadCallback implements InstanceLoadCallback {

	private final DbDao dbDao;

	/**
	 * Instantiates a new db id instance load callback.
	 *
	 * @param dbDao
	 *            the db dao
	 */
	public BasePrimaryIdEntityLoadCallback(DbDao dbDao) {
		this.dbDao = dbDao;
	}

	@Override
	public Serializable getId(Entity<? extends Serializable> entity) {
		return entity.getId();
	}

	@Override
	public Entity<? extends Serializable> getFromCacheById(Serializable id,
			EntityLookupCache<Serializable, Entity<? extends Serializable>, Serializable> cache) {
		if (cache == null) {
			return null;
		}
		return cache.getValue(id);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Entity<? extends Serializable> lookupById(Serializable id,
			EntityLookupCache<Serializable, Entity<? extends Serializable>, Serializable> cache) {
		if (cache == null) {
			return (Entity<? extends Serializable>) fetchByKey(id);
		}
		Pair<Serializable, Entity<? extends Serializable>> pair = cache.getByKey(id);
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

	@Override
	public Object fetchByKey(Serializable key) {
		Collection<Entity<? extends Serializable>> entities = loadPersistedEntities(Arrays.asList(key));
		if (entities.isEmpty()) {
			return null;
		}
		return entities.iterator().next();
	}

	/**
	 * Getter method for dbDao.
	 *
	 * @return the dbDao
	 */
	protected DbDao getDbDao() {
		return dbDao;
	}
}