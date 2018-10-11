package com.sirma.itt.seip.configuration.db;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.CoreDb;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.db.exceptions.DatabaseException;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Dao class for accessing configuration entities
 *
 * @author BBonev
 */
class ConfigurationDao {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String TENANT_ID = "tenantId";
	private static final String ID = "id";
	private static final Pair<String, Object> SYSTEM_TENANT = new Pair<>(TENANT_ID, SecurityContext.SYSTEM_TENANT);

	@Inject
	@CoreDb
	private DbDao dbDao;

	/**
	 * Gets the all configuration entities regardless of the tenant.
	 *
	 * @return the all entities
	 */
	List<ConfigurationEntity> getAllEntities() {
		return dbDao.fetchWithNamed(ConfigurationEntity.QUERY_ALL_CONFIG_KEY, Collections.emptyList());
	}

	/**
	 * Gets only the system configurations.
	 *
	 * @return the system configurations
	 */
	List<ConfigurationEntity> getSystemConfigurations() {
		return dbDao.fetchWithNamed(ConfigurationEntity.QUERY_CONFIG_BY_TENANT_KEY,
				Collections.singletonList(SYSTEM_TENANT));
	}

	/**
	 * Gets the configurations for the particular tenant.
	 *
	 * @param currentTenantId
	 *            the current tenant id
	 * @return the configurations by tenant
	 */
	List<ConfigurationEntity> getConfigurationsByTenant(String currentTenantId) {
		return dbDao.fetchWithNamed(ConfigurationEntity.QUERY_CONFIG_BY_TENANT_KEY,
				Collections.singletonList(new Pair<>(TENANT_ID, currentTenantId)));
	}

	/**
	 * Gets the specified system configurations.
	 *
	 * @param keys
	 *            the keys of the configurations to fetch
	 * @return the system configuration entities
	 */
	List<ConfigurationEntity> getSystemConfigurations(Collection<String> keys) {
		if (keys.isEmpty()) {
			return Collections.emptyList();
		}
		return dbDao.fetchWithNamed(ConfigurationEntity.QUERY_CONFIG_BY_ID_TENANT_KEY,
				Arrays.asList(new Pair<>(ID, keys), SYSTEM_TENANT));
	}

	/**
	 * Gets the specified tenant configurations.
	 *
	 * @param keys
	 *            the keys to load
	 * @param tenantId
	 *            the tenant id to filter the results
	 * @return the tenant configurations
	 */
	List<ConfigurationEntity> getTenantConfigurations(Collection<String> keys, String tenantId) {
		if (keys.isEmpty()) {
			return Collections.emptyList();
		}
		return dbDao.fetchWithNamed(ConfigurationEntity.QUERY_CONFIG_BY_ID_TENANT_KEY,
				Arrays.asList(new Pair<>(ID, keys), new Pair<>(TENANT_ID, tenantId)));
	}

	/**
	 * Persist configuration entity
	 *
	 * @param entity
	 *            the entity
	 */
	void persist(ConfigurationEntity entity) {
		dbDao.saveOrUpdate(entity);
	}

	/**
	 * Delete configuration for the given tenant
	 *
	 * @param key
	 *            the key to delete
	 * @param tenantId
	 *            the tenant id
	 */
	void deleteConfiguration(String key, String tenantId) {
		try {
			dbDao.delete(ConfigurationEntity.class, new ConfigurationId(key, tenantId));
		} catch (DatabaseException e) {
			LOGGER.trace("No configurtion found for key={} in tenant={}", key, tenantId, e);
		}
	}
	
	/**
	 * Delete all the tenant's configurations.
	 * 
	 * @param tenantId
	 *            the tenant which configurations should be deleted
	 */
	void deleteTenantConfigurations(String tenantId) {
		dbDao.executeUpdate(ConfigurationEntity.DELETE_ALL_CONFIGS_FOR_TENANT_KEY,
				Arrays.asList(new Pair<>(TENANT_ID, tenantId)));
	}
}