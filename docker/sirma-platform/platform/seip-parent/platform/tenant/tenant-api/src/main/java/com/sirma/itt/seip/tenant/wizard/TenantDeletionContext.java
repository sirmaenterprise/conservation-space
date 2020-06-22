package com.sirma.itt.seip.tenant.wizard;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sirma.itt.seip.configuration.db.Configuration;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.wizard.exception.TenantDeletionException;

/**
 * Tenant context payload object which carries information about tenant deletion.
 *
 * @author smustafov
 */
public class TenantDeletionContext {

	private TenantInfo tenantInfo;

	private boolean rollback;

	private Map<String, Configuration> configurations;

	/**
	 * Constructs new tenant delete context with given tenant info and if this is rollback.
	 *
	 * @param tenantInfo the tenant info
	 * @param rollback   flag indicating if this is should be a rollback
	 */
	public TenantDeletionContext(TenantInfo tenantInfo, boolean rollback) {
		this.tenantInfo = tenantInfo;
		this.rollback = rollback;
	}

	public TenantInfo getTenantInfo() {
		return tenantInfo;
	}

	public boolean shouldRollback() {
		return rollback;
	}

	public void setConfigurations(Collection<Configuration> tenantConfigurations) {
		Objects.requireNonNull(tenantConfigurations, "Cannot set empty tenant configurations");

		configurations = tenantConfigurations.stream()
				.collect(Collectors.toMap(Configuration::getConfigurationKey, Function.identity()));
	}

	/**
	 * Returns the value of given configuration by its name.
	 *
	 * @param name configuration name
	 * @return configuration value as string
	 */
	public String getConfigValue(String name) {
		Configuration configuration = configurations.get(name);
		if (configuration == null) {
			throw new TenantDeletionException("Configuration not found: " + name);
		}
		return configuration.getRawValue();
	}
}
