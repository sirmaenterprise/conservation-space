package com.sirma.itt.seip.permissions.role;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;

import com.esotericsoftware.minlog.Log;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.concurrent.FragmentedWork;
import com.sirma.itt.seip.db.DbDao;

/**
 * Dao for resource role queries.
 *
 * @author BBonev
 */
class ResourceRolesDao {
	private static final String SOURCE_ID = "sourceId";
	private static final int QUERY_RESULT_LIMIT = 25000;

	@Inject
	private DbDao dbDao;

	/**
	 * Find roles for instance id and type
	 *
	 * @param instanceId
	 *            the instance id
	 * @return the list of roles
	 */
	List<ResourceRoleEntity> findRolesForInstance(String instanceId) {
		List<Pair<String, Object>> args = new ArrayList<>(1);
		args.add(new Pair<String, Object>(SOURCE_ID, instanceId));
		return dbDao.fetchWithNamed(ResourceRoleEntity.QUERY_RESOURCE_ROLES_BY_TARGET_ID_KEY, args);
	}

	/**
	 * Find role entities by db id.
	 *
	 * @param ids
	 *            the ids to fetch
	 * @return the list of fetched from db entities
	 */
	Collection<ResourceRoleEntity> findRoleEntitiesByDbId(Set<Long> ids) {
		return FragmentedWork.doWorkWithResult(ids, QUERY_RESULT_LIMIT, idsPart -> loadEntities(idsPart));
	}

	private List<ResourceRoleEntity> loadEntities(Collection<Long> ids) {
		return dbDao.fetchWithNamed(ResourceRoleEntity.QUERY_RESOURCE_ROLES_BY_IDS_KEY,
				Arrays.asList(new Pair<String, Object>("ids", ids)));
	}

	/**
	 * Find role entities target instances by path. Query is with like - any % could be appended to the path argument
	 *
	 * @param path
	 *            the path to find like
	 * @return the list of fetched from db entities
	 */
	Set<String> findRoleEntitiesTargetsByPath(String path) {
		Set<String> result = new LinkedHashSet<>(1024);
		int skip = 0;
		List<Pair<String, Object>> args = Arrays.asList(new Pair<String, Object>("path", path));
		while (true) {
			List<String> roles = dbDao.fetchWithNamed(ResourceRoleEntity.QUERY_RESOURCE_ROLES_BY_PATH_KEY, args, skip,
					QUERY_RESULT_LIMIT);
			result.addAll(roles);
			// if the result is empty or the results are less then the limit so no need to check for more
			if (roles.size() < QUERY_RESULT_LIMIT) {
				break;
			}
			skip += QUERY_RESULT_LIMIT;
		}
		return result;
	}

	ResourceRoleEntity persist(ResourceRoleEntity resourceRoleEntity) {
		return dbDao.saveOrUpdate(resourceRoleEntity);
	}

	/**
	 * Delete permissions internal.
	 *
	 * @param ids
	 *            the ids to remove
	 */
	void deletePermissionsInternal(Collection<Long> ids) {
		if (ids != null && ids.removeIf(Objects::isNull)) {
			Log.warn("There is a ResourceRoleEntity entity without id");
		}

		if (ids != null && !ids.isEmpty()) {
			List<Pair<String, Object>> args = new ArrayList<>(1);
			args.add(new Pair<String, Object>("ids", ids));

			dbDao.executeUpdate(ResourceRoleEntity.DELETE_RESOURCE_ROLES_BY_IDS_KEY, args);
			args = null;
		}
	}
}
