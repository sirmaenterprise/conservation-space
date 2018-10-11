package com.sirma.itt.seip.permissions;

import static com.sirma.itt.seip.collections.CollectionUtils.emptyMap;
import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;
import static com.sirma.itt.seip.util.EqualsHelper.getOrDefault;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.exceptions.InstanceNotFoundException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.permissions.SecurityModel.BaseRoles;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.permissions.role.ResourceRole;
import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;
import com.sirma.itt.seip.permissions.role.RoleRegistry;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Default implementation of the {@link InstanceAccessEvaluator}.<br>
 * Note that most of the methods are modified to evaluate permissions for the version instances as well. The methods
 * that work with collection of instances and return mapped id with permissions actually return the ids of the original
 * instances, not version instances ids.
 *
 * @author BBonev
 */
@ApplicationScoped
public class InstanceAccessEvaluatorImpl implements InstanceAccessEvaluator {

	private static final Function<Serializable, InstanceAccessPermissions> FULL_ACCESS = x -> InstanceAccessPermissions.CAN_WRITE;

	@Inject
	private PermissionService permissionService;

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Inject
	private ResourceService resourceService;

	@Inject
	private SecurityContext securityContext;

	@Inject
	private AuthorityService authorityService;

	@Inject
	private RoleRegistry roleRegistry;

	@Inject
	private InstanceService instanceService;

	@Override
	public boolean isAtLeastRole(Serializable instanceId, Serializable resourceId, RoleIdentifier minimumRole) {
		if (instanceId == null) {
			return false;
		}

		return isAtLeastRole(resolveReference(instanceId), getResource(resourceId),
				getOrDefault(minimumRole, BaseRoles.VIEWER));
	}

	private boolean isAtLeastRole(Supplier<InstanceReference> referenceSupplier, Resource resource,
			RoleIdentifier minimumRole) {
		if (isAdmin(resource)) {
			return true;
		}
		// resolve the reference if non admin only
		InstanceReference reference = referenceSupplier.get();

		ResourceRole role = permissionService.getPermissionAssignment(reference, resource.getId());
		if (role != null) {
			return role.getRole().getGlobalPriority() >= minimumRole.getGlobalPriority();
		}
		// the user has no permissions if his role is null
		return false;
	}

	private Supplier<InstanceReference> resolveReference(Serializable instanceId) {
		return () -> {
			InstanceReference reference = null;
			if (instanceId instanceof InstanceReference) {
				reference = (InstanceReference) instanceId;
			} else if (instanceId instanceof Instance) {
				reference = ((Instance) instanceId).toReference();
			}

			reference = handleVersionReference(reference);
			if (reference != null) {
				return reference;
			}

			return handleVersion(instanceId);
		};
	}

	// gets original instance reference, if version instance is passed
	// TODO it will do for now, but this looks more like hack then anything else
	// Implement CMF-22836 !!!
	private InstanceReference handleVersionReference(InstanceReference reference) {
		if (reference == null || !InstanceVersionService.isVersion(reference.getId())) {
			return reference;
		}

		return handleVersion(reference.getId());
	}

	private InstanceReference handleVersion(Serializable instanceId) {
		Serializable id = InstanceVersionService.getIdFromVersionId(instanceId);
		Supplier<InstanceNotFoundException> exceptionSupplier = () -> new InstanceNotFoundException(id.toString());
		if (InstanceVersionService.isVersion(instanceId)) {
			// we need to look for deleted instances as well, because we want to show their version in widgets
			return instanceTypeResolver.resolveReference(id).orElseGet(
					() -> instanceService.loadDeleted(id).map(Instance::toReference).orElseThrow(exceptionSupplier));
		}

		return instanceTypeResolver.resolveReference(id).orElseThrow(exceptionSupplier);
	}

	private Resource getResource(Serializable resource) {
		if (resource == null) {
			return getCurrentUser();
		}
		Resource foundResource = resourceService.findResource(resource);
		if (foundResource == null) {
			return getCurrentUser();
		}
		return foundResource;
	}

	private Resource getCurrentUser() {
		User authentication = securityContext.getEffectiveAuthentication();
		if (authentication instanceof Resource) {
			return (Resource) authentication;
		}
		return resourceService.findResource(authentication);
	}

	private boolean isAdmin(Resource user) {
		return authorityService.isAdminOrSystemUser(user);
	}

