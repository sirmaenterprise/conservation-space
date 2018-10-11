package com.sirma.itt.seip.tenant.db;

import static com.sirma.itt.seip.tenant.db.DbProvisioning.createUserNameFromTenantId;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.db.Configuration;
import com.sirma.itt.seip.db.DatabaseConfiguration;
import com.sirma.itt.seip.db.patch.PatchService;
import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.tenant.context.TenantInfo;

/**
 * The RelationDbProvisioning is service that creates/manipulates all related db operation for tenant.
 *
 * @author bbanchev
 * @author BBonev
 */
@ApplicationScoped
public class RelationDbProvisioning extends BaseRelationalDbProvisioning {

	/** The db dialect. */
	@Inject
	@com.sirma.itt.seip.configuration.annotation.Configuration
	@ConfigurationPropertyDefinition(name = "system.db.dialect", defaultValue = "postgresql", label = "Default database provider dialect.", system = true, sensitive = true)
	protected ConfigurationProperty<String> dbDialect;

	/** The db pass. */
	@Inject
	@com.sirma.itt.seip.configuration.annotation.Configuration
	@ConfigurationPropertyDefinition(name = "system.db.pass", defaultValue = "admin", label = "Default host passwordd for database.", system = true, sensitive = true)
	protected ConfigurationProperty<String> dbPass;
	
	/** The db user. */
	@Inject
	@com.sirma.itt.seip.configuration.annotation.Configuration
	@ConfigurationPropertyDefinition(name = "system.db.user", defaultValue = "admin", label = "Default host user for database.", system = true, sensitive = true)
	protected ConfigurationProperty<String> dbUser;

	@Inject
	private PatchService patchDbService;

	@Inject
	private DatabaseConfiguration databaseConfiguration;

	@Override
	protected void patchDatabase(TenantInfo tenantInfo) throws RollbackedException {
		securityContextManager.initializeTenantContext(tenantInfo.getTenantId());
		try {
			patchDbService.patchSchema();
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
			configurations.add(new Configuration(databaseConfiguration.getDatabaseAddressConfigurationName(),
					address.getHost(), tenantInfo.getTenantId()));
			configurations.add(new Configuration(databaseConfiguration.getDatabasePortConfiguration().getName(),
					Integer.toString(address.getPort()), tenantInfo.getTenantId()));
			configurations.add(new Configuration(databaseConfiguration.getDatabaseDialectConfiguration().getName(),
					dbDialect.get(), tenantInfo.getTenantId()));
			configurations.add(new Configuration(databaseConfiguration.getDatabaseNameConfiguration().getName(), dbName,
					tenantInfo.getTenantId()));
			configurations.add(new Configuration(databaseConfiguration.getAdminUsernameConfiguration().getName(),
					context.getAccessUser(), tenantInfo.getTenantId()));
			configurations.add(new Configuration(databaseConfiguration.getAdminPasswordConfiguration().getName(),
					context.getAccessUserPassword(), tenantInfo.getTenantId()));

			return configurations;
		} finally {
			securityContextManager.endContextExecution();
		}
	}

	@Override
	protected String getDatabaseName(TenantInfo tenantInfo) {
		return createUserNameFromTenantId(tenantInfo);
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
	public String getDatasourceName(TenantInfo tenantInfo) {
		return tenantInfo.getTenantId();
	}

	@Override
	protected String getDbDialect() {
		return dbDialect.get();
	}

	@Override
	protected String getSubsystemSuffix() {
		return "";
	}
}