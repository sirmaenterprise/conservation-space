package com.sirma.itt.emf.instance.dao;

import java.io.Serializable;
import java.util.List;

import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinitionModel;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.model.PropertyModel;
import com.sirma.itt.emf.state.operation.Operation;

/**
 * DAO for performing low level instance operations and DB persisting.
 *
 * @param <E>
 *            the concrete implementation type
 * @author BBonev
 */
public interface InstanceDao<E extends Instance> {

	/**
	 * Creates an basic instance of the particular instance type supported by the implementation and
	 * optionally populates the default properties from the definition. If definition is not
	 * provided then the returned instance should only be an empty object with nothing set.
	 *
	 * @param definitionId
	 *            the definition id to use for populating properties from and initialization
	 * @param definitionModel
	 *            the definition model class
	 * @param populateProperties
	 *            if to populate default properties
	 * @return the created instance
	 */
	E createInstance(String definitionId, Class<?> definitionModel, boolean populateProperties);

	/**
	 * Creates an basic instance of the particular instance type supported by the implementation and
	 * optionally populates the default properties from the definition. If definition is not
	 * provided then the returned instance should only be an empty object with nothing set.
	 *
	 * @param definitionModel
	 *            the definition model instance
	 * @param populateProperties
	 *            if to populate default properties
	 * @return the created instance
	 */
	E createInstance(DefinitionModel definitionModel, boolean populateProperties);

	/**
	 * Saves the given instance in the DB and updates the proper cache.
	 * 
	 * @param instance
	 *            the instance to persist
	 * @return the instance old instance or <code>null</code> if saved for the first time
	 */
	E persistChanges(E instance);

	/**
	 * Notifies that the instance is modified and need to be updated and persisted in the DB if
	 * needed.
	 *
	 * @param instance
	 *            the instance to update and save
	 * @param autosave
	 *            to save the instance in the DB if needed
	 */
	void instanceUpdated(E instance, boolean autosave);

	/**
	 * Sets the current user to the {@link PropertyModel} as a value for the given key. If the
	 *
	 * @param model
	 *            the model
	 * @param key
	 *            the key from the model to update his value
	 *            {@link com.sirma.itt.emf.security.AuthenticationService} is not installed then the
	 *            model is not modified.
	 */
	void setCurrentUserTo(E model, String key);

	/**
	 * Updates revisions in the tree.
	 *
	 * @param instance
	 *            the instance to update
	 * @param revision
	 *            the revision to set
	 */
	void synchRevisions(E instance, Long revision);

	/**
	 * Convert the given entity to instance.
	 *
	 * @param source
	 *            the case entity
	 * @param toLoadProps
	 *            the to load properties of the converted instance
	 * @return the instance
	 */
	E convertEntity(Entity<?> source, boolean toLoadProps);

	/**
	 * Loads the cached case using the one of the two given keys. Only one of the two id/dmsId is
	 * used to retrieve entry. First is checked id.
	 *
	 * @param id
	 *            is the case entity/instance to load
	 * @param dmsId
	 *            the dms id
	 * @param loadProperties
	 *            to load instance properties or not
	 * @return loaded instance if found or <code>null</code> if not.
	 */
	E loadInstance(Serializable id, Serializable dmsId, boolean loadProperties);

	/**
	 * Loads all instances identified by their DMS IDs. The method first checks in the cache for
	 * each instance if not found fetch it from DB and update cache. This method is optimal for
	 * loading many instances that are not in the cache.
	 * 
	 * @param <S>
	 *            the secondary key type
	 * @param dmsIds
	 *            is the ID to look for
	 * @return the found and populated instance or empty list if nothing is found
	 */
	<S extends Serializable> List<E> loadInstances(List<S> dmsIds);

	/**
	 * Loads all instances identified by their DMS IDs. The method first checks in the cache for
	 * each instance if not found fetch it from DB and update cache. This method is optimal for
	 * loading many instances that are not in the cache.
	 * 
	 * @param <S>
	 *            the secondary key type
	 * @param dmsIds
	 *            is the ID to look for
	 * @param loadAllProperties
	 *            the load all properties, if <code>true</code> all properties for the returned
	 *            instances will be returned. otherwise only the first level of properties will be
	 *            loaded
	 * @return the found and populated instance or empty list if nothing is found
	 */
	<S extends Serializable> List<E> loadInstances(List<S> dmsIds, boolean loadAllProperties);

