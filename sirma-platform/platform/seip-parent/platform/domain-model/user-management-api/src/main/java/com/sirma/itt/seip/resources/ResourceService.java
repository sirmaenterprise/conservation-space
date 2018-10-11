package com.sirma.itt.seip.resources;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.model.vocabulary.Security;

/**
 * Resource management service. Provides common access for project resources. REVIEW check the documentation
 *
 * @author BBonev
 */
public interface ResourceService {

	/**
	 * Id of the system administration group.
	 */
	String SYSTEM_ADMIN_GROUP_ID = "SYSTEM_ADMINISTRATORS";

	/**
	 * Full uri of the administration group.
	 */
	String SYSTEM_ADMIN_GROUP_URI = EMF.PREFIX + ":" + ResourceProperties.GROUP_PREFIX + SYSTEM_ADMIN_GROUP_ID;

	/**
	 * Display name of the system administration group.
	 */
	String SYSTEM_ADMIN_GROUP_DISPLAY_NAME = "System Administrators";

	/**
	 * Id of the group that represents all users in the system.
	 */
	String EVERYONE_GROUP_ID = "SYSTEM_ALL_OTHER_USERS";

	/**
	 * Full uri of the all users group.
	 */
	String EVERYONE_GROUP_URI = Security.PREFIX + ":" + EVERYONE_GROUP_ID;

	/**
	 * Display name of the all users group.
	 */
	String EVERYONE_GROUP_DISPLAY_NAME = "Everyone";

	/**
	 * Validates the user name entered from the user/admin upon user registration. The method should check for at least
	 * <ul>
	 * <li>minimal and maximal length</li>
	 * <li>invalid or not allowed characters</li>
	 * <li>not allow tenant identifiers - tenant id that is not the current tenant</li>
	 * </ul>
	 *
	 * @param userId
	 *            the user id to validate
	 * @return true if the id is valid and false if not
	 */
	boolean validateUserName(String userId);

	/**
	 * Builds a user from an instance. Note that the build user instance is fictive and is not associated with any
	 * database if the user is not persisted.
	 *
	 * @param instance
	 *            from which to build an user
	 * @return user built from the given user instance
	 */
	User buildUser(Instance instance);

	/**
	 * Add new user to the local store created through the system
	 *
	 * @param instance
	 *            instance used for population of an user object
	 * @return the created and saved user instance created from the provided data
	 */
	User createUser(Instance instance);

	/**
	 * Builds a group from an instance. Note that the build group instance is fictive and is not associated with any
	 * database if the group is not persisted.
	 *
	 * @param instance
	 *            from which to build a group
	 * @return group built from the given group instance
	 */
	Group buildGroup(Instance instance);

	/**
	 * Add new group to the local store created through the system
	 *
	 * @param instance
	 *            instance used for population of a group object
	 * @return the created and saved group instance created from the provided data
	 */
	Group createGroup(Instance instance);

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
	Resource updateResource(Instance instance, Operation operation);

	/**
	 * Loads an instance by database ID.
	 *
	 * @param id
	 *            the DB id
	 * @return the loaded instance or <code>null</code> if not found.
	 */
	Instance loadByDbId(Serializable id);

	/**
	 * Loads a resource by user or group name. This method will ignore the service's cache and external systems.<br>
	 * Will not trigger synchronizations if the resource is not found.
	 *
	 * @param resourceName
	 *            the resource name to load or {@link com.sirma.itt.seip.Pair} that contains the resource name and the
	 *            {@link ResourceType}. If not specified the method will try all types to load the resource
	 * @return the loaded resource or <code>null</code> if not found.
	 */
	Instance load(Serializable resourceName);

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
	 * Deletes an instance. It may or may not be permanently removed from the database.
	 *
	 * @param instance
	 *            the instance to be deleted
	 * @param operation
	 *            the operation
	 * @param permanent
	 *            should instance be permanently and irreversible removed from the database (DMS).
	 */
	void delete(Instance instance, Operation operation, boolean permanent);

	/*
	 * Resource service methods
	 */

	/**
	 * Saves/updates resource in the internal storage.
	 *
	 * @param <R>
	 *            the generic type
	 * @param resource
	 *            the resource
	 * @return the updated resource
	 */
	<R extends Resource> R saveResource(R resource);

