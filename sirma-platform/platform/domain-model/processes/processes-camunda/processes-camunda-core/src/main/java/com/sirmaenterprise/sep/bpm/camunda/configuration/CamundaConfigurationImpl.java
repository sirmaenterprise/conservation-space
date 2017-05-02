package com.sirmaenterprise.sep.bpm.camunda.configuration;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.configuration.ConfigurationException;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.ConverterContext;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Configuration provider for Camunda related configurations. Provides base configurations for db access, process engine
 * access and so on. Some of the configurations could be accessed only in tenant mode, otherwise exception will be
 * generated.
 * 
 * @author bbanchev
 */
@Singleton
public class CamundaConfigurationImpl implements CamundaConfiguration {

	@ConfigurationPropertyDefinition(type = String.class, sensitive = true, name = "processes.camunda.datasource.name", label = "Tenant aware datasource name for camunda.")
	private static final String DATASOURCE_NAME = "processes.camunda.datasource.name";
	@Inject
	@Configuration(DATASOURCE_NAME)
	private ConfigurationProperty<String> dsName;

	@ConfigurationPropertyDefinition(type = String.class, name = "processes.camunda.engine.name", label = "Tenant aware engine name for Camunda.")
	private static final String ENGINE_NAME = "processes.camunda.engine.name";
	@Inject
	@Configuration(ENGINE_NAME)
	private ConfigurationProperty<String> engineName;

	@SuppressWarnings("unused")
	@ConfigurationConverter(DATASOURCE_NAME)
	static String buildDatasourceName(ConverterContext context, SecurityContext securityContext) {// NOSONAR
		validateSecurityContext(DATASOURCE_NAME, securityContext);
		String tenantId = securityContext.getCurrentTenantId();
		if (SecurityContext.isDefaultTenant(tenantId)) {
			return tenantId;
		}
		return tenantId + "_camunda";
	}

	@ConfigurationConverter(ENGINE_NAME)
	@SuppressWarnings("unused")
	static String buildEngineName(ConverterContext context, SecurityContext securityContext) {// NOSONAR
		validateSecurityContext(ENGINE_NAME, securityContext);
		return CamundaConfiguration.getEngineName(securityContext);
	}

	private static void validateSecurityContext(String configuration, SecurityContext securityContext) {
		if (!securityContext.isActive() || securityContext.isSystemTenant()) {
			throw new ConfigurationException(configuration + " could be requested only in tenant mode!");
		}
	}

	@Override
	public ConfigurationProperty<String> getDatasourceName() {
		return dsName;
	}

	@Override
	public ConfigurationProperty<String> getEngineName() {
		return engineName;
	}

}
