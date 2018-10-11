package com.sirma.itt.seip.permissions;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;
import static java.util.Collections.emptyMap;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.permissions.role.PermissionsChange;
import com.sirma.itt.seip.permissions.role.ResourceRole;

/**
 * The PermissionService is responsible to find/set/delete the roles for given instance and authorities.
 *
 * @author bbanchev
 * @author A. Kunchev
 */
public interface PermissionService {

	/**
	 * Gets all authorities with their role for the given instance mapped by authority db id.
	 *
	 * @deprecated replaced by {@link #getPermissionAssignments(Serializable)} where we use directly instance id. This
	 *             way you don't need to load references. Since version 2.22.0
	 * @param reference the instance reference which permissions to get
	 * @return the permission assignments
	 */
	@Deprecated
	default Map<String, ResourceRole> getPermissionAssignments(InstanceReference reference) {
		if (reference == null) {
			return emptyMap();
		}

		return getPermissionAssignments(reference.getId(), null, null);
	}

	/**
	 * Gets all authorities with their role for the given instance mapped by authority db id.
	 *
	 * @param id of the instance which permissions should be retrieved
	 * @return the permission assignments
	 */
	default Map<String, ResourceRole> getPermissionAssignments(Serializable id) {
		return getPermissionAssignments(id, null, null);
	}

	/**
	 * Gets all authorities with their role for the given instance mapped by authority db id providing filtering
	 * options.
	 *
	 * @deprecated replaced by {@link #getPermissionAssignments(Serializable, Boolean, Boolean)} where we use directly
	 *             instance id. This way you don't need to load references. Since version 2.22.0
	 * @param reference the instance reference which permissions to get
	 * @param includeParentPermissions when false, the parent permissions will be skipped. When null the filter won't be
	 *        taken into account and all parent permissions will be fetched (just like if it is true).
	 * @param includeLibraryPermissions when false, the library permissions will be skipped. When null the filter won't
	 *        be taken into account and all library permissions will be fetched (just like if it is true).
	 * @return the permission assignments
	 */
	@Deprecated
	default Map<String, ResourceRole> getPermissionAssignments(InstanceReference reference,
			Boolean includeParentPermissions, Boolean includeLibraryPermissions) {
		if (reference == null) {
			return emptyMap();
		}

		return getPermissionAssignments(reference.getId(), includeParentPermissions, includeLibraryPermissions);
	}

	/**
	 * Gets all authorities with their role for the given instance mapped by authority db id providing filtering
	 * options.
	 *
	 * @param id of the instance which permissions should be retrieved
	 * @param includeParentPermissions when false, the parent permissions will be skipped. When null the filter won't be
	 *        taken into account and all parent permissions will be fetched (just like if it is true).
	 * @param includeLibraryPermissions when false, the library permissions will be skipped. When null the filter won't
	 *        be taken into account and all library permissions will be fetched (just like if it is true).
	 * @return the permission assignments
	 */
	Map<String, ResourceRole> getPermissionAssignments(Serializable id, Boolean includeParentPermissions,
			Boolean includeLibraryPermissions);

	/**
	 * Gets all authorities with their role for the given instances mapped by authority db id providing filtering
	 * options.
	 *
	 * @deprecated replaced by {@link #getPermissionAssignments(Collection, Boolean, Boolean)} where we use directly
	 *             instance id. This way you don't need to load references. Since version 2.22.0
	 * @param references of the instances which permissions are searched
	 * @param includeParentPermissions when false, the parent permissions will be skipped. When null the filter won't be
	 *        taken into account and all parent permissions will be fetched (just like if it is true)
	 * @param includeLibraryPermissions when false, the library permissions will be skipped. When null the filter won't
	 *        be taken into account and all library permissions will be fetched (just like if it is true)
	 * @return {@link Map} containing as keys references of the instances which permissions are searched and as values
	 *         another {@link Map} representing the assignments for that instance
	 */
	@Deprecated
	default Map<InstanceReference, Map<String, ResourceRole>> getPermissionAssignmentsForInstances(
			Collection<InstanceReference> references, Boolean includeParentPermissions,
			Boolean includeLibraryPermissions) {
		if (isEmpty(references)) {
			return emptyMap();
		}

		Map<Serializable, InstanceReference> mappedIds = references
				.stream()
					.collect(Collectors.toMap(InstanceReference::getId, Function.identity()));

		return getPermissionAssignments(mappedIds.keySet(), includeParentPermissions, includeLibraryPermissions)
				.entrySet()
					.stream()
					.collect(Collectors.toMap(entry -> mappedIds.get(entry.getKey()), Entry::getValue));
	}

	/**
	 * Gets all authorities with their role for the given instances mapped by authority db id providing filtering
	 * options.
	 *
	 * @param ids of the instances which permissions are searched
	 * @param includeParentPermissions when false, the parent permissions will be skipped. When null the filter won't be
	 *        taken into account and all parent permissions will be fetched (just like if it is true)
	 * @param includeLibraryPermissions when false, the library permissions will be skipped. When null the filter won't
	 *        be taken into account and all library permissions will be fetched (just like if it is true)
	 * @return {@link Map} containing as keys ids of the instances which permissions are searched and as values another
	 *         {@link Map} representing the assignments for that instance
	 */
	Map<Serializable, Map<String, ResourceRole>> getPermissionAssignments(Collection<Serializable> ids,
			Boolean includeParentPermissions, Boolean includeLibraryPermissions);

