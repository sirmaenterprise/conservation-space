package com.sirma.itt.emf.instance.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.state.operation.Operation;

/**
 * Generic instance service interface. Defines base methods that are mostly always needed to be
 * implemented in a concrete instance service.
 * 
 * @param <I>
 *            the concrete instance type
 * @param <D>
 *            the concrete definition type that is represented by the given instance type
 * @author BBonev
 */
public interface InstanceService<I extends Instance, D extends DefinitionModel> {

	/**
	 * Gets the instance definition class.
	 * 
	 * @return the instance definition class
	 */
	Class<D> getInstanceDefinitionClass();

	/**
	 * Creates an instance by the given definition. The service should populated the default
	 * properties of the created instance and fire an event to notify for instance creation. The
	 * instance should not be persisted at this point.
	 * 
	 * @param definition
	 *            the definition
	 * @param parent
	 *            the parent instance
	 * @return the created instance
	 */
	I createInstance(D definition, Instance parent);

	/**
	 * Creates an instance by the given definition. The service should populated the default
	 * properties of the created instance and fire an event to notify for instance creation. The
	 * instance should not be persisted at this point.
	 * 
	 * @param definition
	 *            the definition
	 * @param parent
	 *            the parent instance
	 * @param operation
	 *            the operation used to/initiated by to create the instance
	 * @return the created instance
	 */
	I createInstance(D definition, Instance parent, Operation operation);

	/**
	 * Saves the given instance. The method may fire events for state change if needed or post and
	 * pre save operations. The method may call subsystems for actual object creation/update.
	 * 
	 * @param instance
	 *            the instance
	 * @param operation
	 *            the operation
	 * @return the updated instance
	 */
	I save(I instance, Operation operation);

	/**
	 * Cancel the given instance.
	 * 
	 * @param instance
	 *            the instance
	 * @return the updated instance
	 */
	I cancel(I instance);

	/**
	 * Refreshes the given instance content without changing the instance. The method should
	 * refreshes properties if modified from other users and/or other sub objects that are connected
	 * the given instance.
	 * 
	 * @param instance
	 *            the instance to refresh
	 */
	void refresh(I instance);

	/**
	 * Load instances that belong to the given owner. The method should not return <code>null</code>
	 * . The method should return instances that has the argument as a direct parent.
	 * 
	 * @param owner
	 *            the owner to check for children of the current type
	 * @return the list of found instances.
	 */
	List<I> loadInstances(Instance owner);

	/**
	 * Loads an instance by database ID.
	 * 
	 * @param id
	 *            the DB id
	 * @return the loaded instance or <code>null</code> if not found.
	 */
	I loadByDbId(Serializable id);

	/**
	 * Loads an instance by secondary key (mostly a DMS key).
	 * 
	 * @param instanceId
	 *            the instance id
	 * @return the loaded instance or <code>null</code> if not found.
	 */
	I load(Serializable instanceId);

	/**
	 * Batch load instances by secondary keys. More effective method for loading multiple instance
	 * at a single call. The method should honor the order of the given list if IDs.
	 * 
	 * @param <S>
	 *            the generic type
	 * @param ids
	 *            the ids
	 * @return the list of found instances
	 */
	<S extends Serializable> List<I> load(List<S> ids);

	/**
	 * Batch load instances by primary database IDs. More effective method for loading multiple
	 * instance at a single call. The method should honor the order of the given list if IDs.
	 * 
	 * @param <S>
	 *            the generic type
	 * @param ids
	 *            the ids
	 * @return the list of found instances
	 */
	<S extends Serializable> List<I> loadByDbId(List<S> ids);

	/**
	 * Batch load instances by secondary keys. More effective method for loading multiple instance
	 * at a single call. The method should honor the order of the given list if IDs.
	 * 
	 * @param <S>
	 *            the generic type
	 * @param ids
	 *            the ids
	 * @param allProperties
	 *            to load the full tree properties or only for the root level
	 * @return the list of found instances
	 */
	<S extends Serializable> List<I> load(List<S> ids, boolean allProperties);

