package com.sirma.itt.seip.db;

import static com.sirma.itt.seip.collections.CollectionUtils.addNonNullValue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.cache.CacheConfiguration;
import com.sirma.itt.seip.cache.Eviction;
import com.sirma.itt.seip.cache.Eviction.EvictionStrategy;
import com.sirma.itt.seip.cache.Expiration;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.seip.cache.lookup.NoEntityLookup;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;

/**
 * Implementation of {@link DbDao} that stores and fetches data from cache. The data could be saved using the any of the
 * save methods and could be retrieved via the {@link #find(Class, Object)} method. Batch fetch is also supported for
 * named query with name {@code SELECT_BY_IDS}.
 *
 * @author BBonev
 */
@VirtualDb
@ApplicationScoped
public class VirtualDbDao extends AbstractDbDao {

	private static final long serialVersionUID = -4270974964126511895L;

	@CacheConfiguration(eviction = @Eviction(strategy = EvictionStrategy.LRU) , expiration = @Expiration(maxIdle = 600000, interval = 60000) , doc = @Documentation("Cache that can be used for storing some temporary data by single identifier") )
	static final String VIRTUAL_STORE_CACHE = "VIRTUAL_STORE_CACHE";

	@Inject
	private EntityLookupCacheContext cacheContext;

	@Inject
	@ExtensionPoint(VirtualDbQueryParser.PLUGIN_NAME)
	private Plugins<VirtualDbQueryParser> parsers;

	/**
	 * Initialize cache store
	 */
	@PostConstruct
	protected void initialize() {
		cacheContext.createCacheIfAbsent(VIRTUAL_STORE_CACHE, true, new NoEntityLookup());
	}

	@Override
	public <E extends Entity<? extends Serializable>> E saveOrUpdate(E entity) {
		return saveOrUpdate(entity, null);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E extends Entity<? extends Serializable>> E saveOrUpdate(E entity, E oldEntity) {
		if (entity != null && entity.getId() != null) {
			getCache().setValue(entity.getId(), (Entity<Serializable>) entity);
		}
		return entity;
	}

	@Override
	public <E extends Entity<? extends Serializable>> E find(Class<E> clazz, Object id) {
		if (!(id instanceof Serializable)) {
			return null;
		}
		Pair<Serializable, Entity<Serializable>> pair = getCache().getByKey((Serializable) id);
		if (pair != null && clazz != null && clazz.isInstance(pair.getSecond())) {
			return clazz.cast(pair.getSecond());
		}
		return null;
	}

	@Override
	public <E extends Entity<? extends Serializable>> E refresh(E entity) {
		return entity;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R, E extends Pair<String, Object>> List<R> fetchWithNamed(String namedQuery, List<E> params) {
		return (List<R>) parse(p -> p.parseNamed(namedQuery, params));
	}

	@Override
	public <R, E extends Pair<String, Object>> List<R> fetchWithNamed(String query, List<E> params, int skip,
			int limit) {
		return fetchWithNamed(query, params);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R, E extends Pair<String, Object>> List<R> fetch(String query, List<E> params) {
		return (List<R>) parse(p -> p.parse(query, params));
	}

	@Override
	public <R, E extends Pair<String, Object>> List<R> fetch(String query, List<E> params, int skip, int limit) {
		return fetch(query, params);
	}

	private List<Object> parse(Function<VirtualDbQueryParser, Optional<Collection<Object>>> tryParsing) {
		Collection<Object> ids = parsers
				.stream()
					.map(tryParsing)
					.filter(Optional::isPresent)
					.findAny()
					.map(Optional::get)
					.orElse(Collections.emptyList());

		// for some reason this code written as stream does not compile via mvn build and only works build via the IDE
		List<Object> result = new ArrayList<>(ids.size());
		for (Object id : ids) {
			addNonNullValue(result, find(Entity.class, id));
		}
		return result;
	}


	@Override
	public <E extends Pair<String, Object>> int executeUpdate(String namedQuery, List<E> params) {
		return 0;
	}

	@Override
	protected <E extends Entity<? extends Serializable>> int deleteInternal(Class<E> clazz, Serializable entityId, boolean softDelete) {
		if (entityId != null) {
			getCache().removeByKey(entityId);
			return 1;
		}
		return 0;
	}

	/**
	 * Gets the cache that contains the stored data
	 *
	 * @return the cache
	 */
	protected EntityLookupCache<Serializable, Entity<Serializable>, Serializable> getCache() {
		return cacheContext.getCache(VIRTUAL_STORE_CACHE);
	}
}
