package com.sirma.itt.seip.permissions.sync.batch;

import static com.sirma.itt.seip.collections.CollectionUtils.addNonNullValue;
import static com.sirma.itt.seip.collections.CollectionUtils.addValueToSetMap;
import static com.sirma.itt.seip.collections.CollectionUtils.createHashMap;
import static com.sirma.itt.seip.collections.CollectionUtils.createHashSet;
import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.batch.api.chunk.ItemProcessor;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.rdf4j.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.instance.CommonInstance;
import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.seip.permissions.EntityPermissions;
import com.sirma.itt.seip.permissions.PermissionService;
import com.sirma.itt.seip.search.ResultItem;
import com.sirma.itt.seip.search.ResultItemTransformer;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.util.EqualsHelper;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Batch step processor that calculates permission changes in the relational to semantic databases and produces a
 * {@link PermissionsDiff} instance if some changes are found.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 09/06/2017
 */
@Named
public class CalculateInstancePermissionProcessor implements ItemProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String PERMISSIONS_SYNC_QUERY = ResourceLoadUtil.loadResource(
			CalculateInstancePermissionProcessor.class,"query-permissions-for-sync.sparql");

	@Inject
	private PermissionSyncUtil syncUtil;
	@Inject
	private PermissionService permissionService;
	@Inject
	private NamespaceRegistryService registryService;
	@Inject
	private SearchService searchService;

	@Override
	public Object processItem(Object item) throws Exception {
		InstanceReference reference = (InstanceReference) item;
		String instanceId = reference.getId();

		// first load the current local permissions that will be used for source of truth
		EntityPermissions currentPermissions = getLocalPermissions(reference);
		// load permissions that need to be compared to our source of truth
		SemanticPermissions semanticPermissions = getSemanticPermissions(instanceId);

		if (isEmptyPermissions(currentPermissions) && isEmptyPermissions(semanticPermissions)) {
			// nothing enabled and no permissions are set probably unknown instance
			// it will be dangerous to just delete everything in the target DB
			LOGGER.warn("Skipping instance {} as no permission information is found in rdb for it", instanceId);
			return null;
		}
		LOGGER.trace("Processing instance {} ", instanceId);

		PermissionsDiff permissionsDiff = new PermissionsDiff(instanceId);

		performAssignmentsDiff(currentPermissions, semanticPermissions, permissionsDiff);
		performInheritanceDiff(currentPermissions, semanticPermissions, permissionsDiff);

		setCurrentInstanceRoles(semanticPermissions, permissionsDiff);

		if (permissionsDiff.hasChanges()) {
			return permissionsDiff;
		}
		return null;
	}

	private static void setCurrentInstanceRoles(SemanticPermissions semanticPermissions,
			PermissionsDiff permissionsDiff) {
		permissionsDiff.setInstanceRoles(semanticPermissions.instanceRoles);
	}

	private static void performInheritanceDiff(EntityPermissions currentPermissions, SemanticPermissions
			semanticPermissions, PermissionsDiff permissionsDiff) {
		// SEE CMF-25913:
		// when parent is the same as the library in the semantic both inheritance flags are enabled
		// and if one of the inheritance flags are disabled in the semantic this leads always to change in the semantic
		// that removes both inheritance that is not valid
		boolean parentSameAsLibrary = nullSafeEquals(currentPermissions.getParent(), currentPermissions.getLibrary());
		boolean disabledParentOrLibraryPermissions = !(currentPermissions.isInheritFromLibrary()
				&& currentPermissions.isInheritFromParent());
		boolean enabledSemanticParentAndLibraryPermissions = semanticPermissions.isInheritFromLibrary()
				&& semanticPermissions.isInheritFromParent();
		if (parentSameAsLibrary
				&& disabledParentOrLibraryPermissions
				&& enabledSemanticParentAndLibraryPermissions) {
			return;
		}

		Set<String> libraryPermissionsToRemove = doWeNeedInheritanceRemove(semanticPermissions.libraries,
				currentPermissions.getLibrary(), currentPermissions.isInheritFromLibrary(),
				semanticPermissions.isInheritFromLibrary());
		String libraryPermissionsToAdd = doWeNeedInheritanceAdd(semanticPermissions.libraries, currentPermissions
				.getLibrary(), currentPermissions.isInheritFromLibrary(), semanticPermissions.isInheritFromLibrary());
		permissionsDiff.libraryInheritanceChanged(libraryPermissionsToRemove, libraryPermissionsToAdd);

		Set<String> parentPermissionsToRemove = doWeNeedInheritanceRemove(semanticPermissions.parents,
				currentPermissions.getParent(), currentPermissions.isInheritFromParent(),
				semanticPermissions.isInheritFromParent());
		String parentPermissionsToAdd = doWeNeedInheritanceAdd(semanticPermissions.parents, currentPermissions
				.getParent(), currentPermissions.isInheritFromParent(), semanticPermissions.isInheritFromParent());
		permissionsDiff.parentInheritanceChanged(parentPermissionsToRemove, parentPermissionsToAdd);
	}

	private void performAssignmentsDiff(EntityPermissions currentPermissions,
			SemanticPermissions semanticPermissions, PermissionsDiff permissionsDiff) {

		Map<String, Set<String>> localRoles = toAssignmentsMapping(currentPermissions,
				assignment -> syncUtil.getRoleTypesMapping().get(assignment.getRole()));

		Map<String, Set<String>> semanticRoles = toAssignmentsMapping(semanticPermissions,
				EntityPermissions.Assignment::getRole);

		Map<String, EqualsHelper.MapValueComparison> diff = EqualsHelper.getMapComparison(localRoles,
				semanticRoles);
		diff.forEach((authority, diffType) -> {
			switch (diffType) {
				case LEFT_ONLY:
					localRoles.get(authority).forEach(
							extraRole -> permissionsDiff.addRoleChange(authority, extraRole, null));
					break;
				case RIGHT_ONLY:
					semanticRoles.get(authority).forEach(missingRole -> permissionsDiff.addRoleChange(authority, null,
							missingRole));
					break;
				case NOT_EQUAL:
					onNotEqual(permissionsDiff, localRoles, semanticRoles, authority);
					break;
				default:
					break;
			}
		});
	}

	private static void onNotEqual(PermissionsDiff permissionsDiff, Map<String, Set<String>> localRoles,
			Map<String, Set<String>> semanticRoles, String authority) {
		Set<String> left = localRoles.get(authority);
		Set<String> right = semanticRoles.get(authority);
		left.stream().filter(item -> !right.contains(item))
				.forEach(l -> permissionsDiff.addRoleChange(authority, l, null));
		right.stream().filter(item -> !left.contains(item))
				.forEach(r -> permissionsDiff.addRoleChange(authority, null, r));
	}

	private static Map<String, Set<String>> toAssignmentsMapping(EntityPermissions currentPermissions,
			Function<EntityPermissions.Assignment, String> assignmentToInstanceRoleId) {
		return currentPermissions.getAssignments()
				.filter(Objects::nonNull)
				.reduce(new HashMap<>(),
						(Map<String, Set<String>> map, EntityPermissions.Assignment a) -> {
							addValueToSetMap(map, a.getAuthority(), assignmentToInstanceRoleId.apply(a));
							return map;
						}, (map1, map2) -> {
							map2.forEach((k, set) -> set.forEach(v -> addValueToSetMap(map1, k, v)));
							return map1;
						});
	}

	/*
	Process inheritance changes. Returns only item that should be added as inheritance only. Such cases are
	  - the inheritance flag
	  - and the `should be` is non null
	  - and it's not currently set
	 */
	private static String doWeNeedInheritanceAdd(Set<String> current, String shouldBe, boolean shouldBeEnabled, boolean isCurrentlyEnabled) {
		if (shouldBe == null) {
			// if the should be part is not present then we does not have inheritance event if it's enabled
			return null;
		}
		if (shouldBeEnabled && (!current.contains(shouldBe) || !isCurrentlyEnabled)) {
			// the current is not enabled
			// and the shouldBy is not set so return it
			return shouldBe;
		}
		// no current inheritance OR
		// one of the current inherited is the one that should be, nothing to do
		return null;
	}

	private static Set<String> doWeNeedInheritanceRemove(Set<String> current, String shouldBe, boolean
			shouldBeEnabled, boolean isCurrentlyEnabled) {
		if (shouldBeEnabled) {
			if (shouldBe != null) {
				if (current.contains(shouldBe)) {
					Set<String> tmp = new HashSet<>(current);
					tmp.remove(shouldBe);
					return tmp;
				}
			} else if (!isCurrentlyEnabled) {
				// should be enabled, but there is nothing set
				// the current is not enabled so we have nothing to do with the current state
				// because in the semantic we will receive the parent id, because its just there
				// but we cannot remove it as it will not have effect
				return Collections.emptySet();
			}
		} else if (!isCurrentlyEnabled) {
			return Collections.emptySet();
		}
		return current;
	}

	private static boolean isEmptyPermissions(EntityPermissions permissions) {
		boolean noLibraryPermissions = permissions.getLibrary() == null && !permissions.isInheritFromLibrary();
		boolean noParentPermissions = permissions.getParent() == null && !permissions.isInheritFromParent();
		return noLibraryPermissions && noParentPermissions && permissions.getAssignments().count() == 0L;
	}

	private EntityPermissions getLocalPermissions(InstanceReference reference) {
		return permissionService.getPermissionsInfo(reference)
				.orElseGet(() -> new EntityPermissions(reference.getId()));
	}

	private SemanticPermissions getSemanticPermissions(String instanceId) {
		IRI instanceURI = registryService.buildUri(instanceId);
		SearchArguments<CommonInstance> rolesFilter = syncUtil.prepareSearchArguments(PERMISSIONS_SYNC_QUERY, false);
		rolesFilter.getArguments().put("entity", instanceURI);
		try (Stream<ResultItem> stream = searchService.stream(rolesFilter, ResultItemTransformer.asIs())) {
			return stream.map(item -> {
				String authority = item.getString("assignedTo");
				String roleType = item.getString("roleType");
				String roleId = item.getString("role");
				boolean isLibrary = item.getBooleanOrFalse("isLibrary");
				String library = item.getString("library");
				String parent = item.getString("parent");
				if (parent == null) {
					parent = item.getString("wrongInstance");
				}
				boolean inheritFromParent = item.getBooleanOrFalse("inheritFromParent");
				boolean inheritFromLibrary = item.getBooleanOrFalse("inheritFromLibrary");
				SemanticPermissions permissions = new SemanticPermissions(instanceId, parent, library,
						inheritFromParent, inheritFromLibrary, isLibrary);
				if (authority != null && roleType != null && roleId != null) {
					permissions.addAssignment(authority, roleType, roleId);
				}
				return permissions;
			}).reduce(new SemanticPermissions(instanceId), SemanticPermissions::merge);
		}
	}

	private static class SemanticPermissions extends EntityPermissions {
		Set<String> parents = createHashSet(2);
		Set<String> libraries = createHashSet(2);
		Map<String, String> instanceRoles = createHashMap(3); //read,write,manager

		SemanticPermissions(String entityId) {
			super(entityId);
		}

		SemanticPermissions(String entityId, String parent, String library, boolean inheritFromParent,
				boolean inheritFromLibrary, boolean isLibrary) {
			super(entityId, parent, library, inheritFromParent, inheritFromLibrary, isLibrary);
		}

		void addAssignment(String authority, String roleType, String roleId) {
			super.addAssignment(authority, roleType);
			instanceRoles.put(roleType, roleId);
		}

		static SemanticPermissions merge(SemanticPermissions current, SemanticPermissions other) {
			String instanceId = current.getEntityId();
			boolean inheritFromLibrary = current.isInheritFromLibrary() || other.isInheritFromLibrary();
			boolean inheritFromParent = current.isInheritFromParent() || other.isInheritFromParent();
			boolean isLibrary = current.isLibrary() || other.isLibrary();

			SemanticPermissions permissions = new SemanticPermissions(instanceId, null, null, inheritFromParent,
					inheritFromLibrary, isLibrary);

			addNonNullValue(permissions.parents, current.getParent());
			addNonNullValue(permissions.parents, other.getParent());
			permissions.parents.addAll(current.parents);

			addNonNullValue(permissions.libraries, other.getLibrary());
			addNonNullValue(permissions.libraries, other.getLibrary());
			permissions.libraries.addAll(current.libraries);

			current.getAssignments().forEach(a -> permissions.addAssignment(a.getAuthority(), a.getRole()));
			other.getAssignments().forEach(a -> permissions.addAssignment(a.getAuthority(), a.getRole()));
			permissions.instanceRoles.putAll(current.instanceRoles);
			permissions.instanceRoles.putAll(other.instanceRoles);
			return permissions;
		}

	}
}
