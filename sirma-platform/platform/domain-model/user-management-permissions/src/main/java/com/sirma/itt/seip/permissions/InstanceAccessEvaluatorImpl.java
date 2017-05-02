package com.sirma.itt.seip.permissions;

import static com.sirma.itt.seip.collections.CollectionUtils.emptyMap;
import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;
import static com.sirma.itt.seip.util.EqualsHelper.getOrDefault;

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
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.exceptions.InstanceNotFoundException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.permissions.SecurityModel.BaseRoles;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.permissions.role.ResourceRole;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;
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
		return role.getRole().getGlobalPriority() >= minimumRole.getGlobalPriority();
	}

	private Supplier<InstanceReference> resolveReference(Serializable instanceId) {
		return () -> {
			InstanceReference reference = null;
			if (instanceId instanceof InstanceReference) {
				reference = (InstanceReference) instanceId;
			} else if (instanceId instanceof Instance) {
				reference = ((Instance) instanceId).toReference();
			}

			reference = handleVersion(reference);
			if (reference != null) {
				return reference;
			}

			// gets original instance id, if version id is passed
			Serializable id = InstanceVersionService.getIdFromVersionId(instanceId);
			return instanceTypeResolver
					.resolveReference(id)
						.orElseThrow(() -> new InstanceNotFoundException(instanceId.toString()));
		};
	}

	// gets original instance reference, if version instance is passed
	// TODO it will do for now, but this looks more like hack then anything else
	// Implement CMF-22836 !!!
	private InstanceReference handleVersion(InstanceReference reference) {
		if (reference == null || !InstanceVersionService.isVersion(reference.getIdentifier())) {
			return reference;
		}

		Serializable id = InstanceVersionService.getIdFromVersionId(reference.getIdentifier());
		return instanceTypeResolver
				.resolveReference(id)
					.orElseThrow(() -> new InstanceNotFoundException(id.toString()));
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
			return identifiers.stream().collect(Collectors.toMap(Function.identity(), FULL_ACCESS));
		}

		// all of this logic will be removed, when CMF-22836 is done
		Collection<InstanceReference> references = getReferences(identifiers);
		if (isEmpty(references)) {
			return emptyMap();
		}

		Map<InstanceReference, ResourceRole> permissions = permissionService.getPermissionAssignment(references,
				resource.getId());
		if (isEmpty(permissions)) {
			return emptyMap();
		}

		return calculatePermissions(permissions, minimumReadRole, minimumWriteRole);
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
					.collect(Collectors.toList());

		if (converted.size() == references.size()) {
			return handleVersionReferencesIfAny(converted);
		}

		converted = handleVersionReferencesIfAny(converted);
		Set<Serializable> ids = references
				.stream()
					.filter(isInstanceOrReference().negate())
					.map(InstanceVersionService::getIdFromVersionId)
					.collect(Collectors.toSet());
		converted.addAll(instanceTypeResolver.resolveReferences(ids));
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
					.filter(reference -> InstanceVersionService.isVersion(reference.getIdentifier()))
					.collect(Collectors.toMap(getIdFromVersionId(), Function.identity()));

		if (versionReferencesMap.isEmpty()) {
			return references;
		}

		references.removeAll(versionReferencesMap.values());
		Collection<InstanceReference> results = instanceTypeResolver.resolveReferences(versionReferencesMap.keySet());
		// add remaining not version references, if any
		results.addAll(references);
		return results;
	}

	private static Function<InstanceReference, Serializable> getIdFromVersionId() {
		return reference -> InstanceVersionService.getIdFromVersionId(reference.getIdentifier());
	}

	private static Map<Serializable, InstanceAccessPermissions> calculatePermissions(
			Map<InstanceReference, ResourceRole> permissions, RoleIdentifier minimumReadRole,
			RoleIdentifier minimumWriteRole) {
		int readRolePriority = minimumReadRole.getGlobalPriority();
		int writeRolePriority = minimumWriteRole.getGlobalPriority();
		Map<Serializable, InstanceAccessPermissions> results = new HashMap<>(permissions.size());
		for (Entry<InstanceReference, ResourceRole> entry : permissions.entrySet()) {
			String id = entry.getKey().getIdentifier();
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
	public Map<Serializable, InstanceAccessPermissions> getAccessPermissions(
			Collection<? extends Serializable> identifiers, Serializable resourceId) {
		return isAtLeastRole(identifiers, resourceId, BaseRoles.VIEWER, BaseRoles.CONTRIBUTOR);
	}

}
