package com.sirma.itt.seip.instance.dao;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.definition.RegionDefinitionModel;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.PropertyModel;

/**
 * DAO for performing low level instance operations and DB persisting.
 *
 * @author BBonev
 */
public interface InstanceDao {

	/**
	 * Creates an basic instance of the particular instance type supported by the implementation and optionally
	 * populates the default properties from the definition. If definition is not provided then the returned instance
	 * should only be an empty object with nothing set.
	 *
	 * @param <E>
	 *            the instance type
	 * @param definitionId
	 *            the definition id to use for populating properties from and initialization
	 * @param populateProperties
	 *            if to populate default properties
	 * @return the created instance
	 */
	<E extends Instance> E createInstance(String definitionId, boolean populateProperties);

	/**
	 * Creates an basic instance of the particular instance type supported by the implementation and optionally
	 * populates the default properties from the definition. If definition is not provided then the returned instance
	 * should only be an empty object with nothing set.
	 *
	 * @param <E>
	 *            the instance type
	 * @param definitionModel
	 *            the definition model instance
	 * @param populateProperties
	 *            if to populate default properties
	 * @return the created instance
	 */
	<E extends Instance> E createInstance(DefinitionModel definitionModel, boolean populateProperties);

	/**
	 * Saves the given instance in the DB and updates the proper cache.
	 *
	 * @param <E>
	 *            the instance type
	 * @param instance
	 *            the instance to persist
	 * @return the old instance or <code>null</code> if saved for the first time
	 */
	<E extends Instance> E persistChanges(E instance);

	/**
	 * Notifies that the instance is modified and need to be updated and persisted in the DB if needed.
	 *
	 * @param instance
	 *            the instance to update and save
	 * @param autosave
	 *            to save the instance in the DB if needed
	 */
	void instanceUpdated(Instance instance, boolean autosave);

	/**
	 * Sets the current user to the {@link PropertyModel} as a value for the given key.
	 *
	 * @param model
	 *            the model
	 * @param key
	 *            the key from the model to update his value
	 */
	void setCurrentUserTo(Instance model, String key);

	/**
	 * Updates revisions in the tree.
	 *
	 * @param instance
	 *            the instance to update
	 * @param revision
	 *            the revision to set
	 */
	void synchRevisions(Instance instance, Long revision);

	/**
	 * Loads the cached case using the one of the two given keys. Only one of the two id/dmsId is used to retrieve
	 * entry. First is checked id.
	 *
	 * @param <E>
	 *            the instance type
	 * @param id
	 *            is the case entity/instance to load
	 * @param dmsId
	 *            the dms id
	 * @param loadProperties
	 *            to load instance properties or not
	 * @return loaded instance if found or <code>null</code> if not.
	 */
	<E extends Instance> E loadInstance(Serializable id, Serializable dmsId, boolean loadProperties);

	/**
	 * Loads all instances identified by their DMS IDs. The method first checks in the cache for each instance if not
	 * found fetch it from DB and update cache. This method is optimal for loading many instances that are not in the
	 * cache.
	 *
	 * @param <E>
	 *            the instance type
	 * @param <S>
	 *            the secondary key type
	 * @param dmsIds
	 *            is the ID to look for
	 * @return the found and populated instance or empty list if nothing is found
	 */
	<S extends Serializable, E extends Instance> List<E> loadInstances(List<S> dmsIds);

	/**
	 * Loads all instances identified by their DMS IDs. The method first checks in the cache for each instance if not
	 * found fetch it from DB and update cache. This method is optimal for loading many instances that are not in the
	 * cache.
	 *
	 * @param <S>
	 *            the secondary key type
	 * @param <E>
	 *            the instance type
	 * @param dmsIds
	 *            is the ID to look for
	 * @param loadAllProperties
	 *            the load all properties, if <code>true</code> all properties for the returned instances will be
	 *            returned. otherwise only the first level of properties will be loaded
	 * @return the found and populated instance or empty list if nothing is found
	 */
	<S extends Serializable, E extends Instance> List<E> loadInstances(List<S> dmsIds, boolean loadAllProperties);

