package com.sirma.itt.seip.permissions.role;

import static com.sirma.itt.seip.collections.CollectionUtils.emptyMap;
import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;
import static com.sirma.itt.seip.collections.CollectionUtils.isNotEmpty;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.INHERIT_LIBRARY_PERMISSIONS;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.INHERIT_PARENT_PERMISSIONS;
import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
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
 */
@ApplicationScoped
public class RolePermissionServiceImpl implements PermissionService {

	private static final Logger LOGGER = LoggerFactory.getLogger(RolePermissionServiceImpl.class);

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
	public Map<String, ResourceRole> getPermissionAssignments(InstanceReference reference,
			Boolean includeParentPermissions, Boolean includeLibraryPermissions) {
		if (!isValidReference(reference)) {
			return emptyMap();
		}

		EntityPermission entityPermission = loadEntitiesPermissions(Arrays.asList(reference))
				.get(reference.getIdentifier());
		return retrieveAssigments(entityPermission, includeParentPermissions, includeLibraryPermissions);
	}

	private Map<String, ResourceRole> retrieveAssigments(EntityPermission entityPermission,
			Boolean includeParentPermissions, Boolean includeLibraryPermissions) {
		if (entityPermission == null) {
			return emptyMap();
		}

		Map<String, ResourceRole> assignments = getAssignmentsForInstance(new HashMap<>(), entityPermission,
				includeParentPermissions, includeLibraryPermissions);

		if (!assignments.containsKey(getAllOtherId())) {
			addAssignment(assignments, getAllOtherId(), SecurityModel.BaseRoles.NO_PERMISSION.getIdentifier(),
					PermissionModelType.SPECIAL);
		}

		// migrate active permissions from RoleAssignments to ResourceRole
		for (Entry<String, ResourceRole> assignment : assignments.entrySet()) {
			ResourceRole resourceRole = assignment.getValue();
			RoleIdentifier roleIdentifier = roleService
					.getRoleIdentifier(resourceRole.getRoleAssignments().getActive());
			resourceRole.setRole(roleIdentifier);
		}

		return assignments;
	}

