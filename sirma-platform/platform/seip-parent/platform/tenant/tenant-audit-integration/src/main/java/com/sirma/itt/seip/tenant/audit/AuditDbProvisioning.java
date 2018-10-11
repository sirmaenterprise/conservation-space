package com.sirma.itt.seip.tenant.audit;

import static com.sirma.itt.seip.tenant.db.DbProvisioning.createUserNameFromTenantId;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.emf.audit.configuration.AuditConfiguration;
import com.sirma.itt.emf.audit.patch.AuditDbPatchService;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.db.Configuration;
import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.db.BaseRelationalDbProvisioning;
import com.sirma.itt.seip.tenant.db.TenantRelationalContext;

/**
 * The AuditDbProvisioning is service that creates/manipulates all related audit db operation for
 * tenant.
 *
 * @author BBonev
 */
@ApplicationScoped
public class AuditDbProvisioning extends BaseRelationalDbProvisioning {

	private static final String AUDIT_SUFFIX = "_audit";

	/** The db dialect. */
	@Inject
	@com.sirma.itt.seip.configuration.annotation.Configuration
	@ConfigurationPropertyDefinition(name = "system.db_audit.dialect", defaultValue = "postgresql", label = "Default audit database provider dialect.", system = true, sensitive = true)
	protected ConfigurationProperty<String> auditDbDialect;

	/** The db user. */
	@Inject
	@com.sirma.itt.seip.configuration.annotation.Configuration
	@ConfigurationPropertyDefinition(name = "system.db_audit.user", defaultValue = "admin", label = "Default host user for audit database.", system = true, sensitive = true)
	protected ConfigurationProperty<String> auditDbUser;

	/** The db pass. */
	@Inject
	@com.sirma.itt.seip.configuration.annotation.Configuration
	@ConfigurationPropertyDefinition(name = "system.db_audit.pass", defaultValue = "admin", label = "Default host password for audit database.", system = true, sensitive = true)
	protected ConfigurationProperty<String> auditDbPass;

	@Inject
	private AuditDbPatchService patchDbService;

	@Inject
	private AuditConfiguration auditConfiguration;

	@Override
	protected void patchDatabase(TenantInfo tenantInfo) throws RollbackedException {
		securityContextManager.initializeTenantContext(tenantInfo.getTenantId());
		try {
			patchDbService.patchAuditDatabase(auditConfiguration);
		} finally {
			securityContextManager.endContextExecution();
		}
	}

	@Override
	protected Collection<Configuration> getConfigurations(TenantInfo tenantInfo, TenantRelationalContext context,
			TenantRelationalContext contextToUse) {
		URI address = context.getServerAddress();
		String dbName = context.getDatabaseName();
		securityContextManager.initializeTenantContext(tenantInfo.getTenantId());
		try {
			List<Configuration> configurations = new ArrayList<>(6);
			configurations.add(new Configuration(auditConfiguration.getDatabaseAddressConfigurationName(),
					address.getHost(), tenantInfo.getTenantId()));
			configurations.add(new Configuration(auditConfiguration.getDatabasePortConfiguration().getName(),
					Integer.toString(address.getPort()), tenantInfo.getTenantId()));
			configurations.add(new Configuration(auditConfiguration.getDatabaseDialectConfiguration().getName(),
					auditDbDialect.get(), tenantInfo.getTenantId()));
			configurations.add(new Configuration(auditConfiguration.getDatabaseNameConfiguration().getName(), dbName,
					tenantInfo.getTenantId()));
			if (contextToUse != null) {
				configurations.add(new Configuration(auditConfiguration.getAdminUsernameConfiguration().getName(),
						contextToUse.getAccessUser(), tenantInfo.getTenantId()));
				configurations.add(new Configuration(auditConfiguration.getAdminPasswordConfiguration().getName(),
						contextToUse.getAccessUserPassword(), tenantInfo.getTenantId()));
			} else {
				configurations.add(new Configuration(auditConfiguration.getAdminUsernameConfiguration().getName(),
						context.getAccessUser(), tenantInfo.getTenantId()));
				configurations.add(new Configuration(auditConfiguration.getAdminPasswordConfiguration().getName(),
						context.getAccessUserPassword(), tenantInfo.getTenantId()));
			}
			return configurations;
		} finally {
			securityContextManager.endContextExecution();
		}
	}

	@Override
	public String getDatabaseName(TenantInfo tenantInfo) {
		return createUserNameFromTenantId(tenantInfo) + AUDIT_SUFFIX;
	}

	@Override
	protected String getDbUser() {
		return auditDbUser.get();
	}

	@Override
	protected String getDbPass() {
		return auditDbPass.get();
	}

	@Override
	public String getDatasourceName(TenantInfo tenantInfo) {
		return tenantInfo.getTenantId() + AUDIT_SUFFIX;
	}

	@Override
	protected String getDbDialect() {
		return auditDbDialect.get();
	}

	@Override
	protected String getSubsystemSuffix() {
		return AUDIT_SUFFIX;
	}

}