	@Override
	public Map<Serializable, InstanceAccessPermissions> isAtLeastRole(Collection<? extends Serializable> identifiers,
			Serializable resourceId, RoleIdentifier minimumReadRole, RoleIdentifier minimumWriteRole) {
		verifyRoles(minimumReadRole, minimumWriteRole);
		if (isEmpty(identifiers)) {
			return emptyMap();
		}

		Resource resource = getResource(resourceId);
		if (isAdmin(resource)) {
			// administrators access everything without at the moment
			return buildFullAccessMap(identifiers);
		}

		// all of this logic will be removed, when CMF-22836 is done
		Collection<InstanceReference> references = getReferences(identifiers);
		if (references.isEmpty()) {
			return emptyMap();
		}

		Map<InstanceReference, ResourceRole> permissions = permissionService.getPermissionAssignment(references,
				resource.getId());
		if (permissions.isEmpty()) {
			return emptyMap();
		}

		return calculatePermissions(permissions, minimumReadRole, minimumWriteRole);
	}

	private static Map<Serializable, InstanceAccessPermissions> buildFullAccessMap(
			Collection<? extends Serializable> resources) {
		return resources.stream().map(toInstanceId()).distinct().collect(toMap(Function.identity(), FULL_ACCESS));
	}

	private static Function<Serializable, Serializable> toInstanceId() {
		return data -> {
			Serializable id;
			if (data instanceof Instance) {
				id = ((Instance) data).getId();
			} else if (data instanceof InstanceReference) {
				id = ((InstanceReference) data).getId();
			} else {
				id = data;
			}

			// we should not return any version ids in the results for this service,
			// so we make sure they are all normalised
			return InstanceVersionService.getIdFromVersionId(id);
		};
	}

	private static void verifyRoles(RoleIdentifier minimumReadRole, RoleIdentifier minimumWriteRole) {
		Objects.requireNonNull(minimumReadRole, "Read role is required.");
		Objects.requireNonNull(minimumWriteRole, "Write role is required.");
		if (minimumReadRole.getGlobalPriority() > minimumWriteRole.getGlobalPriority()) {
			throw new IllegalArgumentException("The read role [" + minimumReadRole.getIdentifier()
					+ "] has more permissions, then the write role [" + minimumWriteRole.getIdentifier()
					+ "], which is not allowed.");
		}
	}

	private Collection<InstanceReference> getReferences(Collection<? extends Serializable> references) {
		Collection<InstanceReference> converted = references
				.stream()
					.filter(isInstanceOrReference())
					.map(toReference())
					.collect(toList());

		if (converted.size() == references.size()) {
			return handleVersionReferencesIfAny(converted);
		}

		converted = handleVersionReferencesIfAny(converted);
		Set<Serializable> ids = references.stream().filter(isInstanceOrReference().negate()).collect(toSet());
		Collection<InstanceReference> resolvedFromIds = instanceTypeResolver.resolveReferences(ids);
		converted.addAll(handleVersionReferencesIfAny(resolvedFromIds));
		return converted;
	}

	private static Predicate<Serializable> isInstanceOrReference() {
		return s -> s instanceof InstanceReference || s instanceof Instance;
	}

	private static Function<Serializable, InstanceReference> toReference() {
		return s -> s instanceof InstanceReference ? (InstanceReference) s : ((Instance) s).toReference();
	}

	/**
	 * This method will convert version instance references to original instance references, because by requirement the
	 * versions should have the same permissions as the original instance. This behaviour is odd, but at the moment
	 * there is no other easy solution for version permission extraction.
	 * <p>
	 * <b>The results from this method will always be references of instances that are not versions. Be careful, if you
	 * need to use it.</b>
	 */
	private Collection<InstanceReference> handleVersionReferencesIfAny(Collection<InstanceReference> references) {
		Map<Serializable, InstanceReference> versionReferencesMap = references
				.stream()
					.filter(Objects::nonNull)
					.filter(reference -> InstanceVersionService.isVersion(reference.getId()))
					// the merge function is just in case there are cases where the key could be duplicated
					// although such cases should not exist, yet
					.collect(toMap(getIdFromVersionId(), Function.identity(), (k1, k2) -> k1));

		if (versionReferencesMap.isEmpty()) {
			return references;
		}

		references.removeAll(versionReferencesMap.values());
		Collection<InstanceReference> results = instanceTypeResolver.resolveReferences(versionReferencesMap.keySet());

		// we have versions of deleted instances and we need to load them as well
		if (results.size() != versionReferencesMap.size()) {
			results.forEach(result -> versionReferencesMap.remove(result.getId()));
			versionReferencesMap.forEach(
					(k, v) -> instanceService.loadDeleted(k).map(Instance::toReference).ifPresent(results::add));
		}

		// add remaining not version references, if any
		results.addAll(references);
		return results;
	}

