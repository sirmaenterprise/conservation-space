package com.sirma.itt.seip.permissions.role;

import static com.sirma.itt.seip.collections.CollectionUtils.createHashMap;
import static com.sirma.itt.seip.collections.CollectionUtils.emptyList;
import static com.sirma.itt.seip.collections.CollectionUtils.emptyMap;
import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.db.customtype.BooleanCustomType;
import com.sirma.itt.seip.permissions.EntityPermissions;

/**
 * DAO for {@link EntityPermission} entities.
 *
 * @author Adrian Mitev
 */
public class EntityPermissionDao {

	@Inject
	private DbDao dbDao;

	/**
	 * Loads {@link EntityPermission} without fetching any associations.
	 *
	 * @param targetId
	 *            id of the instance which {@link EntityPermission} to load.
	 * @return loaded entity.
	 */
	public Optional<EntityPermission> load(String targetId) {
		if (StringUtils.isBlank(targetId)) {
			return Optional.empty();
		}

		List<EntityPermission> result = dbDao.fetchWithNamed(EntityPermission.QUERY_LOAD_BY_TARGET_ID_KEY,
				targetIdArgs(targetId));

		if (!result.isEmpty()) {
			return Optional.of(result.get(0));
		}

		return Optional.empty();
	}

	/**
	 * Loads {@link EntityPermission} including its authority role assignments.
	 *
	 * @param targetId
	 *            id of the instance which {@link EntityPermission} to load.
	 * @return loaded entity.
	 */
	public Optional<EntityPermission> loadWithAssignments(String targetId) {
		if (StringUtils.isBlank(targetId)) {
			return Optional.empty();
		}

		List<EntityPermission> result = dbDao.fetchWithNamed(
				EntityPermission.QUERY_LOAD_BY_TARGET_ID_WITH_ROLE_ASSIGNMENTS_KEY, targetIdArgs(targetId));

		if (!result.isEmpty()) {
			return Optional.of(result.get(0));
		}

		return Optional.empty();
	}

	/**
	 * Fetches the {@link EntityPermission}'s and their {@link AuthorityRoleAssignment}'s for an instance and all its
	 * ancestors (the entire hierarchy).
	 *
	 * @param targetId
	 *            instance id.
	 * @return fetched hierarchy.
	 */
	public Optional<EntityPermission> fetchHierarchyWithAssignments(String targetId) {
		if (StringUtils.isBlank(targetId)) {
			return Optional.empty();
		}

		List<Serializable> targetIds = Collections.singletonList(targetId);
		Map<String, EntityPermission> entityPermissions = fetchPermissions(targetIds);

		// no EntityPermission for the provided target id
		if (entityPermissions.isEmpty()) {
			return Optional.empty();
		}

		addAssigments(entityPermissions, targetIds);
		EntityPermission entityPermission = entityPermissions.get(targetId);
		removeHierarchyLoops(entityPermission);
		return Optional.of(entityPermission);
	}

	/**
	 * Fetches the {@link EntityPermission}'s and their {@link AuthorityRoleAssignment}'s for instances and all of their
	 * ancestors (the entire hierarchy).
	 *
	 * @param targetIds
	 *            the ids of the instances which permissions should be fetch
	 * @return {@link Map} where the keys are the ids of the instances and the values are their permissions
	 */
	public Map<String, EntityPermission> fetchHierarchyWithAssignmentsForInstances(Collection<Serializable> targetIds) {
		if (isEmpty(targetIds)) {
			return emptyMap();
		}

		Map<String, EntityPermission> entitiesPermissions = fetchPermissions(targetIds);
		if (entitiesPermissions.isEmpty()) {
			return emptyMap();
		}

		addAssigments(entitiesPermissions, targetIds);
		entitiesPermissions.values().forEach(this::removeHierarchyLoops);
		return entitiesPermissions;
	}

	/**
	 * Load the entity permissions data for the given instance
	 *
	 * @param targetId
	 *            id of the instance that are at the bottom of the hierarchy.
	 * @return all entity permissions for the given instance.
	 */
	public Optional<EntityPermissions> fetchPermissions(Serializable targetId) {

		Set<Serializable> targetIds = Collections.singleton(targetId);
		Map<String, EntityPermission> entities = fetchPermissions(targetIds);

		EntityPermission entityPermission = entities.get(targetId);
		if (entityPermission != null) {
			EntityPermissions entityPermissions = new EntityPermissions(entityPermission.getTargetId(),
					entityPermission.getParentId(),
					entityPermission.getLibraryId(), entityPermission.getInheritFromParent(), entityPermission
					.getInheritFromLibrary(), entityPermission.isLibrary());

			Map<String, List<AuthorityRoleAssignment>> assignments = fetchAssignments(targetIds);
			assignments.getOrDefault(targetId, Collections.emptyList()).forEach(assignment -> entityPermissions
					.addAssignment(assignment.getAuthority(), assignment.getRole()));

			return Optional.of(entityPermissions);
		}

		return Optional.empty();
	}

	/**
	 * Hierarchically load the assignments for a given instances including the assignments of its library, its parents
	 * (loaded by parent associations) and their libraries.
	 *
	 * @param targetIds
	 *            ids of the instances that are at the bottom of the hierarchy.
	 * @return the assigned permissions paired by authority id and role id, mapped by the id of the instance they are
	 *         assigned to.
	 */
	private Map<String, EntityPermission> fetchPermissions(Collection<Serializable> targetIds) {
		List<Object[]> permissions = dbDao.fetchWithNamed(
				EntityPermission.QUERY_LOAD_ENTITY_PERMISSIONS_FOR_HIERARCHY_KEY, targetIdsArgs(targetIds));
		if (permissions.isEmpty()) {
			return emptyMap();
		}

		Map<String, EntityPermission> entities = buildEntitiesMap(permissions);

		// link the hierarchy
		for (Object[] columns : permissions) {
			String currentTargetId = (String) columns[0];

			EntityPermission permission = entities.get(currentTargetId);
			setPermissionLinkIfExists(permission::setParent, entities, (String) columns[1]);
			setPermissionLinkIfExists(permission::setLibrary, entities, (String) columns[3]);
		}

		return entities;
	}

