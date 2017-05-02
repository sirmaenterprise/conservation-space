package com.sirma.itt.seip.instance.dao;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.CachingSupplier;
import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.cache.CacheConfiguration;
import com.sirma.itt.seip.cache.CacheTransactionMode;
import com.sirma.itt.seip.cache.Eviction;
import com.sirma.itt.seip.cache.Expiration;
import com.sirma.itt.seip.cache.Transaction;
import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.db.RelationalDb;
import com.sirma.itt.seip.db.VirtualDb;
import com.sirma.itt.seip.db.VirtualDb.DbType;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.PropertyModel;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.properties.PropertiesService;
import com.sirma.itt.seip.model.InstanceEntity;

/**
 * {@link InstanceEntity} persistent callback
 *
 * @author BBonev
 */
@Singleton
public class InstanceEntityPersistCallback extends BaseRelationalPersistCallback {

	@CacheConfiguration(transaction = @Transaction(mode = CacheTransactionMode.FULL_XA) , eviction = @Eviction(maxEntries = 5000) , expiration = @Expiration(maxIdle = 1800000, interval = 60000) , doc = @Documentation(""
			+ "Fully transactional cache used to contain the activly loaded instances. "
			+ "The cache will have at most 2 entries for every loaded instance"
			+ "<br>Minimal value expression: (evarageActiveUsers * 15)") )
	public static final String INSTANCE_ENTITY_CACHE = "INSTANCE_ENTITY_CACHE";

	private final InstanceConverter instanceConverter;
	private final EntityConverter entityConverter;
	private final Supplier<InstanceLoadCallback> primaryIdCallback = new CachingSupplier<>(
			() -> createPrimaryIdLoader());
	private final Supplier<InstanceLoadCallback> secondaryIdCallback = new CachingSupplier<>(
			() -> createSecondaryIdLoader());
	private final Map<Object, OnInstanceLoadObserver> observers = new LinkedHashMap<>();

	/**
	 * Instantiates a new instance entity persist callback.
	 *
	 * @param dbDao
	 *            the db dao
	 * @param propertiesService
	 *            the properties service
	 * @param cacheContext
	 *            the cache context
	 * @param instanceConverter
	 *            the instance converter
	 * @param entityConverter
	 *            the entity converter
	 */
	@Inject
	public InstanceEntityPersistCallback(@VirtualDb(DbType.RELATIONAL) DbDao dbDao, PropertiesService propertiesService,
			EntityLookupCacheContext cacheContext,
			@InstanceType(type = ObjectTypes.DEFAULT) InstanceConverter instanceConverter,
			@InstanceType(type = ObjectTypes.DEFAULT) EntityConverter entityConverter) {
		super(dbDao, propertiesService, cacheContext);
		this.instanceConverter = instanceConverter;
		this.entityConverter = entityConverter;
	}

	/**
	 * Creates the secondary id loader.
	 *
	 * @return the dms id instance entity load callback
	 */
	protected InstanceLoadCallback createSecondaryIdLoader() {
		return new DmsIdInstanceEntityLoadCallback(getDataSource());
	}

	/**
	 * Creates the primary id loader.
	 *
	 * @return the db id instance entity load callback
	 */
	protected InstanceLoadCallback createPrimaryIdLoader() {
		return new DbIdInstanceEntityLoadCallback(getDataSource());
	}

	/**
	 * Produce default relational.
	 *
	 * @return the instance loader
	 */
	@Produces
	@RelationalDb
	@Singleton
	public InstanceLoader produceDefaultRelational() {
		return createLoader();
	}

	/**
	 * Produce with entity.
	 *
	 * @return the instance loader
	 */
	@Produces
	@Singleton
	@RelationalDb
	@InstanceType(type = ObjectTypes.DEFAULT)
	public InstanceLoader produceWithEntity() {
		return createLoader();
	}

	/**
	 * Creates the loader.
	 *
	 * @return the instance loader
	 */
	@Produces
	@Singleton
	public InstanceLoader createLoader() {
		return new DefaultInstanceLoader(this);
	}

	/**
	 * Gets the supported entity class.
	 *
	 * @return the supported entity class
	 */
	@Override
	public Class<? extends Entity<? extends Serializable>> getSupportedEntityClass() {
		return InstanceEntity.class;
	}

	@Override
	public String getCacheName() {
		return INSTANCE_ENTITY_CACHE;
	}

	@Override
	public boolean isCachingEnabled() {
		// for now disable instance caching due to many cache locking problems
		return false;
	}

	@Override
	public InstanceConverter getInstanceConverter() {
		return instanceConverter;
	}

	@Override
	public InstanceLoadCallback getPrimaryIdLoadHandler() {
		return primaryIdCallback.get();
	}

	@Override
	public InstanceLoadCallback getSecondaryIdLoadHandler() {
		return secondaryIdCallback.get();
	}

	@Override
	public EntityConverter getEntityConverter() {
		return entityConverter;
	}

	@Override
	public void onInstanceConverted(Entity<? extends Serializable> entity, Instance instance) {
		OnInstanceLoadObserver callable = observers.get(instance.getClass());
		if (callable != null) {
			try {
				callable.call(instance, false);
			} catch (Exception e) {
				throw new EmfRuntimeException("Failed to execute callback on instance load", e);
			}
		}

		super.onInstanceConverted(entity, instance);
	}

	@Override
	public <I extends PropertyModel, E extends List<I>> void onBatchConvertedInstances(E collection) {
		for (I item : collection) {
			OnInstanceLoadObserver callable = observers.get(item.getClass());
			if (callable != null && item instanceof Instance) {
				try {
					callable.call((Instance) item, true);
				} catch (Exception e) {
					throw new EmfRuntimeException("Failed to execute callback on instance load", e);
				}
			}
		}
		super.onBatchConvertedInstances(collection);
	}

	@Override
	public OnInstanceLoadObserver addOnInstanceConvertedObserver(Class<? extends Instance> typeFilter,
			OnInstanceLoadObserver callable) {
		return observers.put(typeFilter, callable);
	}

}
