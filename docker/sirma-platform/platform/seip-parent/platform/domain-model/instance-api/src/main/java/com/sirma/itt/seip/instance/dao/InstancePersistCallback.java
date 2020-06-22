package com.sirma.itt.seip.instance.dao;

import java.io.Serializable;
import java.util.List;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.PropertyModel;
import com.sirma.itt.seip.instance.properties.EntityType;

/**
 * Instance persist callback used to load/save single instance. The instance of this class should be annotated with
 * {@link EntityType} annotation with the concrete supported class.
 *
 * @author BBonev
 */
public interface InstancePersistCallback {

	/**
	 * Gets the data source used to access the instance data.
	 *
	 * @return the data source
	 */
	DbDao getDataSource();

	/**
	 * Optional cache name to use. If the method returns <code>null</code> default will be used.
	 *
	 * @return the cache name to lookup
	 */
	String getCacheName();

	/**
	 * Checks if is caching enabled. If not then no caching will be performed but the method get {@link #getCache()}
	 * will still return a valid object, just caching will not be done. By default caching is enabled.
	 *
	 * @return true, if is caching enabled
	 */
	default boolean isCachingEnabled() {
		return true;
	}

	/**
	 * Delete persisted entity.
	 *
	 * @param id
	 *            the id
	 * @return the int
	 */
	int delete(Serializable id);

	/**
	 * Persist the entity.
	 *
	 * @param entity
	 *            the entity
	 * @return the pair
	 */
	Pair<Serializable, Entity<Serializable>> persist(Entity<Serializable> entity);

	/**
	 * Persists the passed entity and updates the instance cache.
	 *
	 * @param entity that will be persisted
	 * @return the old reference before the persist execution or {@code null} if it is the first persist for that passed
	 *         entity
	 */
	default <I extends Instance> I persistAndUpdateCache(Entity<Serializable> entity) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Gets the supported entity class. (same as the one defined in the annotation {@link EntityType}).
	 *
	 * @return the supported entity class
	 */
	Class<? extends Entity<? extends Serializable>> getSupportedEntityClass();

	/**
	 * Gets a converter capable of converting instance to persistent entity.
	 *
	 * @return the entity converter
	 */
	EntityConverter getEntityConverter();

	/**
	 * Gets a converter capable of converting persisted entity to instance.
	 *
	 * @return the instance converter
	 */
	InstanceConverter getInstanceConverter();

	/**
	 * Method called by the loader after the instance has been converted from entity before returned. If external
	 * properties loading should be done it could go in this method.
	 *
	 * @param entity
	 *            the original source entity, before conversion
	 * @param instance
	 *            the converted instance
	 */
	void onInstanceConverted(Entity<? extends Serializable> entity, Instance instance);

	/**
	 * Adds the on instance converted observer.
	 *
	 * @param typeFilter
	 *            the type filter
	 * @param callable
	 *            the callable
	 * @return the callable object that was registered for the same key if any.
	 */
	OnInstanceLoadObserver addOnInstanceConvertedObserver(Class<? extends Instance> typeFilter,
			OnInstanceLoadObserver callable);

	/**
	 * Method called by the loader after multiple instances have been converted from entity before being returned. If
	 * external properties loading should be done it could go in this method.
	 *
	 * @param <I>
	 *            the instance type
	 * @param <E>
	 *            the collection type
	 * @param collection
	 *            the instance collection than has been converted
	 */
	<I extends PropertyModel, E extends List<I>> void onBatchConvertedInstances(E collection);

	/**
	 * Gets the maximum batch size that can be used for loading.
	 *
	 * @return the max batch size
	 */
	int getMaxBatchSize();

	/**
	 * Gets the primary id load handler.
	 *
	 * @return the primary id load handler
	 */
	InstanceLoadCallback getPrimaryIdLoadHandler();

	/**
	 * Gets the secondary id load handler.
	 *
	 * @return the secondary id load handler
	 */
	InstanceLoadCallback getSecondaryIdLoadHandler();

	/**
	 * Gets the cache callback that handles the entity caching.
	 *
	 * @return the cache
	 */
	EntityLookupCache<Serializable, Entity<? extends Serializable>, Serializable> getCache();

}
