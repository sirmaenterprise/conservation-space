package com.sirma.itt.seip.tenant.step;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.db.DatabaseSettings;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.db.TenantRelationalContext;
import com.sirma.itt.seip.tenant.wizard.AbstractTenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;

/**
 * Base tenant relational step provides common logic needed for the relational tenant
 * creation/deletion steps.
 * 
 * @author nvelkov
 */
public abstract class AbstractTenantRelationalStep extends AbstractTenantStep {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private SecurityContextManager securityContextManager;

	/**
	 * Get the relational context from the database settings in the provided tenant's context. The
	 * relational context is passed down to the relational provisioner and is used to create/delete
	 * the databases and datasources.
	 * 
	 * @param databaseSettings
	 *            the database settings from which to retrieve the database settings
	 * @param datasourceName
	 *            the datasource name
	 * @param tenantInfo
	 *            the tenant info of the provided tenant
	 * @return the {@link TenantRelationalContext}
	 */
	protected TenantRelationalContext getRelationalContext(DatabaseSettings databaseSettings, String datasourceName,
			TenantInfo tenantInfo) {
		securityContextManager.initializeTenantContext(tenantInfo.getTenantId());
		TenantRelationalContext relationalContext = new TenantRelationalContext();
		relationalContext.setDatabaseName(databaseSettings.getDatabaseNameConfiguration().get());
		relationalContext.setDatasourceName(datasourceName);
		relationalContext.setAccessUser(databaseSettings.getAdminUsernameConfiguration().get());
		relationalContext.setAccessUserPassword(databaseSettings.getAdminPasswordConfiguration().get());
		try {
			relationalContext.setServerAddress(new URI(databaseSettings.getDatabaseDialectConfiguration().get(), null,
					databaseSettings.getDatabaseHostConfiguration().get(),
					databaseSettings.getDatabasePortConfiguration().get(), null, null, null));
		} catch (URISyntaxException e) {
			LOGGER.warn("A valid Server address couldn't be constructed from the given database settings.");
			LOGGER.trace(
					"A valid server address couldn't be constructed from the given database settings because of {}.",
					e);
		}
		securityContextManager.endContextExecution();
		return relationalContext;
	}

	/**
	 * Get the relational context from the database settings in the provided tenant's context if
	 * it's going to be reused. Otherwise return null. Used when an already-existing database is
	 * going to be reused instead of creating a new one.
	 * 
	 * @param data
	 *            the tenant step data
	 * @param databaseSettings
	 *            the database settings
	 * @param datasourceName
	 *            the datasource name
	 * @param tenantInfo
	 *            the tenant info
	 * @return the {@link TenantRelationalContext}
	 */
	protected TenantRelationalContext getRelationalContextIfReused(TenantStepData data,
			DatabaseSettings databaseSettings, String datasourceName, TenantInfo tenantInfo) {
		TenantRelationalContext contextToUse = null;
		if (isDatabaseReused(data)) {
			contextToUse = getRelationalContext(databaseSettings, datasourceName, tenantInfo);
		}
		return contextToUse;
	}

	/**
	 * Checks if reusing database is allowed for current tenant data.
	 *
	 * @param data
	 *            is the tenant info data to check
	 * @return false if parameter 'reuseDatabase' is not set or it is false
	 */

	protected static boolean isDatabaseReused(TenantStepData data) {
		if (data == null || data.getProperties() == null) {
			return false;
		}
		Serializable reuseDatabase = data.getProperties().getOrDefault("reuseDatabase", "false");
		return Boolean.parseBoolean(reuseDatabase.toString());
	}
}