package com.sirma.itt.seip.configuration.db;

import static com.sirma.itt.seip.collections.CollectionUtils.emptyConsumer;
import static com.sirma.itt.seip.collections.CollectionUtils.toIdentityMap;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.configuration.build.ConfigurationInstance;
import com.sirma.itt.seip.configuration.build.ConfigurationInstanceProvider;
import com.sirma.itt.seip.configuration.build.ConfigurationProvider;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.security.annotation.RunAsAllTenantAdmins;
import com.sirma.itt.seip.security.annotation.RunAsSystem;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Default implementation for accessing configurations store.
 *
 * @author BBonev
 */
@ApplicationScoped
class ConfigurationManagementImpl implements ConfigurationManagement {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private ConfigurationDao dao;

	@Inject
	private ConfigurationInstanceProvider instanceProvider;
	@Inject
	private ConfigurationProvider configurationProvider;

	@Inject
	private SecurityContext securityContext;

	@Inject
	private DatabaseIdManager databaseIdManager;

	@Startup
	@RunAsSystem
	protected void cleanupDeletedSystemConfigurations() {
		cleanup(dao.getSystemConfigurations());
	}

	@Startup
	@RunAsAllTenantAdmins(includeInactive = true)
	protected void cleanupDeletedConfigurations() {
		cleanup(dao.getConfigurationsByTenant(securityContext.getCurrentTenantId()));
	}

	protected void cleanup(Collection<ConfigurationEntity> currentConfigurations) {
		Set<String> persistedConfigurationIds = currentConfigurations
				.stream()
					.map(entity -> entity.getId().getKey())
					.collect(Collectors.toSet());

		Set<String> registeredConfigurations = instanceProvider.getRegisteredConfigurations();
		persistedConfigurationIds.removeAll(registeredConfigurations);

		for (String configId : persistedConfigurationIds) {
			removeConfiguration(configId);
		}
	}

	@Override
	public Collection<Configuration> getAllConfigurations() {
		List<ConfigurationEntity> configurations = dao.getAllEntities();

		return mergeResults(configurations, buildFromDefinition(instanceProvider.getAllInstances()));
	}

	private Map<ConfigurationId, Configuration> buildFromDefinition(Collection<ConfigurationInstance> allInstances) {
		return allInstances.stream().map(Configuration::new).peek(c -> {
			String tenantId = SecurityContext.SYSTEM_TENANT;
			if (!c.getDefinition().isSystemConfiguration()) {
				tenantId = securityContext.getCurrentTenantId();
			}
			c.setTenantId(tenantId);
		}).collect(toIdentityMap(ConfigurationId::new));
	}

	@Override
	public Collection<Configuration> getSystemConfigurations() {
		List<ConfigurationEntity> configurations = dao.getSystemConfigurations();

		return mergeResults(configurations,
				buildFromDefinition(instanceProvider.getFiltered(ConfigurationInstance::isSystemConfiguration)));
	}

	@Override
	public Collection<Configuration> getCurrentTenantConfigurations() {
		List<ConfigurationEntity> configurations = dao.getConfigurationsByTenant(securityContext.getCurrentTenantId());

		return mergeResults(configurations,
				buildFromDefinition(instanceProvider.getFiltered(e -> !e.isSystemConfiguration())));
	}

	private Collection<Configuration> mergeResults(List<ConfigurationEntity> persisted,
			Map<ConfigurationId, Configuration> resultTo) {
		Set<String> registeredConfigurations = instanceProvider.getRegisteredConfigurations();

		Map<ConfigurationId, Configuration> persistedResult = persisted
				.stream()
					.filter(entity -> registeredConfigurations.contains(entity.getId().getKey()))
					.map(this::convertToInstance)
					.collect(toIdentityMap(ConfigurationId::new));

		resultTo.keySet().removeAll(persistedResult.keySet());

		resultTo.putAll(persistedResult);

		return resultTo.values();
	}

