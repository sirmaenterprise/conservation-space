package com.sirma.itt.seip.instance.dao;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.search.NamedQueries;

/**
 * Primary id lookup from semantic data source.
 *
 * @author BBonev
 */
public class SemanticDbIdLoadCallback implements InstanceLoadCallback {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final DbDao dao;
	private final Class<? extends Entity<? extends Serializable>> instanceClass;

	/**
	 * Instantiates a new semantic db id load callback.
	 *
	 * @param instanceClass the instance class
	 * @param dao the dao
	 */
	SemanticDbIdLoadCallback(Class<? extends Entity<? extends Serializable>> instanceClass, DbDao dao) {
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
		Entity<? extends Serializable> value = cache.getValue(id);
		LOGGER.trace("Cache get for primary key {}={}", id, value);
		return value;
	}

	@Override
	public Collection<Entity<? extends Serializable>> loadPersistedEntities(Collection<? extends Serializable> ids) {
		List<Pair<String, Object>> parameters = Collections.singletonList(new Pair<>(NamedQueries.Params.URIS, ids));
		List<Entity<? extends Serializable>> entities = dao.fetchWithNamed(NamedQueries.SELECT_BY_IDS, parameters);
		traceLog(entities);
		return entities;
	}

	private static void traceLog(List<Entity<? extends Serializable>> loaded) {
		if (LOGGER.isTraceEnabled()) {
			String formatted = loaded.stream()
						.map(item -> item.getId() + "=" + item)
						.collect(Collectors.joining(",\n", "[\n", "]"));
			LOGGER.trace("Multiple cache miss for primary keys {}", formatted);
		}
	}

	@Override
	public void addEntityToCache(Entity<? extends Serializable> entity,
			EntityLookupCache<Serializable, Entity<? extends Serializable>, Serializable> cache) {
		cache.setValue(getId(entity), entity);
		LOGGER.trace("Update cache for key {}={}", getId(entity), entity);
	}

	@Override
	public Entity<? extends Serializable> lookupById(Serializable id,
			EntityLookupCache<Serializable, Entity<? extends Serializable>, Serializable> cache) {
		Pair<Serializable, Entity<? extends Serializable>> pair = cache.getByKey(id);
		if (pair == null) {
			LOGGER.trace("Cache miss for primary key {}", id);
			// deleted instances will not be fined in the cache
			// if needed they will be fetched from the database directly
			// if they are put in the cache the normal requests will get them as well so we bypass the cache
			if (Options.ALLOW_LOADING_OF_DELETED_INSTANCES.isEnabled()) {
				LOGGER.trace("Trying to load deleted instance with id={}", id);
				return instanceClass.cast(fetchByKey(id));
			}
			return null;
		}

		LOGGER.trace("Cache hit for primary key {}={}", id, pair.getSecond());
		return pair.getSecond();
	}
}