	/**
	 * Loads all instances identified by their DB IDs. The method first checks in the cache for each instance if not
	 * found fetch it from DB and update cache. This method is optimal for loading many instances that are not in the
	 * cache.
	 *
	 * @param <S>
	 *            the generic type
	 * @param <E>
	 *            the instance type
	 * @param ids
	 *            is the ID to look for
	 * @return the found and populated instance or empty list if nothing is found
	 */
	<S extends Serializable, E extends Instance> List<E> loadInstancesByDbKey(List<S> ids);

	/**
	 * Loads all instances identified by their DB IDs. The method first checks in the cache for each instance if not
	 * found fetch it from DB and update cache. This method is optimal for loading many instances that are not in the
	 * cache.
	 *
	 * @param <S>
	 *            the generic type
	 * @param <E>
	 *            the instance type
	 * @param ids
	 *            is the ID to look for
	 * @param loadAllProperties
	 *            the load all properties, if <code>true</code> all properties for the returned instances will be
	 *            returned. otherwise only the first level of properties will be loaded
	 * @return the found and populated instance or empty list if nothing is found
	 */
	<S extends Serializable, E extends Instance> List<E> loadInstancesByDbKey(List<S> ids, boolean loadAllProperties);

	/**
	 * Load properties of the given instance.
	 *
	 * @param instance
	 *            the instance
	 */
	void loadProperties(Instance instance);

	/**
	 * Save properties of the given instance. If addOnly property is <code>true</code> then merge will be performed
	 * instead of replace.
	 *
	 * @param instance
	 *            the instance
	 * @param addOnly
	 *            the add only
	 */
	void saveProperties(Instance instance, boolean addOnly);

	/**
	 * Populate properties from the given definition to the given property model.
	 *
	 * @param
	 * 			<P>
	 *            the element type
	 * @param model
	 *            the model
	 * @param fields
	 *            the fields
	 */
	<P extends PropertyDefinition> void populateProperties(PropertyModel model, List<P> fields);

	/**
	 * Populate properties using the given definition model.
	 *
	 * @param model
	 *            the model
	 * @param definitionModel
	 *            the definition model
	 */
	void populateProperties(PropertyModel model, DefinitionModel definitionModel);

	/**
	 * Populate properties using the given region definition model.
	 *
	 * @param model
	 *            the model
	 * @param regionDefinitionModel
	 *            the region definition model
	 */
	void populateProperties(PropertyModel model, RegionDefinitionModel regionDefinitionModel);

	/**
	 * Saves entity to DB and updates the underlying cache if any.
	 *
	 * @param <S>
	 *            the generic type
	 * @param entity
	 *            the entity to save
	 * @return the entity the updated instance of the entity
	 */
	<S extends Serializable> Entity<S> saveEntity(Entity<S> entity);

	/**
	 * Delete the given entity from the DB and cache.
	 * <p>
	 * TODO: add return boolean for success
	 *
	 * @param <S>
	 *            the generic type
	 * @param entity
	 *            the entity
	 */
	<S extends Serializable> void delete(Entity<S> entity);

	/**
	 * Gets the primary id type.
	 *
	 * @param
	 * 			<P>
	 *            the primary ID type
	 * @return the id type
	 */
	<P extends Serializable> Class<P> getPrimaryIdType();

	/**
	 * Clears internal cache of the dao implementations if any.
	 */
	void clearInternalCache();

	/**
	 * Checks if the given instance has been modified by other user and if the given instance is up to date.
	 *
	 * @param instance
	 *            the instance to check for modifications
	 * @return true, if is modified and the given is NOT the latest version.
	 */
	boolean isModified(Instance instance);

	/**
	 * Touch instance or instance reference that it's updated and need to be fetched from the database.
	 *
	 * @param object
	 *            should represent a {@link Instance}, {@link com.sirma.itt.seip.domain.instance.InstanceReference},
	 *            String id or collection of the described before.
	 */
	default void touchInstance(Object object) {
		// nothing to do
	}

	/**
	 * Checks the existence of specific instances in the system. Supports option for checking into deleted instances.
	 *
	 * @param <S> type of result map keys
	 * @param identifiers of the instances that should be checked
	 * @param includeDeleted shows if the method should check into deleted instances or not
	 * @return {@link Map} where as keys are the checked instances and values, whether that instance exists or not
	 */
	<S extends Serializable> Map<S, Boolean> exist(Collection<S> identifiers, boolean includeDeleted);
}
