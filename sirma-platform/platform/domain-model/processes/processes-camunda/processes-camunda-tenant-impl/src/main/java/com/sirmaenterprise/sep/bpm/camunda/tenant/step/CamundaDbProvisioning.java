package com.sirmaenterprise.sep.bpm.camunda.tenant.step;

import static com.sirma.itt.seip.tenant.db.DbProvisioning.createUserPasswordForTenantId;
import static com.sirma.itt.seip.tenant.db.DbProvisioning.toDbAddressUrl;
import static com.sirma.itt.seip.wildfly.cli.db.WildFlyDatasourceProvisioning.addUserAndPassword;
import static com.sirma.itt.seip.wildfly.cli.db.WildFlyDatasourceProvisioning.createXaDatasource;
import static com.sirma.itt.seip.wildfly.cli.db.WildFlyDatasourceProvisioning.getXaDataSourceDatabase;
import static com.sirma.itt.seip.wildfly.cli.db.WildFlyDatasourceProvisioning.removeDatasource;

import java.io.Serializable;
import java.net.URI;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.db.BaseRelationalDbProvisioning;
import com.sirma.itt.seip.tenant.db.DbProvisioning;
import com.sirma.itt.seip.tenant.db.TenantRelationalContext;
import com.sirma.itt.seip.tenant.wizard.SubsystemTenantAddressProvider;
import com.sirma.itt.seip.util.CDI;
import com.sirma.itt.seip.wildfly.cli.db.WildFlyDatasourceProvisioning;
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
	private static final Logger LOGGER = LoggerFactory.getLogger(CamundaDbProvisioning.class);

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

	@Inject
	private DbProvisioning dbProvisioning;

	private SubsystemTenantAddressProvider addressProvider;

	@Override
	@PostConstruct
	protected void initialize() {
		super.initialize();

		String dialect = dbDialect.get() + SYSTEM_SUFFIX;
		addressProvider = CDI.instantiateBean(dialect, SubsystemTenantAddressProvider.class, beanManager);
		if (addressProvider == null) {
			throw new EmfRuntimeException("Could not find address provider for " + dialect);
		}
	}

	/**
	 * Provision the model for Camunda integration.
	 *
	 * @param model
	 *            the source data model
	 * @param tenantInfo
	 *            the currently created tenant info
	 * @param contextToUse
	 *            the context to use as source data - might be null
	 * @param relationalContext
	 *            the relational context to use as configuration storage
	 * @throws RollbackedException
	 *             on provision error
	 */
	public void provision(Map<String, Serializable> model, TenantInfo tenantInfo, TenantRelationalContext contextToUse,
			TenantRelationalContext relationalContext) throws RollbackedException {
		provisionInternal(model, tenantInfo, contextToUse, relationalContext);
	}

	/**
	 * Check if db context is provided for Camunda and setup is complete
	 * 
	 * @param tenantInfo
	 *            the tenant to check provisioning for
	 * @return true if there is registered datasource for the provided {@link TenantInfo}
	 */
	public boolean isProvisioned(TenantInfo tenantInfo) {
		return WildFlyDatasourceProvisioning.getXaDataSourceDatabase(controller, getDsName(tenantInfo)) != null;
	}

	private void provisionInternal(Map<String, Serializable> model, TenantInfo tenantInfo,
			TenantRelationalContext contextToUse, TenantRelationalContext relationalContext)
			throws RollbackedException {
		URI address;
		String dsName = getDsName(tenantInfo);
		if (!SecurityContext.isDefaultTenant(tenantInfo.getTenantId())) {
			String databaseName;
			if (contextToUse != null) {
				address = contextToUse.getServerAddress();
				databaseName = contextToUse.getDatabaseName();

				addUserAndPassword(model, contextToUse.getAccessUser(), contextToUse.getAccessUserPassword());
				relationalContext.setAccessUser(contextToUse.getAccessUser());
				relationalContext.setAccessUserPassword(contextToUse.getAccessUserPassword());
			} else {
				address = addressProvider.provideAddressForNewTenant(Objects.toString(model.get("address"), null));
				createDatabase(address, model, tenantInfo, relationalContext);
				databaseName = getDatabaseName(tenantInfo);
			}
			relationalContext.setServerAddress(address);
			relationalContext.setDatabaseName(databaseName);

			createXaDatasource(controller, address, databaseName, model, dsName);
			relationalContext.setDatasourceName(dsName);

		} else {
			// for default tenant read configurations for DS
			address = getDataSourceAddress(dsName, dbDialect.get());
			relationalContext.setServerAddress(address);
			relationalContext.setDatasourceName(dsName);
			String database = getXaDataSourceDatabase(controller, dsName);
			relationalContext.setDatabaseName(database);
		}
	}

	private void createDatabase(URI address, Map<String, Serializable> model, TenantInfo tenantInfo,
			TenantRelationalContext relationalContext) throws RollbackedException {
		String name = getDatabaseName(tenantInfo);
		String jdbcURL = toDbAddressUrl(address);
		String dbUserName = getDatabaseName(tenantInfo);

		String groupName = dbUserName + "_group";
		String password = createUserPasswordForTenantId(tenantInfo);

		addUserAndPassword(model, dbUserName, password);

		dbProvisioning.createDatabase(jdbcURL, name, dbUser.get(), dbPass.get(), groupName, dbUserName, password);

		relationalContext.setAccessUser(dbUserName);
		relationalContext.setAccessUserPassword(password);
		relationalContext.setDatabaseName(name);
	}

	/**
	 * Rollback Camunda db provisioning.
	 *
	 * @param relationalContext
	 *            the relational context that was used for creation
	 * @param tenantInfo
	 *            the currently rollbacked tenant info
	 * @param allowDatabaseDrop
	 *            is database drop allowed
	 */
	public void rollback(TenantRelationalContext relationalContext, TenantInfo tenantInfo, boolean allowDatabaseDrop) {

		if (!SecurityContext.isDefaultTenant(tenantInfo.getTenantId())) {
			try {
				if (relationalContext.getDatasourceName() != null) {
					removeDatasource(controller, relationalContext.getDatasourceName());
				}
			} catch (Exception e) {
				LOGGER.warn("Could not remove datasource for tenant {} due to error", tenantInfo.getTenantId(), e);
			}
			// this is not to drop the database if reused
			if (allowDatabaseDrop && relationalContext.getDatabaseName() != null) {
				try {
					String databaseName = relationalContext.getDatabaseName();
					String superUserName = dbUser.get();
					dbProvisioning.dropDatabase(databaseName, relationalContext.getAccessUser(),
							relationalContext.getServerAddress(), superUserName, dbPass.get());
				} catch (Exception e) {
					LOGGER.warn("Could not rollback database creation for tenant {} due to error",
							tenantInfo.getTenantId(), e);
				}
			}
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

	private String getDatabaseName(TenantInfo tenantInfo) {
		return getDsName(tenantInfo).replaceAll("[\\.-]+", "_");
	}
}