	/**
	 * Loads a resource by the given argument where the argument could be
	 * <ul>
	 * <li>db (semantic) id
	 * <li>resource identifier (user/group name)
	 * <li>JSON format of a resource reference
	 * <li>instance reference of a resource
	 * <li>resource object
	 * <li>object that could be converted to a {@link Resource} using {@link com.sirma.itt.seip.convert.TypeConverter}
	 * </ul>
	 * <p>
	 * NOTE: this method should be called only if the type of the resource identifier is not known.
	 *
	 * @param <R>
	 *            the resource type
	 * @param id
	 *            if the resource to load.
	 * @return the found resource or <code>null</code> if the id is not valid or there is not resource with the provided
	 *         id
	 * @see #getResource(Serializable)
	 * @see #getResource(String, ResourceType)
	 */
	<R extends Resource> R findResource(Serializable id);

	/**
	 * Gets resource by his primary ID.
	 *
	 * @param <R>
	 *            the generic type
	 * @param id
	 *            the id
	 * @return the resource or <code>null</code> if not found
	 */
	<R extends Resource> R getResource(Serializable id);

	/**
	 * Gets a resource by name. The names are considered unique through out of the application.
	 *
	 * @param <R>
	 *            the generic type
	 * @param name
	 *            the name of the resource to fetch
	 * @param type
	 *            is the resource type
	 * @return the resource or <code>null</code> if not found
	 */
	<R extends Resource> R getResource(String name, ResourceType type);

	/**
	 * Gets all resources of the given type.
	 *
	 * @param <R>
	 *            the generic type
	 * @param type
	 *            the resource type to load
	 * @param sortByProperty
	 *            if the parameter corresponds to a resource property then the output resources will be sorted by the
	 *            given property
	 * @return the all resources in unmodifiable list
	 */
	<R extends Resource> List<R> getAllResources(ResourceType type, String sortByProperty);

	/**
	 * Gets all registered users in the system (active and inactive)<br>
	 * This method is short for calling
	 * <pre>  <code>getAllResources(ResourceType.USER, null)</code></pre>
	 *
	 * @return list of all users in unmodifiable list
	 */
	default List<User> getAllUsers() {
		return getAllResources(ResourceType.USER, null);
	}

	/**
	 * gets all registered groups in the system. This method should return at least the system group that contains all
	 * registered users ( the same returned from {@link #getAllOtherUsers()})
	 * <br>
	 * This method is short for calling
	 * <pre>  <code>getAllResources(ResourceType.GROUP, null)</code></pre>
	 *
	 * @return list of all groups in unmodifiable list
	 */
	default List<Group> getAllGroups() {
		return getAllResources(ResourceType.GROUP, null);
	}

	/**
	 * Gets all active resources of given type. Uses {@link #getAllResources(ResourceType, String)} to get the resources
	 * and then filters them.
	 *
	 * @param <R>
	 *            the generic type
	 * @param type
	 *            the type of the resource (in most cases group or user)
	 * @param sortColumn
	 *            the sorting column
	 * @return filtered resources of given type or empty list if the type is missing or there are no resources of the
	 *         given type
	 */
	<R extends Resource> List<R> getAllActiveResources(ResourceType type, String sortColumn);

	/**
	 * Gets the resources that are contained in provided resource. Used for groups mostly
	 *
	 * @param resourceId
	 *            id of the resource to check what is contained in
	 * @return the contained resources list or throws exception if operation for resource type is not allowed
	 */
	List<Instance> getContainedResources(Serializable resourceId);

	/**
	 * Gets the resources that are contained in provided resource and filter the results by the given type. If a
	 * contained resource is a group it's contained resources will be returned and filtered also. Used for groups
	 * mostly.
	 *
	 * @param resource
	 *            the resource
	 * @param filterBy
	 *            the filter by. If no filtering is needed pass <code>null</code> or {@link ResourceType#ALL}
	 * @return the contained resource identifiers
	 */
	List<Instance> getContainedResources(Instance resource, ResourceType filterBy);

	/**
	 * Modify members of a resource
	 *
	 * @param resource
	 *            the resource members modify
	 * @param addMembers
	 *            the ids of the members to add
	 * @param removeMembers
	 *            the ids of the members to remove
	 */
	void modifyMembers(Resource resource, Collection<String> addMembers, Collection<String> removeMembers);

	/**
	 * Gets the resources that are contained in provided resource. Used for groups mostly
	 *
	 * @param resource
	 *            the resource to check what is contained in
	 * @return the contained resources list or throws exception if operation for resource type is not allowed
	 */
	List<String> getContainedResourceIdentifiers(Resource resource);

	/**
	 * Gets the resources that are contained in provided resource and filter the results by the given type. If a
	 * contained resource is a group it's contained resources will be returned and filtered also. Used for groups
	 * mostly.
	 *
	 * @param resource
	 *            the resource
	 * @param filterBy
	 *            the filter by. If no filtering is needed pass <code>null</code> or {@link ResourceType#ALL}
	 * @return the contained resource identifiers
	 */
	List<String> getContainedResourceIdentifiers(Resource resource, ResourceType filterBy);

