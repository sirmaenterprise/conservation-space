package com.sirmaenterprise.sep.roles.persistence;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.db.DbDao;

/**
 * Entity dao for accessing the roles and actions persistence.
 *
 * @since 2017-03-24
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 */
@Singleton
class RoleActionsDao {

	@Inject
	private DbDao dbDao;

	@Inject
	private DatabaseIdManager idManager;

	Collection<RoleEntity> getRoles() {
		return dbDao.fetchWithNamed(RoleEntity.QUERY_ALL_ROLES_KEY, Collections.emptyList());
	}

	Collection<RoleEntity> getRoles(Collection<String> ids) {
		return dbDao.fetchWithNamed(RoleEntity.QUERY_ROLES_BY_IDS_KEY,
				Arrays.asList(new Pair<>("ids", ids)));
	}

	Collection<ActionEntity> getActions() {
		return dbDao.fetchWithNamed(ActionEntity.QUERY_ALL_ACTIONS_KEY, Collections.emptyList());
	}

	Collection<ActionEntity> getActions(Collection<String> ids) {
		return dbDao.fetchWithNamed(ActionEntity.QUERY_ACTIONS_BY_IDS_KEY, Arrays.asList(new Pair<>("ids", ids)));
	}

	Collection<RoleActionEntity> getRoleActions() {
		return dbDao.fetchWithNamed(RoleActionEntity.QUERY_ALL_ROLE_ACTIONS_KEY, Collections.emptyList());
	}

	@SuppressWarnings("unchecked")
	<E extends Entity<? extends Serializable>> E saveNew(E entity) {
		try {
			idManager.register(entity);
			return dbDao.saveOrUpdate(entity);
		} finally {
			idManager.unregister((Entity<Serializable>) entity);
		}
	}

	<E extends Entity<? extends Serializable>> E save(E entity) {
		return dbDao.saveOrUpdate(entity);
	}

	void deleteRoleActionMappings() {
		dbDao.executeUpdate(RoleActionEntity.DELETE_ALL_ROLE_ACTIONS_KEY, Collections.emptyList());
	}

}
