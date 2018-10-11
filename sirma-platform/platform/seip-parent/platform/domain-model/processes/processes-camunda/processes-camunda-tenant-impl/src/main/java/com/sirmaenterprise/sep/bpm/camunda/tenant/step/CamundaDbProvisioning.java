package com.sirmaenterprise.sep.bpm.camunda.tenant.step;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.db.Configuration;
import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.db.BaseRelationalDbProvisioning;
import com.sirma.itt.seip.tenant.db.TenantRelationalContext;
import com.sirmaenterprise.sep.bpm.camunda.configuration.CamundaConfiguration;

/**
 * The {@link CamundaDbProvisioning} is service that creates/manipulates all related db operation for tenant related to
 * Camunda integration.
 *
 * @author bbanchev
 * @author BBonev
 */
@ApplicationScoped
public class CamundaDbProvisioning extends BaseRelationalDbProvisioning {

	private static final String SYSTEM_SUFFIX = "_camunda";

	/** The Camunda db dialect. */
	@Inject
	@com.sirma.itt.seip.configuration.annotation.Configuration
	@ConfigurationPropertyDefinition(name = "system.db_camunda.dialect", defaultValue = "postgresql", label = "Default Camunda database provider dialect.", system = true, sensitive = true)
	private ConfigurationProperty<String> dbDialect;

	/** The Camunda db user. */
	@Inject
	@com.sirma.itt.seip.configuration.annotation.Configuration
	@ConfigurationPropertyDefinition(name = "system.db_camunda.user", defaultValue = "admin", label = "Default host user for Camunda database.", system = true, sensitive = true)
	private ConfigurationProperty<String> dbUser;

	/** The Camunda db user pass. */
	@Inject
	@com.sirma.itt.seip.configuration.annotation.Configuration
	@ConfigurationPropertyDefinition(name = "system.db_camunda.pass", defaultValue = "admin", label = "Default host password for Camunda database.", system = true, sensitive = true)
	private ConfigurationProperty<String> dbPass;

	@Inject
	private CamundaConfiguration camundaConfiguration;

	/**
	 * Check if db context is provided for Camunda and setup is complete
	 *
	 * @param tenantInfo
	 *            the tenant to check provisioning for
	 * @return true if there is registered datasource for the provided {@link TenantInfo}
	 */
	public boolean isProvisioned(TenantInfo tenantInfo) {
		return datasourceProvisioner.getXaDataSourceDatabase(getDsName(tenantInfo)) != null;
	}

	@Override
	protected Collection<Configuration> getConfigurations(TenantInfo tenantInfo, TenantRelationalContext context,
			TenantRelationalContext contextToUse) {
		URI address = context.getServerAddress();
		securityContextManager.initializeTenantContext(tenantInfo.getTenantId());
		try {
			List<Configuration> configurations = new ArrayList<>(6);
			configurations.add(new Configuration(camundaConfiguration.getDatabaseHostConfiguration().getName(),
					address.getHost(), tenantInfo.getTenantId()));
			configurations.add(new Configuration(camundaConfiguration.getDatabasePortConfiguration().getName(),
					Integer.toString(address.getPort()), tenantInfo.getTenantId()));
			configurations.add(new Configuration(camundaConfiguration.getDatabaseNameConfiguration().getName(),
					context.getDatabaseName(), tenantInfo.getTenantId()));
			configurations.add(new Configuration(camundaConfiguration.getDatabaseDialectConfiguration().getName(),
					dbDialect.get(), tenantInfo.getTenantId()));
			if (contextToUse != null) {
				configurations.add(new Configuration(camundaConfiguration.getAdminUsernameConfiguration().getName(),
						contextToUse.getAccessUser(), tenantInfo.getTenantId()));
				configurations.add(new Configuration(camundaConfiguration.getAdminPasswordConfiguration().getName(),
						contextToUse.getAccessUserPassword(), tenantInfo.getTenantId()));
			} else {
				configurations.add(new Configuration(camundaConfiguration.getAdminUsernameConfiguration().getName(),
						context.getAccessUser(), tenantInfo.getTenantId()));
				configurations.add(new Configuration(camundaConfiguration.getAdminPasswordConfiguration().getName(),
						context.getAccessUserPassword(), tenantInfo.getTenantId()));
			}
			return configurations;
		} finally {
			securityContextManager.endContextExecution();
		}
	}

	private String getDsName(TenantInfo tenantInfo) {
		securityContextManager.initializeTenantContext(tenantInfo.getTenantId());
		try {
			return camundaConfiguration.getDatasourceName().get();
		} finally {
			securityContextManager.endContextExecution();
		}

	}

	@Override
	protected String getDatabaseName(TenantInfo tenantInfo) {
		return getDsName(tenantInfo).replaceAll("[\\.-]+", "_");
	}

	@Override
	protected String getDbUser() {
		return dbUser.get();
	}

	@Override
	protected String getDbPass() {
		return dbPass.get();
	}

	@Override
	protected String getDatasourceName(TenantInfo tenantInfo) {
		return getDsName(tenantInfo);
	}

	@Override
	protected String getDbDialect() {
		return dbDialect.get();
	}

	@Override
	protected String getSubsystemSuffix() {
		return SYSTEM_SUFFIX;
	}

	@Override
	protected void patchDatabase(TenantInfo tenantInfo) throws RollbackedException {
		// No need to patch the camunda database on creation.
	}
}