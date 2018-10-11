package com.sirma.itt.seip.instance;

import java.util.Collection;
import java.util.List;

import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.state.Operation;

/**
 * Defines methods for basic operations on instances. It implementation should contain business logic for the
 * functionalities related to the instances. It should be used as level above {@link InstanceService}. Supports
 * processing of version instances.
 *
 * @author A. Kunchev
 */
public interface DomainInstanceService {

	/**
	 * Save an instance in the database and creates version for it. The version is exact copy of the currently saved
	 * instance with some additional properties in it.
	 *
	 * @param saveContext
	 *            object of type {@link InstanceSaveContext} which contains all of the needed information for completing
	 *            correct instance save
	 * @return the saved instance
	 */
	Instance save(InstanceSaveContext saveContext);

	/**
	 * Clones an {@link Instance} with given identifier. The cloned instance is an exact copy of the progenitor with the
	 * exception of few properties. Some properties are removed because they would be no longer correct. Such would be
	 * createdOn, version and etc. The full list can be seen in {@link DefaultProperties#NOT_CLONABLE_PROPERTIES}
	 * <p>
	 * Please note that this method <b>will NOT clone the instance content</b> and should not be used to clone instances
	 * with content, because it requires further processing.
	 *
	 * @param identifier
	 *            an identifier of an existing instance.
	 * @param operation
	 *            operation.
	 * @return a cloned instance
	 */
	Instance clone(String identifier, Operation operation);

	/**
	 * Load instance by it's identifier. The instance is checked for read permissions. Could load version.
	 *
	 * @param identifier
	 *            Instance identifier
	 * @return Instance loaded by the given id
	 */
	default Instance loadInstance(String identifier) {
		return loadInstance(identifier, false);
	}

	/**
	 * Load instance by it's identifier. The instance is checked for read permissions. Could load version. Could load
	 * soft deleted instance as well.
	 *
	 * @param identifier
	 *            Instance identifier
	 * @param allowDeleted
	 *            to allow loading of soft deleted instances
	 * @return Instance loaded by the given id
	 */
	Instance loadInstance(String identifier, boolean allowDeleted);

	/**
	 * Load a list of instances by their identifiers. This method also supports version instance loading and combination
	 * of version and original instance loading. The type of the instances is determined by the ids. If the second
	 * argument is true the method could also return soft deleted instance data.
	 *
	 * @param identifiers
	 *            collection of instance identifiers
	 * @param allowDeleted
	 *            if the method should return soft deleted instances
	 * @return collection of loaded instances or versions or both at the same time
	 */
	Collection<Instance> loadInstances(Collection<String> identifiers, boolean allowDeleted);

	/**
	 * Load a list of instances by their identifiers. This method also supports version instance loading and combination
	 * of version and original instance loading. The type of the instances is determined by the ids.
	 *
	 * @param identifiers
	 *            collection of instance identifiers
	 * @return collection of loaded instances or versions or both at the same time
	 */
	default Collection<Instance> loadInstances(Collection<String> identifiers) {
		return loadInstances(identifiers, false);
	}

	/**
	 * Deletes instance by id. The delete is *not* permanent.
	 *
	 * @param id
	 *            the id of instance to delete
	 * @return ids of the deleted instances
	 */
	Collection<String> delete(String id);

	/**
	 * Creates the instance.
	 *
	 * @param definitionId
	 *            the definition id
	 * @param parentId
	 *            the parent id
	 * @return the instance
	 */
	Instance createInstance(String definitionId, String parentId);

	/**
	 * Creates the instance from the given definition and parent
	 *
	 * @param definition
	 *            the definition
	 * @param parent
	 *            the parent
	 * @return the instance
	 */
	Instance createInstance(DefinitionModel definition, Instance parent);

	/**
	 * Get list of instance parents to witch the current user have access to read.<br>
	 * The first element of the list will be the top level parent which the current user can see and the last will be
	 * the current instance.
	 * <p>
	 * Possible outcomes
	 * <ul>
	 * <li>The user cannot read requested instance - NoPermissionsException will be thrown
	 * <li>The user can access the given instance but cannot access any of the parents or there is none - a list with
	 * one element will be returned
	 * <li>The user can access the given instance and there are some parents. The list will contains the current
	 * instance and all parents originating from the instance going to the root for which the user has access, stopping
	 * at the first non accessible parent.
	 * </ul>
	 *
	 * @param instanceId
	 *            the source instance id
	 * @return path to the given instance.
	 */
	List<Instance> getInstanceContext(String instanceId);

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
}