	private static Function<InstanceReference, Serializable> getIdFromVersionId() {
		return reference -> InstanceVersionService.getIdFromVersionId(reference.getId());
	}

	/**
	 * Calculates permissions for every instance in the given collection, based on user's role for every instance. These
	 * checks are faster than the other implementation, but they are not covering all scenarios.
	 *
	 * @param permissions a {@link Map} where keys are {@link InstanceReference}s and values are {@link ResourceRole}s
	 * @param minimumReadRole the minimum read role that is required
	 * @param minimumWriteRole the minimum write role that is required
	 * @return {@link Map} where the keys are the ids of the instances and the values are their permissions
	 */
	private static Map<Serializable, InstanceAccessPermissions> calculatePermissions(
			Map<InstanceReference, ResourceRole> permissions, RoleIdentifier minimumReadRole,
			RoleIdentifier minimumWriteRole) {
		int readRolePriority = minimumReadRole.getGlobalPriority();
		int writeRolePriority = minimumWriteRole.getGlobalPriority();
		Map<Serializable, InstanceAccessPermissions> results = new HashMap<>(permissions.size());
		for (Entry<InstanceReference, ResourceRole> entry : permissions.entrySet()) {
			String id = entry.getKey().getId();
			int rolePriority = entry.getValue().getRole().getGlobalPriority();
			if (rolePriority >= writeRolePriority) {
				results.put(id, InstanceAccessPermissions.CAN_WRITE);
			} else if (rolePriority >= readRolePriority) {
				results.put(id, InstanceAccessPermissions.CAN_READ);
			} else {
				results.put(id, InstanceAccessPermissions.NO_ACCESS);
			}
		}

		return results;
	}

	@Override
	public boolean canRead(Serializable instanceId, Serializable resourceId) {
		if (instanceId == null) {
			return false;
		}

		return isAtLeastRole(resolveReference(instanceId), getResource(resourceId), BaseRoles.VIEWER);
	}

	@Override
	public boolean canWrite(Serializable instanceId, Serializable resourceId) {
		if (instanceId == null) {
			return false;
		}

		return isAtLeastRole(resolveReference(instanceId), getResource(resourceId), BaseRoles.CONTRIBUTOR);
	}

	@Override
	public Map<Serializable, InstanceAccessPermissions> getAccessPermissions(Collection<Instance> identifiers,
			Serializable resourceId) {
		return calculatePermissions(identifiers, resourceId);
	}

	/**
	 * Calculates permissions for every instance in the given collection, based on
	 * {@link ActionTypeConstants#EDIT_DETAILS} and {@link ActionTypeConstants#READ} actions. These checks are slower
	 * than the other implementation, but they cover all scenarios.
	 *
	 * @param instances which permissions to be calculated
	 * @param resourceId of a user
	 * @return {@link Map} where the keys are the ids of the instances and the values are their permissions
	 */
	private Map<Serializable, InstanceAccessPermissions> calculatePermissions(Collection<Instance> instances,
			Serializable resourceId) {
		if (isEmpty(instances)) {
			return emptyMap();
		}

		Resource resource = getResource(resourceId);
		if (isAdmin(resource)) {
			return buildFullAccessMap(instances);
		}

		Map<Serializable, InstanceAccessPermissions> results = CollectionUtils.createHashMap(instances.size());
		for (Instance instance : instances) {
			Serializable id = InstanceVersionService.getIdFromVersionId(instance.getId());
			Set<String> allowedActions = authorityService.getAllowedActionNames(instance, null);

			if (allowedActions.contains(ActionTypeConstants.EDIT_DETAILS)) {
				results.put(id, InstanceAccessPermissions.CAN_WRITE);
			} else if (allowedActions.contains(ActionTypeConstants.READ)) {
				results.put(id, InstanceAccessPermissions.CAN_READ);
			} else {
				results.put(id, InstanceAccessPermissions.NO_ACCESS);
			}
		}

		return results;
	}

	@Override
	public boolean actionAllowed(Serializable instanceId, Serializable resourceId, String actionId) {
		if (instanceId == null || StringUtils.isBlank(actionId)) {
			return false;
		}
		Resource resource = getResource(resourceId);
		if (isAdmin(resource)) {
			return true;
		}

		InstanceReference reference = resolveReference(instanceId).get();
		ResourceRole resourceRole = permissionService.getPermissionAssignment(reference, resource.getId());
		if (resourceRole != null) {
			Role role = roleRegistry.find(resourceRole.getRole());
			return role.getAllAllowedActions().stream().anyMatch(action -> action.getActionId().equals(actionId));
		}

		return false;
	}

}