	/**
	 * Gets the authorities role for a instance by given authority. All inherited permissions are searched. The most
	 * specific role is used searching starting from the instance and going to all inherited from instances.
	 *
	 * @deprecated replaced by {@link #getPermissionAssignment(Serializable, Serializable)} where we use directly
	 *             instance id. This way you don't need to load references. Since version 2.22.0
	 * @param reference the target instance reference to find role for
	 * @param authorityId id of the authority which permissions to provide
	 * @return the authority role or null if nothing could not be found
	 */
	@Deprecated
	default ResourceRole getPermissionAssignment(InstanceReference reference, Serializable authorityId) {
		if (reference == null) {
			return null;
		}

		return getPermissionAssignment(reference.getId(), authorityId);
	}

	/**
	 * Gets the authorities role for a instance by given authority. All inherited permissions are searched. The most
	 * specific role is used searching starting from the instance and going to all inherited from instances.
	 *
	 * @param id of the instance which permissions are searched
	 * @param authorityId id of the authority which permissions to provide
	 * @return the authority role or null if nothing could not be found
	 */
	ResourceRole getPermissionAssignment(Serializable id, Serializable authorityId);

	/**
	 * Gets the authorities role for instances by given authority. All inherited permissions are searched. The result
	 * map contains the reference and bound role to it.
	 *
	 * @deprecated replaced by {@link #getPermissionAssignmentForIds(Collection, Serializable)} where we use directly
	 *             instance id. This way you don't need to load references. Since version 2.22.0
	 * @param references of the instances which permission assignments are searched
	 * @param authorityId id of the authority which permissions to provide
	 * @return {@link Map} with references for the instances and their resource roles or empty map if noting is found
	 */
	@Deprecated
	default Map<InstanceReference, ResourceRole> getPermissionAssignment(Collection<InstanceReference> references,
			Serializable authorityId) {
		if (isEmpty(references)) {
			return emptyMap();
		}

		Map<Serializable, InstanceReference> mappedIds = references
				.stream()
					.collect(Collectors.toMap(InstanceReference::getId, Function.identity()));

		return getPermissionAssignmentForIds(mappedIds.keySet(), authorityId)
				.entrySet()
					.stream()
					.collect(Collectors.toMap(entry -> mappedIds.get(entry.getKey()), Entry::getValue));
	}

	/**
	 * Gets the authorities role for instances by given authority. All inherited permissions are searched. The result
	 * map contains the reference and bound role to it.
	 *
	 * @param ids of the instances which permission assignments are searched
	 * @param authorityId id of the authority which permissions to provide
	 * @return {@link Map} with ids for the instances and their resource roles or empty map if noting is found
	 */
	Map<Serializable, ResourceRole> getPermissionAssignmentForIds(Collection<Serializable> ids,
			Serializable authorityId);

	/**
	 * Retrieves entity permission information about a given instance.
	 *
	 * @deprecated replaced by {@link #getPermissionsInfo(Serializable)} where we use directly instance id. This way you
	 *             don't need to load references. Since version 2.22.0
	 * @param reference the instance reference to fetch the permissions for
	 * @return the found permissions or empty optional
	 */
	@Deprecated
	default Optional<EntityPermissions> getPermissionsInfo(InstanceReference reference) {
		if (reference == null) {
			return Optional.empty();
		}

		return getPermissionsInfo(reference.getId());
	}

	/**
	 * Retrieves entity permission information about a given instance.
	 *
	 * @param id of the instance which permissions should be retrieved
	 * @return the found permissions or empty optional
	 */
	Optional<EntityPermissions> getPermissionsInfo(Serializable id);

	/**
	 * Sets permissions for an instance based on set of changes on the permission model.
	 *
	 * @param instance instance for which to set the permissions
	 * @param changes set of changes to apply
	 */
	void setPermissions(InstanceReference instance, Collection<PermissionsChange> changes);

	/**
	 * Determines the permission model type based on current instance.
	 *
	 * @deprecated replaced by {@link #getPermissionModel(Serializable)} where we use directly instance id. This way you
	 *             don't need to load references. Since version 2.22.0
	 * @param reference the instance reference to check
	 * @return the permission model type
	 */
	@Deprecated
	default PermissionModelType getPermissionModel(InstanceReference reference) {
		if (reference == null) {
			return PermissionModelType.UNDEFINED;
		}

		return getPermissionModel(reference.getId());
	}

	/**
	 * Determines the permission model type based on current instance.
	 *
	 * @param id of the instance to check
	 * @return the permission model type
	 */
	PermissionModelType getPermissionModel(Serializable id);

	/**
	 * Remove the assignments of all descendants of the provided instance and set their inherit from parent flag
	 * according to the flag defined in the model.
	 *
	 * @param reference instance reference which children to restore.
	 */
	void restoreParentPermissions(InstanceReference reference);

	/**
	 * Check if this is root instance and manager permissions should be processed in diff manner
	 *
	 * @deprecated replaced by {@link #checkIsRoot(Serializable)} where we use directly instance id. This way you don't
	 *             need to load references. Since version 2.22.0
	 * @param refrence is the instance reference to check
	 * @return true if this is root (class, project, saved filter)
	 */
	@Deprecated
	default boolean checkIsRoot(InstanceReference refrence) {
		if (refrence == null) {
			return false;
		}

		return checkIsRoot(refrence.getId());
	}

	/**
	 * Check if this is root instance and manager permissions should be processed in diff manner
	 *
	 * @param id of the instance to check
	 * @return true if this is root (class, project, saved filter)
	 */
	boolean checkIsRoot(Serializable id);
}