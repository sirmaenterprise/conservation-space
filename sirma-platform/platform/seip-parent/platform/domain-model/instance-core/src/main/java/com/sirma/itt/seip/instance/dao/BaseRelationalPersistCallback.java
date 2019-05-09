package com.sirma.itt.seip.instance.dao;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.PropertyModel;
import com.sirma.itt.seip.instance.properties.PropertiesService;
import com.sirma.itt.seip.model.InstanceEntity;

/**
 * {@link InstanceEntity} persistent callback
 *
 * @author BBonev
 */
public abstract class BaseRelationalPersistCallback implements InstancePersistCallback {

	private final DbDao dbDao;
	protected final PropertiesService propertiesService;
	protected final EntityLookupCacheContext cacheContext;

	/**
	 * Instantiates a new base relational persist callback.
	 *
	 * @param dbDao
	 *            the db dao
	 * @param propertiesService
	 *            the properties service
	 * @param cacheContext
	 *            the cache context
	 */
	@Inject
	public BaseRelationalPersistCallback(DbDao dbDao, PropertiesService propertiesService,
			EntityLookupCacheContext cacheContext) {
		this.dbDao = dbDao;
		this.propertiesService = propertiesService;
		this.cacheContext = cacheContext;
	}

	/**
	 * Initialize cache.
	 */
	@PostConstruct
	protected void initializeCache() {
		cacheContext.createCacheIfAbsent(getCacheName(), isCachingEnabled(), new EntityLookupDao(this).enableSecondaryKeyManagement());
	}

	/**
	 * Gets the data source.
	 *
	 * @return the data source
	 */
	@Override
	public DbDao getDataSource() {
		return dbDao;
	}

	/**
	 * Persist.
	 *
	 * @param entity
	 *            the entity
	 * @return the pair
	 */
	@Override
	public Pair<Serializable, Entity<Serializable>> persist(Entity<Serializable> entity) {
		Entity<Serializable> updated = getDataSource().saveOrUpdate(entity);
		return new Pair<>(getPrimaryIdLoadHandler().getId(updated), updated);
	}

	@Override
	public void onInstanceConverted(Entity<? extends Serializable> entity, Instance instance) {
		propertiesService.loadProperties(instance);
	}

	@Override
	public <I extends PropertyModel, E extends List<I>> void onBatchConvertedInstances(E collection) {
		propertiesService.loadProperties(collection);
	}

	@Override
	public int getMaxBatchSize() {
		return 1024;
	}

	@Override
	public int delete(Serializable id) {
		return getDataSource().delete(getSupportedEntityClass(), id);
	}

	@Override
	public EntityLookupCache<Serializable, Entity<? extends Serializable>, Serializable> getCache() {
		return cacheContext.getCache(getCacheName());
	}

}
