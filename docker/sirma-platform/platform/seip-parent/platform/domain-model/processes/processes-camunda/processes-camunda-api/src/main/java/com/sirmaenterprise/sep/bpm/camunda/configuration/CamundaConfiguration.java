package com.sirmaenterprise.sep.bpm.camunda.configuration;

import java.util.Objects;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.db.DatabaseSettings;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * The {@link CamundaConfiguration} is factory for all Camunda related configurations.
 *
 * @author bbanchev
 */
public interface CamundaConfiguration extends DatabaseSettings {

	/**
	 * Gets the datasource name.
	 *
	 * @return the datasource name
	 */
	ConfigurationProperty<String> getDatasourceName();

	/**
	 * Gets the engine name.
	 *
	 * @return the engine name
	 */
	ConfigurationProperty<String> getEngineName();

	/**
	 * Retrieves a Camunda engine name from the provided security context
	 *
	 * @param securityContext
	 *            to get engine name for
	 * @return the Camunda engine name or throws error on invalid context provided
	 */
	static String getEngineName(SecurityContext securityContext) {
		Objects.requireNonNull(securityContext,
				"Initialized security context is a required object to build Camunda engine name!");
		Objects.requireNonNull(securityContext.getCurrentTenantId(),
				"Tenant identifier is a required object to build Camunda engine name!");
		return securityContext.getCurrentTenantId().replaceAll("\\.", "_");
	}

	/**
	 * Retrieves a Camunda engine name from the provided tenant id
	 *
	 * @param tenantId
	 *            to get engine name for
	 * @return the Camunda engine name or throws error on invalid tenant id provided
	 */
	static String getEngineName(String tenantId) {
		Objects.requireNonNull(tenantId, "Tenant identifier is a required object to build Camunda engine name!");
		return tenantId.replaceAll("\\.", "_");
	}

}