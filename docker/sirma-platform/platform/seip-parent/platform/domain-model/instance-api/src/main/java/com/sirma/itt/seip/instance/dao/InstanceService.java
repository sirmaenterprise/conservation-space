package com.sirma.itt.seip.instance.dao;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.state.Operation;

/**
 * Generic instance service interface. Defines base methods that are mostly always needed to be implemented in a
 * concrete instance service.
 *
 * @author BBonev
 */
public interface InstanceService {

	/**
	 * Creates an instance by the given definition. The service should populated the default properties of the created
	 * instance and fire an event to notify for instance creation. The instance should not be persisted at this point.
	 *
	 * @param definition
	 *            the definition
	 * @param parent
	 *            the parent instance
	 * @return the created instance
	 */
	Instance createInstance(DefinitionModel definition, Instance parent);

	/**
	 * Creates an instance by the given definition. The service should populated the default properties of the created
	 * instance and fire an event to notify for instance creation. The instance should not be persisted at this point.
	 *
	 * @param definition
	 *            the definition
	 * @param parent
	 *            the parent instance
	 * @param operation
	 *            the operation used to/initiated by to create the instance
	 * @return the created instance
	 */
	Instance createInstance(DefinitionModel definition, Instance parent, Operation operation);

	/**
	 * Saves the given instance. The method may fire events for state change if needed or post and pre save operations.
	 * The method may call subsystems for actual object creation/update.
	 *
	 * @param instance
	 *            the instance
	 * @param operation
	 *            the operation
	 * @return the updated instance
	 */
	Instance save(Instance instance, Operation operation);

	/**
	 * Cancel the given instance.
	 *
	 * @param instance
	 *            the instance
	 * @return the updated instance
	 */
	Instance cancel(Instance instance);

	/**
	 * Refreshes the given instance content without changing the instance. The method should refreshes properties if
	 * modified from other users and/or other sub objects that are connected the given instance.
	 *
	 * @param instance
	 *            the instance to refresh
	 */
	void refresh(Instance instance);

	/**
	 * Load instances that belong to the given owner. The method should not return <code>null</code> . The method should
	 * return instances that has the argument as a direct parent.
	 *
	 * @param owner
	 *            the owner to check for children of the current type
	 * @return the list of found instances.
	 */
	List<Instance> loadInstances(Instance owner);

	/**
	 * Loads an instance by database ID.
	 *
	 * @param id
	 *            the DB id
	 * @return the loaded instance or <code>null</code> if not found.
	 */
	Instance loadByDbId(Serializable id);

	/**
	 * Loads an instance by secondary key (mostly a DMS key).
	 *
	 * @param instanceId
	 *            the instance id
	 * @return the loaded instance or <code>null</code> if not found.
	 */
	Instance load(Serializable instanceId);

	/**
	 * Batch load instances by secondary keys. More effective method for loading multiple instance at a single call. The
	 * method should honor the order of the given list if IDs.
	 *
	 * @param <S>
	 *            the generic type
	 * @param ids
	 *            the ids
	 * @return the list of found instances
	 */
	<S extends Serializable> List<Instance> load(List<S> ids);

	/**
	 * Batch load instances by primary database IDs. More effective method for loading multiple instance at a single
	 * call. The method should honor the order of the given list if IDs.
	 *
	 * @param <S>
	 *            the generic type
	 * @param ids
	 *            the ids
	 * @return the list of found instances
	 */
	<S extends Serializable> List<Instance> loadByDbId(List<S> ids);

	/**
	 * Batch load instances by secondary keys. More effective method for loading multiple instance at a single call. The
	 * method should honor the order of the given list if IDs.
	 *
	 * @param <S>
	 *            the generic type
	 * @param ids
	 *            the ids
	 * @param allProperties
	 *            to load the full tree properties or only for the root level
	 * @return the list of found instances
	 */
	<S extends Serializable> List<Instance> load(List<S> ids, boolean allProperties);

	/**
	 * Batch load instances by primary database IDs. More effective method for loading multiple instance at a single
	 * call. The method should honor the order of the given list if IDs.
	 *
	 * @param <S>
	 *            the generic type
	 * @param ids
	 *            the ids
	 * @param allProperties
	 *            to load the full tree properties or only for the root level
	 * @return the list of found instances
	 */
	<S extends Serializable> List<Instance> loadByDbId(List<S> ids, boolean allProperties);

	/**
	 * Checks if is child allowed.
	 *
	 * @param owner
	 *            the owner
	 * @param type
	 *            the type
	 * @param definitionId
	 *            the definition id
	 * @return true, if is child allowed
	 */
	boolean isChildAllowed(Instance owner, String type, String definitionId);

	/**
	 * Method should return a mapping of the allowed child objects for the given object. The mapping should contains as
	 * a key the child type and as a value a list of definition objects for the given type. The child type should be
	 * resolvable and a valid parameter for any of the methods of
	 * {@code com.sirma.itt.seip.definition.TypeMappingProvider}.
	 *
	 * @param owner
	 *            the owner instance
	 * @return the allowed instance definitions
	 */
	Map<String, List<DefinitionModel>> getAllowedChildren(Instance owner);

