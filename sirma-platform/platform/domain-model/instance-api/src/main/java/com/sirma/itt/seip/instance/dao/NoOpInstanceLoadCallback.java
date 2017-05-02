package com.sirma.itt.seip.instance.dao;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;

/**
 * No operation callback implementation.
 *
 * @author BBonev
 */
public class NoOpInstanceLoadCallback implements InstanceLoadCallback {

	private static final InstanceLoadCallback INSTANCE = new NoOpInstanceLoadCallback();

	/**
	 * Provide singleton instance.
	 *
	 * @return the instance load callback
	 */
	public static InstanceLoadCallback instance() {
		return INSTANCE;
	}

	@Override
	public Serializable getId(Entity<? extends Serializable> entity) {
		return null;
	}

	@Override
	public Entity<? extends Serializable> getFromCacheById(Serializable id,
			EntityLookupCache<Serializable, Entity<? extends Serializable>, Serializable> cache) {
		return null;
	}

	@Override
	public Collection<Entity<? extends Serializable>> loadPersistedEntities(Collection<? extends Serializable> ids) {
		return Collections.emptyList();
	}

	@Override
	public void addEntityToCache(Entity<? extends Serializable> entity,
			EntityLookupCache<Serializable, Entity<? extends Serializable>, Serializable> cache) {
		// nothing to do
	}

	@Override
	public Object fetchByKey(Serializable key) {
		return null;
	}

	@Override
	public Entity<? extends Serializable> lookupById(Serializable id,
			EntityLookupCache<Serializable, Entity<? extends Serializable>, Serializable> cache) {
		return null;
	}

}
