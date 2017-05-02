package com.sirma.itt.seip.instance.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.search.NamedQueries;

/**
 * Primary id lookup from semantic data source
 *
 * @author BBonev
 */
public class SemanticDbIdLoadCallback implements InstanceLoadCallback {

	private final DbDao dao;
	private final Class<? extends Entity<? extends Serializable>> instanceClass;

	/**
	 * Instantiates a new semantic db id load callback.
	 *
	 * @param instanceClass
	 *            the instance class
	 * @param dao
	 *            the dao
	 */
	public SemanticDbIdLoadCallback(Class<? extends Entity<? extends Serializable>> instanceClass, DbDao dao) {
		this.instanceClass = instanceClass;
		this.dao = dao;
	}

	@Override
	public Serializable getId(Entity<? extends Serializable> entity) {
		return entity.getId();
	}

	@Override
	public Object fetchByKey(Serializable key) {
		return dao.find(instanceClass, key);
	}

	@Override
	public Entity<? extends Serializable> getFromCacheById(Serializable id,
			EntityLookupCache<Serializable, Entity<? extends Serializable>, Serializable> cache) {
		return cache.getValue(id);
	}

	@Override
	public Collection<Entity<? extends Serializable>> loadPersistedEntities(Collection<? extends Serializable> ids) {

		List<Pair<String, Object>> parameters = new ArrayList<>();
		parameters.add(new Pair<String, Object>(NamedQueries.Params.URIS, ids));
		return dao.fetchWithNamed(NamedQueries.SELECT_BY_IDS, parameters);
	}

	@Override
	public void addEntityToCache(Entity<? extends Serializable> entity,
			EntityLookupCache<Serializable, Entity<? extends Serializable>, Serializable> cache) {
		cache.setValue(entity.getId(), entity);
	}

	@Override
	public Entity<? extends Serializable> lookupById(Serializable id,
			EntityLookupCache<Serializable, Entity<? extends Serializable>, Serializable> cache) {
		Pair<Serializable, Entity<? extends Serializable>> pair = cache.getByKey(id);
		if (pair == null) {
			// deleted instances will not be fined in the cache
			// if needed they will be fetched from the database directly
			// if they are put in the cache the normal requests will get them as well so we bypass the cache
			if (Options.ALLOW_LOADING_OF_DELETED_INSTANCES.isEnabled()) {
				return instanceClass.cast(fetchByKey(id));
			}
			return null;
		}
		return pair.getSecond();
	}

}