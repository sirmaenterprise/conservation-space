package com.sirma.itt.seip.permissions.role;

import static com.sirma.itt.seip.collections.CollectionUtils.emptyMap;
import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;
import static com.sirma.itt.seip.collections.CollectionUtils.isNotEmpty;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.INHERIT_LIBRARY_PERMISSIONS;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.INHERIT_PARENT_PERMISSIONS;
import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.permissions.EntityPermissions;
import com.sirma.itt.seip.permissions.InstancePermissionsHierarchyResolver;
import com.sirma.itt.seip.permissions.PermissionAssignmentChange;
import com.sirma.itt.seip.permissions.PermissionInheritanceChange;
import com.sirma.itt.seip.permissions.PermissionModelChangedEvent;
import com.sirma.itt.seip.permissions.PermissionModelType;
import com.sirma.itt.seip.permissions.PermissionService;
import com.sirma.itt.seip.permissions.PermissionsRestored;
import com.sirma.itt.seip.permissions.SecurityModel;
import com.sirma.itt.seip.permissions.role.PermissionsChange.AddRoleAssignmentChange;
import com.sirma.itt.seip.permissions.role.PermissionsChange.InheritFromLibraryChange;
import com.sirma.itt.seip.permissions.role.PermissionsChange.InheritFromParentChange;
import com.sirma.itt.seip.permissions.role.PermissionsChange.LibraryChange;
import com.sirma.itt.seip.permissions.role.PermissionsChange.ParentChange;
import com.sirma.itt.seip.permissions.role.PermissionsChange.RemoveRoleAssignmentChange;
import com.sirma.itt.seip.permissions.role.PermissionsChange.SetLibraryIndicatorChange;
import com.sirma.itt.seip.resources.ResourceService;

/**
 * The PermissionServiceImpl is implementation of {@link PermissionService} including caching.
 *
 * @author bbanchev
 * @author A. Kunchev
 */