	/**
	 * Loads all instances identified by their DB IDs. The method first checks in the cache for each
	 * instance if not found fetch it from DB and update cache. This method is optimal for loading
	 * many instances that are not in the cache.
	 *
	 * @param <S>
	 *            the generic type
	 * @param ids
	 *            is the ID to look for
	 * @return the found and populated instance or empty list if nothing is found
	 */
	<S extends Serializable> List<E> loadInstancesByDbKey(List<S> ids);

	/**
	 * Loads all instances identified by their DB IDs. The method first checks in the cache for each
	 * instance if not found fetch it from DB and update cache. This method is optimal for loading
	 * many instances that are not in the cache.
	 *
	 * @param <S>
	 *            the generic type
	 * @param ids
	 *            is the ID to look for
	 * @param loadAllProperties
	 *            the load all properties, if <code>true</code> all properties for the returned
	 *            instances will be returned. otherwise only the first level of properties will be
	 *            loaded
	 * @return the found and populated instance or empty list if nothing is found
	 */
	<S extends Serializable> List<E> loadInstancesByDbKey(List<S> ids, boolean loadAllProperties);

	/**
	 * Load properties of the given instance.
	 *
	 * @param instance
	 *            the instance
	 */
	void loadProperties(E instance);

	/**
	 * Load instances that belong to the given owner. The method should not return <code>null</code>
	 * . The method should return instances that has the argument as a direct parent.
	 *
	 * @param owner
	 *            the owner to check for children of the current type
	 * @param loadProperties
	 *            if the method should pre load the instance properties or not
	 * @return the list of found instances.
	 */
	List<E> loadInstances(Instance owner, boolean loadProperties);

	/**
	 * Save properties of the given instance. If addOnly property is <code>true</code> then merge
	 * will be performed instead of replace.
	 *
	 * @param instance
	 *            the instance
	 * @param addOnly
	 *            the add only
	 */
	void saveProperties(E instance, boolean addOnly);

	/**
	 * Populate properties from the given definition to the given property model.
	 *
	 * @param <P>
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
	 * Load entity from the DB. The difference between this method and the direct DB load is that
	 * here we can check first into the cache for the entity.
	 *
	 * @param <S>
	 *            the generic type
	 * @param entityId
	 *            the entity id
	 * @return the entity
	 */
	<S extends Serializable> Entity<S> loadEntity(S entityId);

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
	 * Operation should be invoked just before invoking adapter to provide some needed updates on
	 * current model representing the up to date model that would be persisted.
	 *
	 * @param <P>
	 *            the generic type
	 * @param model
	 *            the model
	 * @param fields
	 *            the fields for the model
	 * @return the instance updated
	 */
	<P extends PropertyDefinition> Instance preSaveInstance(PropertyModel model, List<P> fields);

	/**
	 * Gets the primary id type.
	 *
	 * @param <P>
	 *            the primary ID type
	 * @return the id type
	 */
	<P extends Serializable> Class<P> getPrimaryIdType();

	/**
	 * Attaches the given list of children to the target instance and returns the these that where
	 * actually attached. The method will check if any of the provided instance if it's not already
	 * a child of the target instance.
	 * 
	 * @param targetInstance
	 *            the target instance
	 * @param operation
	 *            the operation that is executed and that lead to attaching the given future
	 *            children to the target instance
	 * @param children
	 *            the children to attach
	 * @return the list of actually attached instance.
	 */
	List<Instance> attach(E targetInstance, Operation operation, Instance... children);

	/**
	 * Detaches the given list of instance from the given source instance. If some of the given
	 * instance is not a child to the instance it will be ignored.
	 * 
	 * @param sourceInstance
	 *            the source instance
	 * @param operation
	 *            the operation that lead to detaching the given children from the target instance
	 * @param children
	 *            the list of children to detach
	 * @return the list of actually detached children.
	 */
	List<Instance> detach(E sourceInstance, Operation operation, Instance... children);

	/**
	 * Clears internal cache of the dao implementations if any.
	 */
	void clearInternalCache();

	/**
	 * Checks if the given instance has been modified by other user and if the given instance is up
	 * to date.
	 * 
	 * @param instance
	 *            the instance to check for modifications
	 * @return true, if is modified and the given is NOT the latest version.
	 */
	boolean isModified(E instance);
}