	@Override
	@Transactional(TxType.REQUIRES_NEW)
	public Collection<Configuration> addConfigurations(Collection<Configuration> configurations) {

		Map<String, Map<ConfigurationId, Configuration>> tenantConfigurations = new HashMap<>();
		for (Configuration configuration : configurations) {
			String tenantId = StringUtils.trimToNull(configuration.getTenantId());
			if (tenantId == null) {
				tenantId = SecurityContext.SYSTEM_TENANT;
			}
			tenantConfigurations.computeIfAbsent(tenantId, k -> new HashMap<>()).put(
					new ConfigurationId(configuration.getConfigurationKey(), tenantId), configuration);
		}

		Collection<ConfigurationId> added = new HashSet<>();

		for (Entry<String, Map<ConfigurationId, Configuration>> perTenant : tenantConfigurations.entrySet()) {
			Map<ConfigurationId, Configuration> tenantConfigs = perTenant.getValue();
			added.addAll(addNewConfigurations(perTenant.getKey(), tenantConfigs));
		}

		return CollectionUtils.transformToList(added, Configuration::new);
	}

	/*
	 * add the new values only and do not modify any existing configurations. Also does not fire events for changes.
	 */
	private Collection<ConfigurationId> addNewConfigurations(String tenantId,
			Map<ConfigurationId, Configuration> toFilter) {

		// new configuration keys
		Collection<String> keyToLoad = toFilter
				.keySet()
					.stream()
					.map(ConfigurationId::getKey)
					.collect(Collectors.toSet());

		// get configurations that are present in the database
		List<ConfigurationEntity> foundEntities = dao.getTenantConfigurations(keyToLoad, tenantId);

		// remove from the input configurations all that are already defined
		Set<ConfigurationId> withValues = foundEntities
				.stream()
					.filter(c -> StringUtils.trimToNull(c.getValue()) != null)
					.map(ConfigurationEntity::getId)
					.collect(Collectors.toSet());

		toFilter.keySet().removeAll(withValues);
		List<ConfigurationEntity> emptyValues = foundEntities
				.stream()
					.filter(c -> !withValues.contains(c.getId()))
					.collect(Collectors.toList());

		// nothing to add or update
		if (toFilter.isEmpty() && emptyValues.isEmpty()) {
			return Collections.emptyList();
		}

		// update all existing configurations
		updateConfigurationsInternal(toFilter, emptyValues, tenantId, emptyConsumer());

		// combine modified ids
		List<ConfigurationId> modified = new ArrayList<>(toFilter.size() + emptyValues.size());
		modified.addAll(toFilter.keySet());
		emptyValues.forEach(c -> modified.add(c.getId()));
		return modified;
	}

	@Override
	public void updateSystemConfiguration(Configuration configuration) {
		List<ConfigurationEntity> list = dao
				.getSystemConfigurations(
				Collections.singletonList(configuration.getConfigurationKey()));

		updateSingleConfigurationInternal(configuration, list, SecurityContext.SYSTEM_TENANT);
	}

	@Override
	public void updateSystemConfigurations(Collection<Configuration> configurations) {
		updateConfigurationsInternal(configurations,
				c -> dao.getSystemConfigurations(CollectionUtils.transformToList(c, ConfigurationId::getKey)),
				SecurityContext.SYSTEM_TENANT, this::notifyForValueChange);
	}



	@Override
	public void updateConfiguration(Configuration configuration) {
		List<ConfigurationEntity> list = getCurrentTenantConfigurations(
				Collections.singletonList(configuration.getConfigurationKey()));
		updateSingleConfigurationInternal(configuration, list, securityContext.getCurrentTenantId());
	}

	private void updateSingleConfigurationInternal(Configuration configuration, List<ConfigurationEntity> list,
			String tenantId) {
		ConfigurationEntity entity;
		if (list.isEmpty()) {
			entity = new ConfigurationEntity();
			entity.setId(new ConfigurationId(configuration.getConfigurationKey(), tenantId));
			databaseIdManager.register(entity);
		} else {
			entity = list.get(0);
		}
		entity.setValue(Objects.toString(configuration.getValue(), null));
		try {
			dao.persist(entity);
			notifyForValueChange(entity.getId());
		} finally {
			databaseIdManager.unregister(entity);
		}
	}

	@Override
	public void updateConfigurations(Collection<Configuration> configurations) {
		updateConfigurationsInternal(configurations,
				c -> getCurrentTenantConfigurations(CollectionUtils.transformToList(c, ConfigurationId::getKey)),
				securityContext.getCurrentTenantId(), this::notifyForValueChange);
	}