	/**
	 * Gets the allowed children for the given parent instance and the concrete child type. The child type should be
	 * resolvable and a valid parameter for any of the methods of
	 *
	 * @param owner
	 *            the owner instance
	 * @param type
	 *            the type of the children to fetch if any
	 * @return the allowed children or empty list if non are allowed at the current moment.
	 *         {@link com.sirma.itt.seip.definition.TypeMappingProvider}. The method should always return an empty list
	 *         if the method {@link #isChildAllowed(Instance, String)} returns <code>false</code>.
	 */
	List<DefinitionModel> getAllowedChildren(Instance owner, String type);

	/**
	 * Checks if child of the given type is allowed at the current moment. If this methods returns <code>true</code>
	 * then the method {@link #getAllowedChildren(Instance, String)} with the same arguments should return a non empty
	 * list. Also the call to the method
	 *
	 * @param owner
	 *            the owner instance
	 * @param type
	 *            the type of the children to check
	 * @return <code>true</code>, if is child allowed and <code>false</code> if not
	 *         {@link #getAllowedChildren(Instance)} should result a map that contains a non <code>null</code>, non
	 *         empty list for a key the same as the argument if the method returns <code>true</code>.
	 */
	boolean isChildAllowed(Instance owner, String type);

	/**
	 * Clones the given instance. The method may fire events for post and pre clone operations. The method may call
	 * subsystems for actual object creation. Removes the object properties from the result instance. Check
	 * {@link DefaultProperties#NOT_CLONABLE_PROPERTIES} for additional list of properties that will not be transfered
	 * to the result instance.
	 *
	 * @param instanceToClone
	 *            the instance that will be cloned
	 * @param operation
	 *            the operation with which will be created the result instance
	 * @return the clone
	 */
	Instance clone(Instance instanceToClone, Operation operation);

	/**
	 * Clones given instance. All of the properties that the given instance contains will be cloned to the result
	 * instance. The method will fire event for operation executed for the instance that is cloned(given instance).
	 *
	 * @param instanceToClone
	 *            instance that will be cloned
	 * @param operation
	 *            the operation with which will be created the result instance
	 * @return new instance, almost identical copy of the given with different id
	 */
	Instance deepClone(Instance instanceToClone, Operation operation);

	/**
	 * Deletes an instance. It may or may not be permanently removed from the database.
	 *
	 * @param instance
	 *            the instance to be deleted
	 * @param operation
	 *            the operation
	 * @param permanent
	 *            should instance be permanently and irreversible removed from the database (DMS).
	 * @return ids of the deleted instances, includes the children of the target instance
	 */
	Collection<String> delete(Instance instance, Operation operation, boolean permanent);

	/**
	 * Attach the given list of instance to the provided instance. The method should also fire the proper events for the
	 * attachment of each child.
	 *
	 * @param targetInstance
	 *            the target instance
	 * @param operation
	 *            the executed operation if any
	 * @param children
	 *            the list of children to attach. No exception will be thrown if empty.
	 */
	void attach(Instance targetInstance, Operation operation, Instance... children);

	/**
	 * Detach the given list of children from the source instance. The method should also fire the proper events for the
	 * detachment of each child.
	 *
	 * @param sourceInstance
	 *            the source instance
	 * @param operation
	 *            the executed operation if any
	 * @param instances
	 *            the list of instances to detach. No exception will be thrown if empty.
	 */
	void detach(Instance sourceInstance, Operation operation, Instance... instances);

	/**
	 * Publish the given instance into the given parent location using the given operation.
	 *
	 * @param instance
	 *            the instance to publish
	 * @param operation
	 *            the operation that triggered the method invocation
	 * @return the created new revision
	 */
	Instance publish(Instance instance, Operation operation);

	/**
	 * Create operation for the instance.
	 *
	 * @return The "create" operation.
	 */
	default Operation getCreateOperation() {
		return null;
	}

	/**
	 * Gets the primary parent for the instance represented by the given reference.
	 *
	 * @param reference
	 *            the reference
	 * @return the primary parent or <code>null</code> if no parent is defined or deleted
	 */
	default InstanceReference getPrimaryParent(InstanceReference reference) {
		return null;
	}

	/**
	 * Touch instance or instance reference that it's updated and need to be fetched from the database.
	 *
	 * @param object
	 *            should represent a {@link Instance}, {@link InstanceReference}, String id or collection of the
	 *            described before.
	 */
	default void touchInstance(Object object) {
		// nothing to do
	}

	/**
	 * Loads an instance that may also be soft deleted. The difference with the other load methods is that this will
	 * load even deleted instances. To check if the instance is deleted use {@link Instance#isDeleted()} method
	 *
	 * @param id
	 *            the instance id to load
	 * @return the found instance if any
	 */
	Optional<Instance> loadDeleted(Serializable id);

	/**
	 * Checks the existence of specified instances in the system.
	 *
	 * @param <S> type of the identifiers
	 * @param identifiers of the instances that should be checked
	 * @return {@link InstanceExistResult} containing the results of the check
	 * @see InstanceService#exist(Collection, boolean)
	 */
	default <S extends Serializable> InstanceExistResult<S> exist(Collection<S> identifiers) {
		return exist(identifiers, false);
	}

	/**
	 * Checks the existence of specified instances in the system. Supports option for checking into deleted instances.
	 *
	 * @param <S> type of the identifiers
	 * @param identifiers of the instances that should be checked
	 * @param includeDeleted shows if the method should check into deleted instances or not
	 * @return {@link InstanceExistResult} containing the results of the check
	 */
	<S extends Serializable> InstanceExistResult<S> exist(Collection<S> identifiers, boolean includeDeleted);
}