	private Map<String, EntityPermission> loadEntitiesPermissions(Collection<InstanceReference> references) {
		Set<Serializable> ids = references.stream().map(InstanceReference::getIdentifier).collect(Collectors.toSet());
		Map<String, EntityPermission> permissionEntities = entityPermissionDao
				.fetchHierarchyWithAssignmentsForInstances(ids);
		if (isNotEmpty(permissionEntities)) {
			return permissionEntities;
		}

		// WORKAROUND:
		// this is done to provide permissions for imported instances
		// when migrating to microservices the approach should be changed
		Map<String, EntityPermission> libraryPermissionMap = new HashMap<>(references.size());
		// TODO create real cache maybe
		// used so that we don't need to query library permissions, if they were extracted once already
		Map<String, EntityPermission> localLibraryPermissionsCache = new HashMap<>();
		for (InstanceReference reference : references) {
			EntityPermission permission = buildLibraryPermissions(reference, localLibraryPermissionsCache);
			libraryPermissionMap.put(reference.getIdentifier(), permission);
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
		return cache.computeIfAbsent(library.getIdentifier(),
				id -> entityPermissionDao.loadWithAssignments(id).orElse(null));
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
	 * @param assignments
	 *            currently discovered assignments mapped by authority id.
	 * @param entityPermission
	 *            EntityPermission corresponding to the current object that is processed.
	 * @param includeParentPermissions
	 *            when false, the parent permissions will be skipped.
	 * @param includeLibraryPermissions
	 *            when false, the library permissions will be skipped.
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
		boolean inheritFromParent = entityPermission.getInheritFromParent()
				&& includeParentPermissions == null || Boolean.TRUE.equals(includeParentPermissions);

		inheritPermissionAssignments(assignments, entityPermission.getParent(), !inheritFromParent,
				PermissionModelType.INHERITED);

		includeSpecialPermissions(assignments, entityPermission);

		return assignments;
	}

	private void includeSpecialPermissions(Map<String, ResourceRole> assignments, EntityPermission entityPermission) {
		for (AuthorityRoleAssignment assignment : entityPermission.getAssignments()) {
			addAssignment(assignments, assignment.getAuthority(), assignment.getRole(), PermissionModelType.SPECIAL);
		}
	}

	/**
	 * The inherited permissions are taken in two ways - when the parent/library inheritance is enabled all permissions
	 * assignments are taken. However when the parent/library inheritance is disabled only the permission assignments
	 * for the manager role from the parent/library are taken.
	 *
	 * @param assignments
	 *            map where to store the inherited assignments
	 * @param entityPermission
	 *            EntityPermission for which to inherit assignments.
	 * @param modelType
	 *            model type to use when gathering the permissions
	 * @param managersOnly
	 *            if true, only the manager assignments will be returned.
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
			ResourceRole inheritedResourceRоle = inheritedAssignment.getValue();

			if (!managersOnly || inheritedResourceRоle.getRoleAssignments().isManager()) {
				addAssignment(assignments, authorityId, inheritedResourceRоle.getRoleAssignments().getActive(),
						modelType);
			}
		}
	}

	private void addAssignment(Map<String, ResourceRole> assignments, String authorityId, String role,
			PermissionModelType modelType) {
		ResourceRole resourceRole = assignments.get(authorityId);
		if (resourceRole == null) {
			resourceRole = new ResourceRole();
			resourceRole.setRoleAssignments(new RoleAssignments(roleService.getManagerRole().getIdentifier()));
			resourceRole.setAuthorityId(authorityId);
			assignments.put(authorityId, resourceRole);
		}

		resourceRole.getRoleAssignments().addAssignment(role, modelType);
	}

	private static boolean isValidReference(InstanceReference reference) {
		if (reference == null) {
			LOGGER.error("Requested permission retrieval for null reference");
			return false;
		}

		if (reference.getIdentifier() == null) {
			LOGGER.error("Requested permission retrieval for reference with null id");
			return false;
		}

		return true;
	}

	@Override
	public Map<InstanceReference, Map<String, ResourceRole>> getPermissionAssignmentsForInstances(
			Collection<InstanceReference> references, Boolean includeParent, Boolean includeLibrary) {
		if (isEmpty(references)) {
			return emptyMap();
		}

		Map<String, EntityPermission> entitiesPermissions = loadEntitiesPermissions(references);
		Map<InstanceReference, Map<String, ResourceRole>> results = new HashMap<>(entitiesPermissions.size());
		for (InstanceReference reference : references) {
			EntityPermission entityPermission = entitiesPermissions.get(reference.getIdentifier());
			Map<String, ResourceRole> assigments = retrieveAssigments(entityPermission, includeParent, includeLibrary);
			results.put(reference, assigments);
		}

		return results;
	}

	@Override
	public ResourceRole getPermissionAssignment(InstanceReference reference, Serializable authorityId) {
		String authority = (String) authorityId;
		if (!isValidReference(reference) || StringUtils.isEmpty(authority)) {
			return null;
		}

		Map<String, ResourceRole> permissionAssignments = getPermissionAssignments(reference);
		return getResourceRole(authority, permissionAssignments);
	}

	private ResourceRole getResourceRole(String authority, Map<String, ResourceRole> permissionAssignments) {
		if (isEmpty(permissionAssignments)) {
			return null;
		}

		ResourceRole role = permissionAssignments.get(authority);
		if (role != null) {
			return role;
		}

		List<String> containerAuthorities = getContainingResources(authority);
		List<ResourceRole> roles = permissionAssignments
				.entrySet()
					.stream()
					.filter(e -> containerAuthorities.contains(e.getKey()))
					.map(Entry::getValue)
					.collect(Collectors.toList());

		role = findMaxRole(roles);
		if (role == null) {
			role = permissionAssignments.get(getAllOtherId());
		}

		return role;
	}

	@Override
	public Map<InstanceReference, ResourceRole> getPermissionAssignment(Collection<InstanceReference> references,
			Serializable authorityId) {
		String authority = Objects.toString(authorityId, null);
		if (isEmpty(references) || StringUtils.isBlank(authority)) {
			return emptyMap();
		}

		Map<InstanceReference, Map<String, ResourceRole>> assignments = getPermissionAssignmentsForInstances(references,
				null, null);
		return references.stream().collect(Collectors.toMap(Function.identity(),
				reference -> getResourceRole(authority, assignments.get(reference))));
	}

	private List<String> getContainingResources(String authorityId) {
		return resourceService
				.getContainingResources(authorityId)
					.stream()
					.map(authority -> authority.getId().toString())
					.collect(Collectors.toList());
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
	public void setPermissions(InstanceReference instance, Collection<PermissionsChange> changes) {
		if (!isValidReference(instance)) {
			throw new IllegalArgumentException("No reference is provided");
		}

		if (changes == null || changes.isEmpty()) {
			return;
		}

		LOGGER.info("Setting permissions for {}", instance.getIdentifier());

		String instanceId = instance.getIdentifier();
		EntityPermission entityPermission = entityPermissionDao
				.loadWithAssignments(instanceId)
					.orElseGet(() -> new EntityPermission().setTargetId(instanceId));

		Set<PermissionAssignmentChange> changeset = new HashSet<>();

		handleRemovedAssignments(entityPermission, changes, changeset);

		handleAddedAssignments(entityPermission, changes, changeset);

		handleParentInheritanceChange(entityPermission, changes, changeset);

		handleLibraryInheritanceChange(entityPermission, changes, changeset);

		handleLibraryIndicatorChange(entityPermission, changes);

		ensureAtLeastOneManager(instance, entityPermission);

		entityPermissionDao.save(entityPermission);

		eventService.fire(new PermissionModelChangedEvent(instance, changeset));
	}

	/**
	 * Ensures that at least one manager is available for the particular instance. If there is a parent, its managers
	 * are inherited. If there is no parent, there should be an assignment for at least one manager.
	 *
	 * @param reference
	 *            instance to check.
	 * @param entityPermission
	 *            entityPermission object.
	 */
	private void ensureAtLeastOneManager(InstanceReference reference, EntityPermission entityPermission) {
		if (checkIsRoot(reference) && !entityPermission.isLibrary()) {
			boolean hasManager = false;
			for (AuthorityRoleAssignment assignment : entityPermission.getAssignments()) {
				if (isManagerRole(assignment.getRole())) {
					hasManager = true;
					break;
				}
			}

			if (!hasManager) {
				throw new EmfRuntimeException("Missing at least one manager on root level!");
			}
		}
	}

	private void handleParentInheritanceChange(EntityPermission entityPermission, Collection<PermissionsChange> changes,
			Set<PermissionAssignmentChange> changeset) {
		boolean changed = false;
		String oldParent = getTargetId(entityPermission.getParent());
		String newParent = oldParent;

		Optional<ParentChange> parentChange = changes
				.stream()
					.filter(change -> change instanceof ParentChange)
					.map(change -> (ParentChange) change)
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

		Optional<InheritFromParentChange> inheritFromParentChange = changes
				.stream()
					.filter(change -> change instanceof InheritFromParentChange)
					.map(change -> (InheritFromParentChange) change)
					.filter(change -> entityPermission.getInheritFromParent() != change.getValue())
					.findFirst();

		if (inheritFromParentChange.isPresent()) {
			entityPermission.setInheritFromParent(inheritFromParentChange.get().getValue());
			changed = true;
		}

		if (changed) {
			changeset.add(
					new PermissionInheritanceChange(oldParent, newParent, !entityPermission.getInheritFromParent()));
		}
	}

	private void handleLibraryInheritanceChange(EntityPermission entityPermission,
			Collection<PermissionsChange> changes, Set<PermissionAssignmentChange> changeset) {
		boolean changed = false;
		String oldLibrary = getTargetId(entityPermission.getLibrary());
		String newLibrary = oldLibrary;

		Optional<LibraryChange> libraryChange = changes
				.stream()
					.filter(change -> change instanceof LibraryChange)
					.map(change -> (LibraryChange) change)
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

		Optional<InheritFromLibraryChange> inheritFromLibraryChange = changes
				.stream()
					.filter(change -> change instanceof InheritFromLibraryChange)
					.map(change -> (InheritFromLibraryChange) change)
					.filter(change -> entityPermission.getInheritFromLibrary() != change.getValue())
					.findFirst();

		if (inheritFromLibraryChange.isPresent()) {
			entityPermission.setInheritFromLibrary(inheritFromLibraryChange.get().getValue());
			changed = true;
		}

		if (changed) {
			changeset.add(
					new PermissionInheritanceChange(oldLibrary, newLibrary, !entityPermission.getInheritFromLibrary()));
		}
	}

	private static String getTargetId(EntityPermission entityPermission) {
		if (entityPermission != null) {
			return entityPermission.getTargetId();
		}
		return null;
	}

	private static void handleAddedAssignments(EntityPermission entityPermission, Collection<PermissionsChange> changes,
			Set<PermissionAssignmentChange> changeset) {
		changes
				.stream()
					.filter(change -> change instanceof AddRoleAssignmentChange)
					.map(change -> (AddRoleAssignmentChange) change)
					.forEach(change -> {
						AuthorityRoleAssignment existingAssignment = findAssignment(entityPermission,
								change.getAuthority());

						if (existingAssignment == null) {
							changeset
									.add(new PermissionAssignmentChange(change.getAuthority(), null, change.getRole()));
							entityPermission.getAssignments().add(
									new AuthorityRoleAssignment(change.getAuthority(), change.getRole()));

						} else if (!existingAssignment.getRole().equals(change.getRole())) {
							changeset.add(new PermissionAssignmentChange(change.getAuthority(),
									existingAssignment.getRole(), change.getRole()));
							existingAssignment.setRole(change.getRole());
						}
					});
	}

	private static void handleRemovedAssignments(EntityPermission entityPermission,
			Collection<PermissionsChange> changes, Set<PermissionAssignmentChange> changeset) {
		changes
				.stream()
					.filter(change -> change instanceof RemoveRoleAssignmentChange)
					.map(change -> (RemoveRoleAssignmentChange) change)
					.forEach(change -> {
						AuthorityRoleAssignment assignmentToRemove = findAssignment(entityPermission,
								change.getAuthority());

						if (assignmentToRemove != null && assignmentToRemove.getRole().equals(change.getRole())) {
							entityPermission.getAssignments().remove(assignmentToRemove);
							changeset
									.add(new PermissionAssignmentChange(change.getAuthority(), change.getRole(), null));
						}
					});
	}

	private static void handleLibraryIndicatorChange(EntityPermission entityPermission,
			Collection<PermissionsChange> changes) {
		changes
				.stream()
					.filter(change -> change instanceof SetLibraryIndicatorChange)
					.map(change -> (SetLibraryIndicatorChange) change)
					.forEach(change -> {
						entityPermission.setIsLibrary(change.getValue());
					});
	}

	private static AuthorityRoleAssignment findAssignment(EntityPermission entityPermission, String authority) {
		for (AuthorityRoleAssignment assignment : entityPermission.getAssignments()) {
			if (assignment.getAuthority().equals(authority)) {
				return assignment;
			}
		}
		return null;
	}

	private boolean isManagerRole(String role) {
		return roleService.isManagerRole(role);
	}

	private String getAllOtherId() {
		return resourceService.getAllOtherUsers().getId().toString();
	}

	@Override
	public PermissionModelType getPermissionModel(InstanceReference reference) {
		if (reference == null) {
			return PermissionModelType.UNDEFINED;
		}

		Optional<EntityPermission> entityPermission = entityPermissionDao
				.loadWithAssignments(reference.getIdentifier());

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
		LOGGER.info("Restoring permissions from parent for descendants of {}", reference.getIdentifier());
		String instanceId = reference.getIdentifier();
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
					.loadWithAssignments(currentReference.getIdentifier())
						.orElseGet(EntityPermission::new);

			Set<PermissionAssignmentChange> changeset = new LinkedHashSet<>();
			for (AuthorityRoleAssignment assignment : entityPermission.getAssignments()) {
				changeset.add(new PermissionAssignmentChange(assignment.getAuthority(), assignment.getRole(), null));
			}

			entityPermissionDao.deleteAssignments(entityPermission.getId());

			boolean libraryFlag = currentReference.getType().hasTrait(INHERIT_PARENT_PERMISSIONS);
			if (entityPermission.getInheritFromParent() != libraryFlag) {
				entityPermission.setInheritFromParent(libraryFlag);
				String parentId = entityPermission.getParent().getTargetId();
				changeset.add(
						new PermissionInheritanceChange(parentId, parentId, !entityPermission.getInheritFromParent()));
				entityPermissionDao.save(entityPermission);
			}

			if (!changeset.isEmpty()) {
				LOGGER.info("Restoring permissions from parent for {}", currentReference.getIdentifier());

				eventService.fire(new PermissionModelChangedEvent(currentReference, changeset));
				eventService.fire(new PermissionsRestored(currentReference.toInstance()));
			}
		}
	}

	@Override
	public boolean checkIsRoot(InstanceReference reference) {
		if (reference == null) {
			return false;
		}

		return hierarchyResolver.isInstanceRoot(reference.getIdentifier());
	}

}