	/**
	 * Update configuration internal.
	 *
	 * @param configurations
	 *            the configurations to add or update
	 * @param loadCurrentConfigurations
	 *            called to load the current configurations
	 * @param tenantId
	 *            the tenant filter
	 * @param notifyForChange
	 *            called to notify for change for the given configuration name in the current tenant context
	 */
	private void updateConfigurationsInternal(Collection<Configuration> configurations,
			Function<Collection<ConfigurationId>, List<ConfigurationEntity>> loadCurrentConfigurations, String tenantId,
			Consumer<ConfigurationId> notifyForChange) {
		if (configurations.isEmpty()) {
			return;
		}

		Map<ConfigurationId, Configuration> configMapping = toIdentityMap(configurations,
				c -> new ConfigurationId(c.getConfigurationKey(), tenantId));

		List<ConfigurationEntity> list = loadCurrentConfigurations.apply(configMapping.keySet());

		// update all existing configurations
		updateConfigurationsInternal(configMapping, list, tenantId, notifyForChange);
	}

	/**
	 * Update configurations internal.
	 *
	 * @param changedConfigs
	 *            the configuration mapping to update by config key.
	 * @param toUpdate
	 *            of current configurations
	 * @param tenantId
	 *            the tenant id to set to new configurations
	 * @param notifyForChange
	 *            called to notify for change for the update configurations
	 */
	private void updateConfigurationsInternal(Map<ConfigurationId, Configuration> changedConfigs,
			List<ConfigurationEntity> toUpdate, String tenantId, Consumer<ConfigurationId> notifyForChange) {
		// update existing configurations
		for (ConfigurationEntity entity : toUpdate) {
			Configuration configuration = changedConfigs.remove(entity.getId());
			entity.setValue(Objects.toString(configuration.getValue(), null));
			LOGGER.trace("Updating configuration value: {}", entity);
			dao.persist(entity);
			notifyForChange.accept(entity.getId());
		}

		Set<String> allConfigurations = instanceProvider.getRegisteredConfigurations();

		// insert new configurations
		for (Entry<ConfigurationId, Configuration> entry : changedConfigs.entrySet()) {
			ConfigurationId key = entry.getKey();
			if (!allConfigurations.contains(key.getKey())) {
				LOGGER.warn("Trying to add configuration that is not defined. Skipping it!");
				continue;
			}
			Configuration value = entry.getValue();
			ConfigurationEntity entity = new ConfigurationEntity();
			entity.setId(new ConfigurationId(key.getKey(), tenantId));
			entity.setValue(Objects.toString(value.getValue(), null));
			databaseIdManager.register(entity);
			try {
				LOGGER.trace("Adding new configuration: {}", entity);
				dao.persist(entity);
				notifyForChange.accept(key);
			} finally {
				databaseIdManager.unregister(entity);
			}
		}
	}

	private List<ConfigurationEntity> getCurrentTenantConfigurations(Collection<String> keys) {
		return dao.getTenantConfigurations(keys, securityContext.getCurrentTenantId());
	}

	private Configuration convertToInstance(ConfigurationEntity entity) {
		String configId = entity.getId().getKey();
		ConfigurationInstanceProvider configProvider = instanceProvider;
		ConfigurationProvider valueProvider = configurationProvider;
		Configuration configuration = new Configuration(configId, entity.getId().getTenantId(),
				() -> valueProvider.getProperty(configId).get(), () -> configProvider.getConfiguration(configId));
		// Preserving the original value before any conversion/modification
		configuration.setRawValue(entity.getValue());
		return configuration;
	}

	private void notifyForValueChange(ConfigurationId key) {
		configurationProvider.getProperty(key.getKey()).valueUpdated();
	}

	@Override
	public void removeSystemConfiguration(String key) {
		dao.deleteConfiguration(key, SecurityContext.SYSTEM_TENANT);
	}


	@Override
	public void removeConfiguration(String key) {
		dao.deleteConfiguration(key, securityContext.getCurrentTenantId());
	}

	@Override
	public void removeAllConfigurations() {
		dao.deleteTenantConfigurations(securityContext.getCurrentTenantId());
	}

}
