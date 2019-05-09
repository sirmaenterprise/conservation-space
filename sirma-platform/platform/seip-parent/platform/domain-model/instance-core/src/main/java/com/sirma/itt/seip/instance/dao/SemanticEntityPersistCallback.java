package com.sirma.itt.seip.instance.dao;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.CachingSupplier;
import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.cache.CacheConfiguration;
import com.sirma.itt.seip.cache.CacheTransactionMode;
import com.sirma.itt.seip.cache.Eviction;
import com.sirma.itt.seip.cache.Expiration;
import com.sirma.itt.seip.cache.LockIsolation;
import com.sirma.itt.seip.cache.Locking;
import com.sirma.itt.seip.cache.Transaction;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.db.SemanticDb;
import com.sirma.itt.seip.db.VirtualDb;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.PropertyModel;
import com.sirma.itt.seip.instance.InstanceTypes;
import com.sirma.itt.seip.instance.ObjectInstance;

/**
 * Semantic instance persistent callback
 *
 * @author BBonev
 */
@Singleton
public class SemanticEntityPersistCallback implements InstancePersistCallback {

	// transaction mode is disabled, because it cause locking problems, when saving instances containing same
	// instance/object property as property. See CMF-20047, CMF-18158
	// enabled cache transactions
	@CacheConfiguration(transaction = @Transaction(mode = CacheTransactionMode.FULL_XA),
			locking = @Locking(isolation = LockIsolation.READ_COMMITTED),
			eviction = @Eviction(maxEntries = 8192, strategy = Eviction.EvictionStrategy.UNORDERED),
			expiration = @Expiration(maxIdle = 180_0000, interval = 60_000) , doc = @Documentation(""
			+ "Fully transactional cache used to contain the actively loaded instances from semantic database. "
			+ "<br>Minimal value expression: (averageActiveUsers * 10)") )
	static final String SEMANTIC_INSTANCE_ENTITY_CACHE = "SEMANTIC_INSTANCE_ENTITY_CACHE";

	private final DbDao dbDao;
	private final EntityLookupCacheContext cacheContext;
	private final Supplier<InstanceLoadCallback> primaryIdCallback = new CachingSupplier<>(
			() -> new SemanticDbIdLoadCallback(getSupportedEntityClass(), getDataSource()));
	private final CopyInstanceConverter instanceConverter;
	private final InstanceTypes instanceTypes;

	/**
	 * Instantiates a new semantic entity persist callback.
	 *
	 * @param dbDao
	 *            the db dao
	 * @param cacheContext
	 *            the cache context
	 * @param instanceConverter
	 *            the instance converter
	 * @param instanceTypes
	 *            the instance types
	 */
	@Inject
	public SemanticEntityPersistCallback(@VirtualDb(VirtualDb.DbType.SEMANTIC) DbDao dbDao,
			EntityLookupCacheContext cacheContext, CopyInstanceConverter instanceConverter,
			InstanceTypes instanceTypes) {
		this.dbDao = dbDao;
		this.cacheContext = cacheContext;
		this.instanceConverter = instanceConverter;
		this.instanceTypes = instanceTypes;
	}

	/**
	 * Initialize caches.
	 */
	@PostConstruct
	protected void initializeCache() {
		cacheContext.createCacheIfAbsent(getCacheName(), isCachingEnabled(), new EntityLookupDao(this).enableSecondaryKeyManagement());
	}

	/**
	 * Creates the loader.
	 *
	 * @return the instance loader
	 */
	@Produces
	@SemanticDb
	@Singleton
	public InstanceLoader getLoader() {
		return new DefaultInstanceLoader(this);
	}

	@Override
	public DbDao getDataSource() {
		return dbDao;
	}

	@Override
	public Pair<Serializable, Entity<Serializable>> persist(Entity<Serializable> entity) {
		Entity<Serializable> updated = dbDao.saveOrUpdate(entity);
		return new Pair<>(updated.getId(), updated);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Also updates temporary instance cache, which is populated only on instance save and the values are removed
	 * automatically, when specific time period expires. It is used for stale data check, when the instances are
	 * retrieved from the semantic DB, right after they are persisted. There are cases, where the data retrieved from
	 * the DB is stale due to not yet updated cluster. This cache will prevent further use of such data by providing the
	 * latest data for the requested instance that was just saved.
	 */
	@Override
	public <I extends Instance> I persistAndUpdateCache(Entity<Serializable> entity) {
		Serializable id = entity.getId();
		I old = getLoader().find(id);

		Entity<Serializable> saved = getDataSource().saveOrUpdate(entity, old);

		// updates the main instance cache
		getCache().setValue(id, saved);
		return old;
	}

	@Override
	public Class<? extends Entity<? extends Serializable>> getSupportedEntityClass() {
		return ObjectInstance.class;
	}

	@Override
	public String getCacheName() {
		return SEMANTIC_INSTANCE_ENTITY_CACHE;
	}

	@Override
	public EntityLookupCache<Serializable, Entity<? extends Serializable>, Serializable> getCache() {
		return cacheContext.getCache(getCacheName());
	}

	@Override
	public InstanceConverter getInstanceConverter() {
		return instanceConverter;
	}

	@Override
	public EntityConverter getEntityConverter() {
		return instanceConverter;
	}

	@Override
	public void onInstanceConverted(Entity<? extends Serializable> entity, Instance instance) {
		if (entity instanceof Instance && instance.type() == null) {
			// this is here because Kryo copy will clean the type field
			// copy the existing type if any
			instance.setType(((Instance) entity).type());
		}
		// refresh the type because it may be a stale type from the cache
		instanceTypes.from(instance);
	}

	@Override
	public int getMaxBatchSize() {
		return 512;
	}

	@Override
	public int delete(Serializable id) {
		return getDataSource().delete(null, id);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <I extends PropertyModel, E extends List<I>> void onBatchConvertedInstances(E collection) {
		instanceTypes.resolveTypes((Collection<? extends Instance>) collection);
	}

	@Override
	public InstanceLoadCallback getPrimaryIdLoadHandler() {
		return primaryIdCallback.get();
	}

	@Override
	public InstanceLoadCallback getSecondaryIdLoadHandler() {
		return NoOpInstanceLoadCallback.instance();
	}

	@Override
	public OnInstanceLoadObserver addOnInstanceConvertedObserver(Class<? extends Instance> typeFilter,
			OnInstanceLoadObserver callable) {
		// implement it if needed
		return null;
	}
}