@ApplicationScoped
public class RolePermissionServiceImpl implements PermissionService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private RoleService roleService;

	@Inject
	private ResourceService resourceService;

	@Inject
	private EventService eventService;

	@Inject
	private InstancePermissionsHierarchyResolver hierarchyResolver;

	@Inject
	private EntityPermissionDao entityPermissionDao;

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Override
	public Map<String, ResourceRole> getPermissionAssignments(Serializable id, Boolean includeParentPermissions,
			Boolean includeLibraryPermissions) {
		if (Objects.toString(id, "").isEmpty()) {
			return emptyMap();
		}

		EntityPermission entityPermission = loadEntitiesPermissions(Collections.singleton(id)).get(id);
		return retrieveAssigments(entityPermission, includeParentPermissions, includeLibraryPermissions);
	}

	private Map<String, EntityPermission> loadEntitiesPermissions(Collection<Serializable> ids) {
		Map<String, EntityPermission> permissionEntities = entityPermissionDao
				.fetchHierarchyWithAssignmentsForInstances(ids);
		if (isNotEmpty(permissionEntities)) {
			return permissionEntities;
		}

		// WORKAROUND:
		// this is done to provide permissions for imported instances
		// when migrating to microservices the approach should be changed
		Map<String, EntityPermission> libraryPermissionMap = new HashMap<>(ids.size());
		// TODO create real cache maybe
		// used so that we don't need to query library permissions, if they were extracted once already
		Map<String, EntityPermission> localLibraryPermissionsCache = new HashMap<>();
		Collection<InstanceReference> references = instanceTypeResolver.resolveReferences(ids);
		for (InstanceReference reference : references) {
			EntityPermission permission = buildLibraryPermissions(reference, localLibraryPermissionsCache);
			libraryPermissionMap.put(reference.getId(), permission);
		}

		return libraryPermissionMap;
	}

	private EntityPermission buildLibraryPermissions(InstanceReference reference, Map<String, EntityPermission> cache) {
		EntityPermission permission = new EntityPermission();
		InstanceReference library = hierarchyResolver.getLibrary(reference);
		if (library != null) {
			InstanceType libraryType = library.getType();
			permission.setInheritFromLibrary(libraryType.hasTrait(INHERIT_LIBRARY_PERMISSIONS));
			permission.setInheritFromParent(libraryType.hasTrait(INHERIT_PARENT_PERMISSIONS));
			EntityPermission libraryPermissionEntity = getLibraryPermissions(library, cache);
			permission.setLibrary(libraryPermissionEntity);
		} else {
			// this here happens when the instance is library itself.
			// the libraries does not have libraries, yet
			permission.setInheritFromLibrary(false);
			permission.setInheritFromParent(false);
		}

		return permission;
	}

	private EntityPermission getLibraryPermissions(InstanceReference library, Map<String, EntityPermission> cache) {
		return cache.computeIfAbsent(library.getId(), id -> entityPermissionDao.loadWithAssignments(id).orElse(null));
	}

	private Map<String, ResourceRole> retrieveAssigments(EntityPermission entityPermission,
			Boolean includeParentPermissions, Boolean includeLibraryPermissions) {
		if (entityPermission == null) {
			return emptyMap();
		}

		Map<String, ResourceRole> assignments = getAssignmentsForInstance(new HashMap<>(), entityPermission,
				includeParentPermissions, includeLibraryPermissions);

		// migrate active permissions from RoleAssignments to ResourceRole
		for (Entry<String, ResourceRole> assignment : assignments.entrySet()) {
			ResourceRole resourceRole = assignment.getValue();
			RoleIdentifier roleIdentifier = roleService
					.getRoleIdentifier(resourceRole.getRoleAssignments().getActive());
			resourceRole.setRole(roleIdentifier);
		}

		return assignments;
	}

	/**
	 * Algorithm: <br/>
	 * - Get the special permissions. <br/>
	 * - If library inheritance is enabled, get all the permissions assigned to the library. <br/>
	 * - If inherit permissions from parent is enabled, get its permission traversing upward its hierarchy using the
	 * same algorithm (and library if library inheritance is enabled). <br/>
	 * - If library inheritance is not enabled, the library assignments still have to taken into account but only the
	 * manager role.
	 *
	 * @param assignments currently discovered assignments mapped by authority id.
	 * @param entityPermission EntityPermission corresponding to the current object that is processed.
	 * @param includeParentPermissions when false, the parent permissions will be skipped.
	 * @param includeLibraryPermissions when false, the library permissions will be skipped.
	 * @return currently discovered assignments mapped by authority id.
	 */
	private Map<String, ResourceRole> getAssignmentsForInstance(Map<String, ResourceRole> assignments,
			EntityPermission entityPermission, Boolean includeParentPermissions, Boolean includeLibraryPermissions) {

		// inherit library permissions if enabled in the database or using includeLibraryPermissions
		boolean libraryInheritanceEnabled = entityPermission.getInheritFromLibrary()
				&& includeLibraryPermissions == null || Boolean.TRUE.equals(includeLibraryPermissions);

		inheritPermissionAssignments(assignments, entityPermission.getLibrary(), !libraryInheritanceEnabled,
				PermissionModelType.LIBRARY);

		// inherit parent permissions if enabled in the database or using includeParentPermissions
		boolean inheritFromParent = entityPermission.getInheritFromParent() && includeParentPermissions == null
				|| Boolean.TRUE.equals(includeParentPermissions);

		inheritPermissionAssignments(assignments, entityPermission.getParent(), !inheritFromParent,
				PermissionModelType.INHERITED);

		includeSpecialPermissions(assignments, entityPermission);

		return assignments;
	}

	/**
	 * The inherited permissions are taken in two ways - when the parent/library inheritance is enabled all permissions
	 * assignments are taken. However when the parent/library inheritance is disabled only the permission assignments
	 * for the manager role from the parent/library are taken.
	 *
	 * @param assignments map where to store the inherited assignments
	 * @param entityPermission EntityPermission for which to inherit assignments.
	 * @param modelType model type to use when gathering the permissions
	 * @param managersOnly if true, only the manager assignments will be returned.
	 */
	private void inheritPermissionAssignments(Map<String, ResourceRole> assignments, EntityPermission entityPermission,
			boolean managersOnly, PermissionModelType modelType) {
		if (entityPermission == null) {
			return;
		}

		Map<String, ResourceRole> inheritedAssignments = new HashMap<>();

		getAssignmentsForInstance(inheritedAssignments, entityPermission, null, null);

		for (Entry<String, ResourceRole> inheritedAssignment : inheritedAssignments.entrySet()) {
			String authorityId = inheritedAssignment.getKey();
			ResourceRole inheritedResourceRole = inheritedAssignment.getValue();

			if (!managersOnly || inheritedResourceRole.getRoleAssignments().isManager()) {
				addAssignment(assignments, authorityId, inheritedResourceRole.getRoleAssignments().getActive(),
						modelType, entityPermission.getTargetId());
			}
		}
	}

	private void addAssignment(Map<String, ResourceRole> assignments, String authorityId, String role,
			PermissionModelType modelType, String inheritedFrom) {
		ResourceRole resourceRole = assignments.computeIfAbsent(authorityId, authority -> {
			ResourceRole newRole = new ResourceRole();
			newRole.setRoleAssignments(new RoleAssignments(roleService.getManagerRole().getIdentifier()));
			newRole.setAuthorityId(authority);
			newRole.setInheritedFromReference(inheritedFrom);
			return newRole;
		});

		resourceRole.getRoleAssignments().addAssignment(role, modelType);
	}

	private void includeSpecialPermissions(Map<String, ResourceRole> assignments, EntityPermission entityPermission) {
		for (AuthorityRoleAssignment assignment : entityPermission.getAssignments()) {
			addAssignment(assignments, assignment.getAuthority(), assignment.getRole(), PermissionModelType.SPECIAL,
					null);
		}
	}

	@Override
	public Map<Serializable, Map<String, ResourceRole>> getPermissionAssignments(Collection<Serializable> ids,
			Boolean includeParent, Boolean includeLibrary) {
		if (isEmpty(ids)) {
			return emptyMap();
		}

		Map<String, EntityPermission> entitiesPermissions = loadEntitiesPermissions(ids);
		Map<Serializable, Map<String, ResourceRole>> results = new HashMap<>(entitiesPermissions.size());
		for (Serializable id : ids) {
			EntityPermission entityPermission = entitiesPermissions.get(id);
			Map<String, ResourceRole> assigments = retrieveAssigments(entityPermission, includeParent, includeLibrary);
			results.put(id, assigments);
		}

		return results;
	}

	@Override
	public ResourceRole getPermissionAssignment(Serializable id, Serializable authorityId) {
		String authority = Objects.toString(authorityId, "");
		if (Objects.toString(id, "").isEmpty() || authority.isEmpty()) {
			return null;
		}

		Map<String, ResourceRole> permissionAssignments = getPermissionAssignments(id);
		return getResourceRole(authority, permissionAssignments).orElse(null);
	}

	private Optional<ResourceRole> getResourceRole(String authority, Map<String, ResourceRole> permissionAssignments) {
		if (isEmpty(permissionAssignments)) {
			return Optional.empty();
		}

		ResourceRole role = permissionAssignments.get(authority);
		if (role != null) {
			return Optional.of(role);
		}

		List<String> containingAuthorities = getContainingResources(authority);
		removeAllOtherUsersIfNeeded(permissionAssignments, containingAuthorities);

		List<ResourceRole> roles = permissionAssignments
				.entrySet()
					.stream()
					.filter(assignment -> containingAuthorities.contains(assignment.getKey()))
					.map(Entry::getValue)
					.collect(Collectors.toList());

		return Optional.ofNullable(findMaxRole(roles));
	}

	private List<String> getContainingResources(String authorityId) {
		return resourceService
				.getContainingResources(authorityId)
					.stream()
					.map(authority -> authority.getId().toString())
					.collect(Collectors.toList());
	}

	/**
	 * Removes all other users from containing authorities if the user has assignment via some other group in which he
	 * is member. This way all other users will not be taken into account when resolving user's role for the instance,
	 * since by requirement the role for all other users must have lower priority than the roles of user's groups.
	 *
	 * @param permissionAssignments for the current instance
	 * @param containingAuthorities groups in which the current user is member of
	 */
	private void removeAllOtherUsersIfNeeded(Map<String, ResourceRole> permissionAssignments,
			List<String> containingAuthorities) {
		String allOtherUsersId = (String) resourceService.getAllOtherUsers().getId();
		Set<String> assignedAuthorities = permissionAssignments.keySet();
		for (Serializable assignedAuthority : assignedAuthorities) {
			if (!assignedAuthority.equals(allOtherUsersId) && containingAuthorities.contains(assignedAuthority)) {
				containingAuthorities.remove(allOtherUsersId);
				break;
			}
		}
	}

	private ResourceRole findMaxRole(Collection<ResourceRole> roles) {
		List<RoleIdentifier> activeRoles = roleService.getActiveRoles();
		int maxLevel = -1;
		ResourceRole maxResourceRole = null;
		for (ResourceRole resourceRole : roles) {
			int level = activeRoles.indexOf(roleService.getRoleIdentifier(resourceRole.getRole().getIdentifier()));
			// TODO find better way to compare roles
			if (maxLevel == activeRoles.size()) {
				// if manager from some group just return it
				return resourceRole;
			} else if (level > maxLevel) {
				maxResourceRole = resourceRole;
				maxLevel = level;
			}
		}
		return maxResourceRole;
	}

	@Override
	public Map<Serializable, ResourceRole> getPermissionAssignmentForIds(Collection<Serializable> ids,
			Serializable authorityId) {
		String authority = Objects.toString(authorityId, "");
		if (isEmpty(ids) || authority.isEmpty()) {
			return emptyMap();
		}

		Map<Serializable, Map<String, ResourceRole>> assignments = getPermissionAssignments(ids, null, null);
		return ids.stream().collect(
				Collectors.toMap(Function.identity(), id -> getResourceRole(authority, assignments.get(id))
						.orElseGet(() -> buildNoPermissions(id, authority))));
	}

	private ResourceRole buildNoPermissions(Serializable targetId, Serializable authority) {
		ResourceRole role = new ResourceRole();
		role.setAuthorityId(authority.toString());
		role.setRoleAssignments(new RoleAssignments(roleService.getManagerRole().getIdentifier()));
		role.setTargetReference(targetId.toString());
		role.setRole(SecurityModel.BaseRoles.NO_PERMISSION);
		return role;
	}

	@Override
	public Optional<EntityPermissions> getPermissionsInfo(Serializable id) {
		if (Objects.toString(id, "").isEmpty()) {
			return Optional.empty();
		}

		return entityPermissionDao.fetchPermissions(id);
	}

	@Override
	public void setPermissions(InstanceReference reference, Collection<PermissionsChange> changes) {
		if (!isValidReference(reference)) {
			throw new IllegalArgumentException("No reference is provided");
		}

		if (isEmpty(changes)) {
			return;
		}

		String id = reference.getId();
		LOGGER.info("Setting permissions for {}", id);
		EntityPermission entityPermission = entityPermissionDao
				.loadWithAssignments(id)
					.orElseGet(() -> new EntityPermission().setTargetId(id));

		Set<PermissionAssignmentChange> changeSet = new HashSet<>();

		handleRemovedAssignments(entityPermission, changes, changeSet);

		handleAddedAssignments(entityPermission, changes, changeSet);

		handleParentInheritanceChange(entityPermission, changes, changeSet);

		handleLibraryInheritanceChange(entityPermission, changes, changeSet);

		handleLibraryIndicatorChange(entityPermission, changes);

		ensureAtLeastOneManager(reference, entityPermission);

		entityPermissionDao.save(entityPermission);

		eventService.fire(new PermissionModelChangedEvent(reference, changeSet));
	}

	private static boolean isValidReference(InstanceReference reference) {
		if (reference == null) {
			LOGGER.error("Requested permission retrieval for null reference");
			return false;
		}

		if (reference.getId() == null) {
			LOGGER.error("Requested permission retrieval for reference with null id");
			return false;
		}

		return true;
	}

	private static void handleRemovedAssignments(EntityPermission entityPermission,
			Collection<PermissionsChange> changes, Set<PermissionAssignmentChange> changeset) {
		changes
				.stream()
					.filter(RemoveRoleAssignmentChange.class::isInstance)
					.map(RemoveRoleAssignmentChange.class::cast)
					.forEach(removeAssignment(entityPermission, changeset));
	}

	private static Consumer<RemoveRoleAssignmentChange> removeAssignment(EntityPermission entityPermission,
			Set<PermissionAssignmentChange> changeSet) {
		return change -> {
			String authority = change.getAuthority();
			String role = change.getRole();
			AuthorityRoleAssignment assignmentToRemove = findAssignment(entityPermission, authority);
			if (assignmentToRemove != null && assignmentToRemove.getRole().equals(role)) {
				entityPermission.getAssignments().remove(assignmentToRemove);
				changeSet.add(new PermissionAssignmentChange(authority, role, null));
			}
		};
	}

	private static AuthorityRoleAssignment findAssignment(EntityPermission entityPermission, String authority) {
		return entityPermission
				.getAssignments()
					.stream()
					.filter(assignment -> assignment.getAuthority().equals(authority))
					.findFirst()
					.orElse(null);
	}

	private static void handleAddedAssignments(EntityPermission entityPermission, Collection<PermissionsChange> changes,
			Set<PermissionAssignmentChange> changeset) {
		changes
				.stream()
					.filter(AddRoleAssignmentChange.class::isInstance)
					.map(AddRoleAssignmentChange.class::cast)
					.forEach(addAssignment(entityPermission, changeset));
	}

	private static Consumer<AddRoleAssignmentChange> addAssignment(EntityPermission entityPermission,
			Set<PermissionAssignmentChange> changeset) {
		return change -> {
			String authority = change.getAuthority();
			String role = change.getRole();
			AuthorityRoleAssignment existingAssignment = findAssignment(entityPermission, authority);
			if (existingAssignment == null) {
				changeset.add(new PermissionAssignmentChange(authority, null, role));
				entityPermission.getAssignments().add(new AuthorityRoleAssignment(authority, role));
			} else if (!existingAssignment.getRole().equals(role)) {
				changeset.add(new PermissionAssignmentChange(authority, existingAssignment.getRole(), role));
				existingAssignment.setRole(role);
			}
		};
	}

	private void handleParentInheritanceChange(EntityPermission entityPermission, Collection<PermissionsChange> changes,
			Set<PermissionAssignmentChange> changeSet) {
		boolean changed = false;
		String oldParent = getTargetId(entityPermission.getParent());
		String newParent = oldParent;

		Optional<ParentChange> parentChange = changes
				.stream()
					.filter(ParentChange.class::isInstance)
					.map(ParentChange.class::cast)
					.filter(change -> !nullSafeEquals(oldParent, change.getValue()))
					.findFirst();

		if (parentChange.isPresent()) {
			newParent = parentChange.get().getValue();
			EntityPermission parent = null;
			if (newParent != null) {
				parent = entityPermissionDao.load(newParent).orElse(null);
			}
			entityPermission.setParent(parent);
			changed = true;
		}

		changed |= handleInheritanceChanges(entityPermission, changes);
		addIfChanged(changed, !entityPermission.getInheritFromParent(), changeSet, oldParent, newParent);
	}

	private static String getTargetId(EntityPermission entityPermission) {
		return entityPermission == null ? null : entityPermission.getTargetId();
	}

	private static boolean handleInheritanceChanges(EntityPermission entityPermission,
			Collection<PermissionsChange> changes) {
		return changes
				.stream()
					.filter(InheritFromParentChange.class::isInstance)
					.map(InheritFromParentChange.class::cast)
					.filter(change -> entityPermission.getInheritFromParent() != change.getValue())
					.findFirst()
					.map(change -> {
						entityPermission.setInheritFromParent(change.getValue());
						return change;
					})
					.isPresent();
	}

	private static void addIfChanged(boolean changed, boolean managersOnly, Set<PermissionAssignmentChange> changeSet,
			String oldParent, String newParent) {
		if (!changed) {
			return;
		}

		changeSet.add(new PermissionInheritanceChange(oldParent, newParent, managersOnly));
	}

	private void handleLibraryInheritanceChange(EntityPermission entityPermission,
			Collection<PermissionsChange> changes, Set<PermissionAssignmentChange> changeSet) {
		boolean changed = false;
		String oldLibrary = getTargetId(entityPermission.getLibrary());
		String newLibrary = oldLibrary;

		Optional<LibraryChange> libraryChange = changes
				.stream()
					.filter(LibraryChange.class::isInstance)
					.map(LibraryChange.class::cast)
					.filter(change -> !nullSafeEquals(oldLibrary, change.getValue()))
					.findFirst();

		if (libraryChange.isPresent()) {
			newLibrary = libraryChange.get().getValue();
			EntityPermission library = null;
			if (newLibrary != null) {
				library = entityPermissionDao.load(newLibrary).orElse(null);
			}
			entityPermission.setLibrary(library);
			changed = true;
		}

		changed |= handleLibraryChange(entityPermission, changes);
		addIfChanged(changed, !entityPermission.getInheritFromLibrary(), changeSet, oldLibrary, newLibrary);
	}

	private static boolean handleLibraryChange(EntityPermission entityPermission,
			Collection<PermissionsChange> changes) {
		return changes
				.stream()
					.filter(InheritFromLibraryChange.class::isInstance)
					.map(InheritFromLibraryChange.class::cast)
					.filter(change -> entityPermission.getInheritFromLibrary() != change.getValue())
					.findFirst()
					.map(change -> {
						entityPermission.setInheritFromLibrary(change.getValue());
						return change;
					})
					.isPresent();
	}

	private static void handleLibraryIndicatorChange(EntityPermission entityPermission,
			Collection<PermissionsChange> changes) {
		changes
				.stream()
					.filter(SetLibraryIndicatorChange.class::isInstance)
					.map(SetLibraryIndicatorChange.class::cast)
					.map(SetLibraryIndicatorChange::getValue)
					.forEach(entityPermission::setIsLibrary);
	}

	/**
	 * Ensures that at least one manager is available for the particular instance. <br>
	 * The following conditions must be true in order to check for manager permissions:
	 * <ul>
	 * <li>the instance is not a group - groups created in idp have only consumer permissions</li>
	 * <li>the instance has no parent - meaning this is a root</li>
	 * <li>the instance is not a library - we must allow libraries without any permissions</li>
	 * </ul>
	 * This way no need to check the entire hierarchy, because we ensure that the root will always have at least one
	 * manager. The library permissions are also checked.
	 *
	 * @param reference of instance to check
	 * @param entityPermission entityPermission object
	 */
	private void ensureAtLeastOneManager(InstanceReference reference, EntityPermission entityPermission) {
		if (!reference.getType().is(ObjectTypes.GROUP) && isRootAndNotLibrary(entityPermission)
				&& !hasManagerPermissions(entityPermission) && !hasManagerPermissions(entityPermission.getLibrary())) {
			throw new EmfRuntimeException("Missing at least one manager on root level for: " + reference.getId());
		}
	}

	private static boolean isRootAndNotLibrary(EntityPermission entityPermission) {
		return entityPermission.getParent() == null && !entityPermission.isLibrary();
	}

	private boolean hasManagerPermissions(EntityPermission entityPermission) {
		if (entityPermission == null) {
			return false;
		}

		for (AuthorityRoleAssignment assignment : entityPermission.getAssignments()) {
			if (roleService.isManagerRole(assignment.getRole())) {
				return true;
			}
		}

		return false;
	}

	@Override
	public PermissionModelType getPermissionModel(Serializable id) {
		String instanceId = Objects.toString(id, "");
		if (instanceId.isEmpty()) {
			return PermissionModelType.UNDEFINED;
		}

		Optional<EntityPermission> entityPermission = entityPermissionDao.loadWithAssignments(instanceId);
		if (!entityPermission.isPresent()) {
			return PermissionModelType.UNDEFINED;
		}

		boolean inheritFromParent = false;
		boolean inheritFromLibrary = false;
		boolean special = false;
		EntityPermission permission = entityPermission.get();
		if (permission.getInheritFromParent()) {
			inheritFromParent = true;
		}

		if (permission.getInheritFromLibrary()) {
			inheritFromLibrary = true;
		}

		special = !permission.getAssignments().isEmpty();
		return new PermissionModelType(inheritFromParent, inheritFromLibrary, special);
	}

	@Override
	public void restoreParentPermissions(InstanceReference reference) {
		if (reference == null) {
			return;
		}

		String instanceId = reference.getId();
		LOGGER.info("Restoring permissions from parent for descendants of {}", instanceId);
		Collection<String> ids = entityPermissionDao.getDescendants(instanceId);
		if (isEmpty(ids)) {
			LOGGER.debug("There are no descendants for instance - {}", instanceId);
			return;
		}
		// the current instance shouldn't be touched
		ids.remove(instanceId);

		Collection<InstanceReference> descendants = instanceTypeResolver.resolveReferences(ids);

		for (InstanceReference currentReference : descendants) {
			EntityPermission entityPermission = entityPermissionDao
					.loadWithAssignments(currentReference.getId())
						.orElseGet(EntityPermission::new);

			Set<PermissionAssignmentChange> changeset = new LinkedHashSet<>();
			for (AuthorityRoleAssignment assignment : entityPermission.getAssignments()) {
				changeset.add(new PermissionAssignmentChange(assignment.getAuthority(), assignment.getRole(), null));
			}

			entityPermissionDao.deleteAssignments(entityPermission.getId());

			handleParentInheritance(currentReference, entityPermission, changeset);

			if (!changeset.isEmpty()) {
				LOGGER.info("Restoring permissions from parent for {}", currentReference.getId());
				eventService.fire(new PermissionModelChangedEvent(currentReference, changeset));
				eventService.fire(new PermissionsRestored(currentReference.toInstance()));
			}
		}
	}

	private void handleParentInheritance(InstanceReference currentReference, EntityPermission entityPermission,
			Set<PermissionAssignmentChange> changeset) {
		boolean libraryFlag = currentReference.getType().hasTrait(INHERIT_PARENT_PERMISSIONS);
		if (entityPermission.getInheritFromParent() != libraryFlag) {
			entityPermission.setInheritFromParent(libraryFlag);
			String parentId = entityPermission.getParent().getTargetId();
			changeset.add(new PermissionInheritanceChange(parentId, parentId, !libraryFlag));
			entityPermissionDao.save(entityPermission);
		}
	}

	@Override
	public boolean checkIsRoot(Serializable id) {
		String instanceId = Objects.toString(id, "");
		if (instanceId.isEmpty()) {
			return false;
		}

		return hierarchyResolver.isInstanceRoot(instanceId);
	}
}