	private static List<Pair<String, Object>> targetIdsArgs(Collection<Serializable> targetIds) {
		List<Pair<String, Object>> params = new ArrayList<>(1);
		params.add(new Pair<>("targetIds", targetIds));
		return params;
	}

	private static Map<String, EntityPermission> buildEntitiesMap(List<Object[]> assignments) {
		Map<String, EntityPermission> entities = createHashMap(assignments.size());
		for (Object[] columns : assignments) {
			String currentTargetId = (String) columns[0];

			EntityPermission permission = new EntityPermission();
			permission.setTargetId(currentTargetId);
			permission.setInheritFromParent(toBoolean((short) columns[2]));
			permission.setInheritFromLibrary(toBoolean((short) columns[4]));
			permission.setIsLibrary(toBoolean((short) columns[5]));

			entities.put(currentTargetId, permission);
		}

		return entities;
	}

	/**
	 * Converts integer to boolean based on 1 == true. For more info see {@link BooleanCustomType}.
	 *
	 * @param number
	 *            the number to convert.
	 * @return converted number.
	 */
	private static boolean toBoolean(short number) {
		return number == 1;
	}

	private static void setPermissionLinkIfExists(Consumer<EntityPermission> entityConsumer,
			Map<String, EntityPermission> entities, String entityId) {
		if (StringUtils.isNotBlank(entityId)) {
			entityConsumer.accept(entities.get(entityId));
		}
	}

	private void addAssigments(Map<String, EntityPermission> entityPermissions, Collection<Serializable> targetIds) {
		Map<String, List<AuthorityRoleAssignment>> assignments = fetchAssignments(targetIds);
		for (Entry<String, EntityPermission> entry : entityPermissions.entrySet()) {
			String instanceId = entry.getKey();
			if (assignments.containsKey(instanceId)) {
				entry.getValue().getAssignments().addAll(assignments.get(instanceId));
			}
		}
	}

	/**
	 * Hierarchically load the assignments for a given instance including the assignments of its library, its parents
	 * (loaded by parent associations) and their libraries.
	 *
	 * @param targetIds
	 *            ids of the instances that is at the bottom of the hierarchy.
	 * @return {@link Map} containing the ids of the instances and their {@link AuthorityRoleAssignment}s
	 */
	private Map<String, List<AuthorityRoleAssignment>> fetchAssignments(Collection<Serializable> targetIds) {
		List<Object[]> assignments = dbDao.fetchWithNamed(EntityPermission.QUERY_LOAD_ASSIGNMENTS_FOR_HIERARCHY_KEY,
				targetIdsArgs(targetIds));

		Map<String, List<AuthorityRoleAssignment>> result = new HashMap<>();

		for (Object[] columns : assignments) {
			List<AuthorityRoleAssignment> entityAssignments = result.computeIfAbsent((String) columns[0],
					param -> new ArrayList<>());
			entityAssignments.add(new AuthorityRoleAssignment((String) columns[1], (String) columns[2]));
		}

		return result;
	}

	/**
	 * Hierarchy loops could appear, when 'move' operation is executed. Imagine that you have hierarchy with objects:
	 * <b>{@literal project -> case -> document -> picture}</b>, when the user moves for example the picture object
	 * between the project and the case it causes loop in the permissions, because it tries to evaluate the permissions
	 * for the case/picture, but they are at the same time children and parents of each other.
	 */
	private void removeHierarchyLoops(EntityPermission entityPermission) {
		Set<String> visited = new HashSet<>();

		for (EntityPermission current = entityPermission; current.getParent() != null; current = current.getParent()) {
			visited.add(current.getTargetId());

			if (visited.contains(current.getParent().getTargetId())) {
				current.setParent(null);
				break;
			}
		}
	}

	/**
	 * Fetches the targetId id of an {@link EntityPermission} and all its descendants.
	 *
	 * @param targetId
	 *            id of the instance that is on top of the hierarchy.
	 * @return fetched targetIds.
	 */
	public Collection<String> getDescendants(String targetId) {
		if (StringUtils.isBlank(targetId)) {
			return emptyList();
		}

		return dbDao.fetchWithNamed(EntityPermission.QUERY_GET_DESCENDANTS_KEY, targetIdArgs(targetId));
	}

	private static List<Pair<String, Object>> targetIdArgs(String targetId) {
		List<Pair<String, Object>> params = new ArrayList<>(1);
		params.add(new Pair<>("targetId", targetId));
		return params;
	}

	/**
	 * Saves an {@link EntityPermission} with its role assignments.
	 *
	 * @param entityPermission
	 */
	public void save(EntityPermission entityPermission) {
		dbDao.saveOrUpdate(entityPermission);
	}

	/**
	 * Delete all assignments for a give {@link EntityPermission} id.
	 *
	 * @param entityPermissionId
	 *            id of the {@link EntityPermission}.
	 */
	public void deleteAssignments(Long entityPermissionId) {
		if (entityPermissionId == null) {
			return;
		}
		List<Pair<String, Object>> params = new ArrayList<>(1);
		params.add(new Pair<>("permissionId", entityPermissionId));
		dbDao.executeUpdate(EntityPermission.QUERY_DELETE_ASSIGNMENTS_KEY, params);
	}

}