	/**
	 * Gets the contained resources from every found resource by passed resource identifiers.
	 * <p>
	 * <b>This method returns as result all requested resources as groups are expanded and users are are added too.</b>
	 * </p>
	 *
	 * @param resourceIdentifiers
	 *            this can contain anything that the {@link ResourceService#findResource(Serializable)} can accept like:
	 *            resource id, resource,...
	 * @param filterBy
	 *            the filter by
	 * @return the containing resources
	 */
	List<Instance> getContainedResources(Collection<?> resourceIdentifiers, ResourceType filterBy);

	/**
	 * Gets the resources that are containing the provided resource. Used for user mostly
	 *
	 * @param resourceId
	 *            id of the resource
	 * @return the list of resources resource is part of as child or null, or throws exception if operation for resource
	 *         type is not allowed
	 */
	List<Instance> getContainingResources(Serializable resourceId);

	/**
	 * Gets the display name for the given resource identifier. The resource is searched first as user then as a group.
	 *
	 * @param resourceName
	 *            the resource name
	 * @return the display name
	 */
	String getDisplayName(Serializable resourceName);

	/**
	 * Checks if the given objects represent the same resource. The method arguments could be any identifier or object
	 * for a resource representation. The method works with user identifier, user DB id, user URI and resource instance.
	 * The arguments may not have the same format when comparing.<br>
	 * NOTE: passing two <code>null</code> arguments will result in <code>false</code>. The method does not consider 2
	 * non existent persons/groups as equal.
	 *
	 * @param resource1
	 *            the resource1
	 * @param resource2
	 *            the resource2
	 * @return true, if both arguments point to the same resource
	 */
	boolean areEqual(Object resource1, Object resource2);

	/**
	 * Checks if resource with the given identifier exists in the system. The method should not attempt any external
	 * synchronizations to check for the resource.
	 *
	 * @param id
	 *            the resource identifier or system id.
	 * @return true, if a resource with such id exists
	 */
	boolean resourceExists(Serializable id);

	/**
	 * Activate a resource (user). Active resources (users) could login into the system and could have task assigned to
	 * them for example. If the resource is already active this method should not do anything.
	 * <p>
	 * Calling this method on soft deleted resource will not have any effect
	 *
	 * @param <R>
	 *            the resource type
	 * @param resource
	 *            the resource to activate. Could be resource primary id, resource name or the resource itself.
	 * @param operation
	 *            the user operation that triggered the deactivation
	 * @return the updated resource
	 * @see #findResource(Serializable)
	 */
	<R extends Resource> R activate(Serializable resource, Operation operation);

	/**
	 * Deactivate a resource. Inactive resources (users) cannot login into the system and cannot be used for task
	 * assignment or to execute operations.
	 * <p>
	 * Calling deactivate on inactive or soft deleted resource will have no effect.
	 * <p>
	 * Note that system resources cannot be deactivated.
	 *
	 * @param <R>
	 *            the resource type
	 * @param resource
	 *            the resource to deactivate. Could be resource primary id, resource name or the resource itself.
	 * @param operation
	 *            the user operation that triggered the deactivation
	 * @return the updated resource
	 * @see #findResource(Serializable)
	 */
	<R extends Resource> R deactivate(Serializable resource, Operation operation);

	/**
	 * Returns an resource that represents all other users in the system. The resource will report group traits
	 *
	 * @return the all other users
	 */
	Resource getAllOtherUsers();

	/**
	 * Returns a group that represents all admin users.
	 *
	 * @return group that represents all admin users
	 */
	Resource getSystemAdminGroup();

	/**
	 * Checks if the given instance points to an user.
	 *
	 * @param instance
	 *            the instance
	 * @return true, if is user
	 */
	default boolean isUser(Instance instance) {
		return isType(instance, ResourceType.USER);
	}

	/**
	 * Checks if the given instance points to a group.
	 *
	 * @param instance
	 *            the instance
	 * @return true, if is group
	 */
	default boolean isGroup(Instance instance) {
		return isType(instance, ResourceType.GROUP);
	}

	/**
	 * Checks if the given instance points to the given type
	 *
	 * @param instance
	 *            the instance
	 * @param type
	 *            the type
	 * @return true, if is the same type
	 */
	default boolean isType(Instance instance, ResourceType type) {
		return instance instanceof Resource && ((Resource) instance).getType() == type;
	}
}