	/**
	 * Batch load instances by primary database IDs. More effective method for loading multiple
	 * instance at a single call. The method should honor the order of the given list if IDs.
	 * 
	 * @param <S>
	 *            the generic type
	 * @param ids
	 *            the ids
	 * @param allProperties
	 *            to load the full tree properties or only for the root level
	 * @return the list of found instances
	 */
	<S extends Serializable> List<I> loadByDbId(List<S> ids, boolean allProperties);

	/**
	 * Method should return a mapping of the allowed child objects for the given object. The mapping
	 * should contains as a key the child type and as a value a list of definition objects for the
	 * given type. The child type should be resolvable and a valid parameter for any of the methods
	 * of {@link com.sirma.itt.emf.definition.dao.AllowedChildrenTypeProvider}.
	 * 
	 * @param owner
	 *            the owner instance
	 * @return the allowed instance definitions
	 */
	Map<String, List<DefinitionModel>> getAllowedChildren(I owner);

	/**
	 * Gets the allowed children for the given parent instance and the concrete child type. The
	 * child type should be resolvable and a valid parameter for any of the methods of
	 * 
	 * @param owner
	 *            the owner instance
	 * @param type
	 *            the type of the children to fetch if any
	 * @return the allowed children or empty list if non are allowed at the current moment.
	 *         {@link com.sirma.itt.emf.definition.dao.AllowedChildrenTypeProvider}. The method
	 *         should always return an empty list if the method
	 *         {@link #isChildAllowed(Instance, String)} returns <code>false</code>.
	 */
	List<DefinitionModel> getAllowedChildren(I owner, String type);

	/**
	 * Checks if child of the given type is allowed at the current moment. If this methods returns
	 * <code>true</code> then the method {@link #getAllowedChildren(Instance, String)} with the same
	 * arguments should return a non empty list. Also the call to the method
	 * 
	 * @param owner
	 *            the owner instance
	 * @param type
	 *            the type of the children to check
	 * @return <code>true</code>, if is child allowed and <code>false</code> if not
	 *         {@link #getAllowedChildren(Instance)} should result a map that contains a non
	 *         <code>null</code>, non empty list for a key the same as the argument if the method
	 *         returns <code>true</code>.
	 */
	boolean isChildAllowed(I owner, String type);

	/**
	 * Clones the given instance. The method may fire events for post and pre clone operations. The
	 * method may call subsystems for actual object creation.
	 * 
	 * @param instance
	 *            the instance to be cloned
	 * @param operation
	 *            the operation
	 * @return the clone
	 */
	I clone(I instance, Operation operation);

	/**
	 * Deletes an instance. It may or may not be permanently removed from the database.
	 * 
	 * @param instance
	 *            the instance to be deleted
	 * @param operation
	 *            the operation
	 * @param permanent
	 *            should instance be permanently and irreversible removed from the database (DMS).
	 */
	void delete(I instance, Operation operation, boolean permanent);

	/**
	 * Attach the given list of instance to the provided instance. The method should also fire the
	 * proper events for the attachment of each child.
	 * 
	 * @param targetInstance
	 *            the target instance
	 * @param operation
	 *            the executed operation if any
	 * @param children
	 *            the list of children to attach. No exception will be thrown if empty.
	 */
	void attach(I targetInstance, Operation operation, Instance... children);

	/**
	 * Detach the given list of children from the source instance. The method should also fire the
	 * proper events for the detachment of each child.
	 * 
	 * @param sourceInstance
	 *            the source instance
	 * @param operation
	 *            the executed operation if any
	 * @param instances
	 *            the list of instances to detach. No exception will be thrown if empty.
	 */
	void detach(I sourceInstance, Operation operation, Instance... instances);

}
