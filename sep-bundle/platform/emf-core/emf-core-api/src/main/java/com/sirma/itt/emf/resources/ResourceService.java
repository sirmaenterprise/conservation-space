package com.sirma.itt.emf.resources;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.sirma.itt.emf.definition.model.GenericDefinition;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.resources.model.ResourceRole;
import com.sirma.itt.emf.security.model.RoleIdentifier;

/**
 * Resource management service. Provides common access for project resources. REVIEW check the
 * documentation
 *
 * @author BBonev
 */
public interface ResourceService extends InstanceService<Resource, GenericDefinition> {

	/**
	 * Assigns a resource a an instance with a given role.
	 *
	 * @param <R>
	 *            the resource type type
	 * @param resource
	 *            the resource to assign
	 * @param role
	 *            the role represented by the given resource into the given instance
	 * @param instance
	 *            the target instance that will be bound to the given resource
	 * @return the created {@link ResourceRole} object that represents the bond between the resource
	 *         and the given instance.
	 */
	<R extends Resource> ResourceRole assignResource(R resource, RoleIdentifier role,
			Instance instance);

	/**
	 * Assigns a resource to an instance with a given role.
	 *
	 * @param resourceId
	 *            the resource identifier
	 * @param type
	 *            the resource type
	 * @param role
	 *            the role represented by the given resource into the given instance
	 * @param instance
	 *            the target instance that will be bound to the given resource
	 * @return the created {@link ResourceRole} object that represents the bond between the resource
	 *         and the given instance.
	 */
	ResourceRole assignResource(String resourceId, ResourceType type, RoleIdentifier role,
			Instance instance);

	/**
	 * Assigns a resources to an instance with a given role. The old roles are kept and might me
	 * overridden
	 *
	 * @param <R>
	 *            the resource type type
	 * @param instance
	 *            the target instance that will be bound to the given resource
	 * @param authorityRoles
	 *            map holding the id of resource keyed to its role
	 * @return the resource roles assigned
	 */
	<R extends Resource> List<ResourceRole> assignResources(Instance instance,
			Map<Resource, RoleIdentifier> authorityRoles);

	/**
	 * Assigns a resources to an instance with a given role.If old saved roles are not part of the
	 *
	 * @param authorityRoles
	 *            map holding the id of resource keyed to its role
	 * @param instance
	 *            the target instance that will be bound to the given resource
	 */
	void setResources(Map<String, RoleIdentifier> authorityRoles, Instance instance);

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
	 * <li>object that could be converted to a {@link Resource} using
	 * {@link com.sirma.itt.emf.converter.TypeConverter}
	 * </ul>
	 * <p>
	 * NOTE: this method should be called only if the type of the resource identifier is not known.
	 *
	 * @param <R>
	 *            the resource type
	 * @param id
	 *            if the resource to load.
	 * @return the found resource or <code>null</code> if the id is not valid or there is not
	 *         resource with the provided id
	 * @see #getResource(Serializable)
	 * @see #getResource(String, ResourceType)
	 */
	<R extends Resource> R findResource(Serializable id);

	/**
	 * Gets the resource by his primary ID.
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
	 * Batch loads resources by their DB ids.
	 *
	 * @param <R>
	 *            the generic type
	 * @param ids
	 *            the ids
	 * @return the resources or empty list if nothing is found.
	 */
	<R extends Resource> List<R> getResources(List<Serializable> ids);

	/**
	 * Gets the resources of for the given {@link Instance}.
	 *
	 * @param <R>
	 *            the generic type
	 * @param instance
	 *            the instance
	 * @return the found resources for the given project instance if any.
	 */
	<R extends Resource> List<R> getResources(Instance instance);

	/**
	 * Gets the resources for the given instance by role.
	 *
	 * @param <R>
	 *            the resource type
	 * @param instance
	 *            the instance
	 * @param role
	 *            the role
	 * @return the resources by role
	 */
	<R extends Resource> List<R> getResourcesByRole(Instance instance, RoleIdentifier role);

	/**
	 * Gets all resources with their role for the given instance.
	 *
	 * @param instance
	 *            the instance
	 * @return the resource roles
	 */
	List<ResourceRole> getResourceRoles(Instance instance);

	/**
	 * Gets the resource role for a instance and resource name.
	 *
	 * @param instance
	 *            the target instance
	 * @param name
	 *            the name of the resource
	 * @param type
	 *            is the resource type
	 * @return the resource
	 */
	ResourceRole getResourceRole(Instance instance, String name, ResourceType type);

	/**
	 * Gets the resource role for a instance by given resource.
	 *
	 * @param instance
	 *            the target instance
	 * @param resource
	 *            the resource to evaluate
	 * @return the resource role
	 */
	ResourceRole getResourceRole(Instance instance, Resource resource);

	/**
	 * Gets the all resources of the given type.
	 *
	 * @param <R>
	 *            the generic type
	 * @param type
	 *            the type
	 * @param sortColumn
	 *            is the sorting
	 * @return the all resources
	 */
	<R extends Resource> List<R> getAllResources(ResourceType type, String sortColumn);

	/**
	 * Gets the or create resource using the given {@link Resource}. If no such resource exits then
	 * new will be created and added.<strong> Returns the same instance with updated db id.</strong>
	 *
	 * @param <R>
	 *            the generic type
	 * @param resource
	 *            the resource to create or find in db
	 * @return the resource created or retrieved
	 */
	<R extends Resource> R getOrCreateResource(R resource);

	/**
	 * Gets the resources that are contained in provided resource. Used for groups mostly
	 *
	 * @param <R>
	 *            the generic type of resource
	 * @param resource
	 *            the resource to check what is contained in
	 * @return the contained resources list or throws exception if operation for resource type is
	 *         not allowed
	 */
	<R extends Resource> List<R> getContainedResources(Resource resource);

	/**
	 * Gets the resources that are contained in provided resource. Used for groups mostly
	 *
	 * @param resource
	 *            the resource to check what is contained in
	 * @return the contained resources list or throws exception if operation for resource type is
	 *         not allowed
	 */
	List<String> getContainedResourceIdentifiers(Resource resource);

	/**
	 * Gets the resources that are containing the provided resource. Used for user mostly
	 *
	 * @param <R>
	 *            the generic type
	 * @param resource
	 *            the resource
	 * @return the list of resources resource is part of as child or null, or throws exception if
	 *         operation for resource type is not allowed
	 */
	<R extends Resource> List<R> getContainingResources(Resource resource);

	/**
	 * Gets the display name for the given resource identifier. The resource is searched first as
	 * user then as a group.
	 *
	 * @param resourceName
	 *            the resource name
	 * @return the display name
	 */
	String getDisplayName(Serializable resourceName);

	/**
	 * Checks if the given objects represent the same resource. The method arguments could be any
	 * identifier or object for a resource representation. The method works with user identifier,
	 * user DB id, user URI and resource instance. The arguments may not have the same format when
	 * comparing.<br>
	 * NOTE: passing two <code>null</code> arguments will result in <code>false</code>. The method
	 * does not consider 2 non existent persons/groups as equal.
	 *
	 * @param resource1
	 *            the resource1
	 * @param resource2
	 *            the resource2
	 * @return true, if both arguments point to the same resource
	 */
	boolean areEqual(Object resource1, Object resource2);

	/**
	 * Synch contained resources. The method should trigger synchronization of groups and their
	 * users.
	 */
	void synchContainedResources();
}
