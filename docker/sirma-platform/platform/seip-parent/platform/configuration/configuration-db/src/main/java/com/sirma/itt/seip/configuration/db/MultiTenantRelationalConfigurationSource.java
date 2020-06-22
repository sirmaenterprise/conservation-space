package com.sirma.itt.seip.configuration.db;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;
import javax.persistence.PersistenceException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.configuration.build.ConfigurationSource;
import com.sirma.itt.seip.db.CoreDb;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Provides access to configuration values located in the core relational database in respect to the current active
 * tenant.
 *
 * @author BBonev
 */
@Extension(target = ConfigurationSource.NAME, order = 100)
class MultiTenantRelationalConfigurationSource implements ConfigurationSource {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String TENANT_ID = "tenantId";
	private static final Pair<String, Object> SYSTEM_TENANT = new Pair<>("systemTenant", SecurityContext.SYSTEM_TENANT);
	private static final List<Pair<String, Object>> CONFIGURATION_ARGS = Arrays
			.asList(new Pair<>(TENANT_ID, "anyTenant"), SYSTEM_TENANT);

	@Inject
	@CoreDb
	private DbDao dbDao;

	@Override
	public String getConfigurationValue(String key) {
		return getConfigurationValues(key, SecurityContext.SYSTEM_TENANT);
	}

	@Override
	public String getConfigurationValue(String key, String tenantId) {
		return getConfigurationValues(key, tenantId);
	}

	private String getConfigurationValues(String key, String tenantId) {
		try {
			List<String> values = dbDao.fetchWithNamed(ConfigurationEntity.QUERY_CONFIG_VALUE_BY_ID_TENANT_KEY,
					Arrays.asList(new Pair<>("id", key), new Pair<>(TENANT_ID, tenantId)), 0, 1);
			if (values.isEmpty()) {
				return null;
			}
			return getTrimedValue(values);
		} catch (IllegalStateException | PersistenceException e) {
			LOGGER.info("Configuration cannot be retrieved found {} because of {}", key, e.getMessage());
			LOGGER.trace("Configuration cannot be retrieved found {}", key, e);
		}
		return null;
	}

	private static String getTrimedValue(List<String> values) {
		return StringUtils.trimToNull(values.get(0));
	}

	@Override
	public Properties getConfigurations() {
		List<ConfigurationEntity> entities = dbDao.fetchWithNamed(ConfigurationEntity.QUERY_CONFIG_FOR_TENANT_KEY,
				CONFIGURATION_ARGS);

		Properties properties = new Properties();
		entities.stream().forEach((c) -> properties.put(buildPropertyKey(c), c.getValue()));
		return properties;
	}

	@Override
	public Properties getConfigurations(String tenantId) {
		if (tenantId == null) {
			return new Properties();
		}

		List<ConfigurationEntity> entities = dbDao.fetchWithNamed(ConfigurationEntity.QUERY_CONFIG_FOR_TENANT_KEY,
				Arrays.asList(new Pair<>(TENANT_ID, tenantId), SYSTEM_TENANT));

		Properties properties = new Properties();
		entities.stream().forEach((c) -> properties.put(buildPropertyKey(c), c.getValue()));
		return properties;
	}

	private static Object buildPropertyKey(ConfigurationEntity c) {
		return c.getId().getTenantId() != null ? c.getId().getTenantId() + "." + c.getId() : c.getId();
	}

}
