package com.sirma.itt.seip.tenant.audit;

import static com.sirma.itt.seip.tenant.db.DbProvisioning.createUserNameFromTenantId;
import static com.sirma.itt.seip.tenant.db.DbProvisioning.createUserPasswordForTenantId;
import static com.sirma.itt.seip.tenant.db.DbProvisioning.toDbAddressUrl;
import static com.sirma.itt.seip.wildfly.cli.db.WildFlyDatasourceProvisioning.addUserAndPassword;
import static com.sirma.itt.seip.wildfly.cli.db.WildFlyDatasourceProvisioning.createXaDatasource;
import static com.sirma.itt.seip.wildfly.cli.db.WildFlyDatasourceProvisioning.getXaDataSourceDatabase;
import static com.sirma.itt.seip.wildfly.cli.db.WildFlyDatasourceProvisioning.removeDatasource;

import java.io.Serializable;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.audit.configuration.AuditConfiguration;
import com.sirma.itt.emf.audit.patch.AuditDbPatchService;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.db.Configuration;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.db.BaseRelationalDbProvisioning;
import com.sirma.itt.seip.tenant.db.DbProvisioning;
import com.sirma.itt.seip.tenant.db.TenantRelationalContext;
import com.sirma.itt.seip.tenant.wizard.SubsystemTenantAddressProvider;
import com.sirma.itt.seip.util.CDI;

/**
 * The AuditDbProvisioning is service that creates/manipulates all related audit db operation for tenant.
 *
 * @author BBonev
 */
@ApplicationScoped
public class AuditDbProvisioning extends BaseRelationalDbProvisioning {
	private static final String AUDIT_SUFFIX = "_audit";
	private static final Logger LOGGER = LoggerFactory.getLogger(AuditDbProvisioning.class);

	private SubsystemTenantAddressProvider addressProvider;

	/** The db dialect. */
	@Inject
	@com.sirma.itt.seip.configuration.annotation.Configuration
	@ConfigurationPropertyDefinition(name = "system.db_audit.dialect", defaultValue = "postgresql", label = "Default audit database provider dialect.", system = true, sensitive = true)
	protected ConfigurationProperty<String> dbDialect;
	/** The db user. */
	@Inject
	@com.sirma.itt.seip.configuration.annotation.Configuration
	@ConfigurationPropertyDefinition(name = "system.db_audit.user", defaultValue = "admin", label = "Default host user for audit database.", system = true, sensitive = true)
	protected ConfigurationProperty<String> dbUser;
	/** The db pass. */
	@Inject
	@com.sirma.itt.seip.configuration.annotation.Configuration
	@ConfigurationPropertyDefinition(name = "system.db_audit.pass", defaultValue = "admin", label = "Default host password for audit database.", system = true, sensitive = true)
	protected ConfigurationProperty<String> dbPass;

	@Inject
	private AuditDbPatchService patchDbService;

	@Inject
	private AuditConfiguration auditConfiguration;

	@Inject
	private DbProvisioning dbProvisioning;

	@Override
	@PostConstruct
	protected void initialize() {
		super.initialize();

		addressProvider = CDI.instantiateBean(dbDialect.get() + AUDIT_SUFFIX, SubsystemTenantAddressProvider.class,
				beanManager);
		if (addressProvider == null) {
			throw new EmfRuntimeException("Could not find address provider for " + dbDialect.get());
		}
	}

	/**
	 * Provision the model.
	 *
	 * @param model
	 *            the model
	 * @param tenantInfo
	 *            the tenant info
	 * @param contextToUse
	 *            the context to use
	 * @param relationalContext
	 *            the relational context
	 * @throws RollbackedException
	 *             on provision error
	 */
	public void provision(Map<String, Serializable> model, TenantInfo tenantInfo, TenantRelationalContext contextToUse,
			TenantRelationalContext relationalContext) throws RollbackedException {
		try {
			provisionInternal(model, tenantInfo, contextToUse, relationalContext);

			insertConfigurations(relationalContext, tenantInfo);
		} catch (Exception e) {
			throw new RollbackedException(e);
		}
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

			patchDatabase(tenantInfo);
		} else {
			// for default tenant read configurations for DS
			address = getDataSourceAddress(dsName, dbDialect.get());
			relationalContext.setServerAddress(address);
			relationalContext.setDatasourceName(dsName);
			String database = getXaDataSourceDatabase(controller, dsName);
			relationalContext.setDatabaseName(database);
		}
	}

	private void patchDatabase(TenantInfo tenantInfo) throws RollbackedException {
		securityContextManager.initializeTenantContext(tenantInfo.getTenantId());
		try {
			patchDbService.patchAuditDatabase(auditConfiguration);
		} finally {
			securityContextManager.endContextExecution();
		}
	}

	private void insertConfigurations(TenantRelationalContext context, TenantInfo tenantInfo) {
		URI address = context.getServerAddress();
		String dbName = context.getDatabaseName();
		securityContextManager.initializeTenantContext(tenantInfo.getTenantId());
		try {
			Configuration host = new Configuration(auditConfiguration.getDatabaseAddressConfiguration(),
					address.getHost(), tenantInfo.getTenantId());
			Configuration port = new Configuration(auditConfiguration.getDatabasePortConfiguration(),
					Integer.toString(address.getPort()), tenantInfo.getTenantId());
			Configuration dialect = new Configuration(auditConfiguration.getDatabaseDialectConfiguration(),
					dbDialect.get(), tenantInfo.getTenantId());
			Configuration name = new Configuration(auditConfiguration.getDatabaseNameConfiguration(), dbName,
					tenantInfo.getTenantId());
			configurationManagement.addConfigurations(Arrays.asList(host, port, dialect, name));
		} finally {
			securityContextManager.endContextExecution();
		}
	}

	/**
	 * Creates the database.
	 *
	 * @param address
	 *            the address
	 * @param model
	 *            the model
	 * @param tenantInfo
	 *            the tenant info
	 * @param relationalContext
	 *            the relational context
	 * @throws RollbackedException
	 *             the rollbacked exception
	 */
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
	 * Rollback audit db creation.
	 *
	 * @param relationalContext
	 *            the relational context
	 * @param tenantInfo
	 *            the tenant info
	 * @param allowDatabaseDrop
	 *            the allow database drop
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
		try {
			removeConfigurations(tenantInfo);
		} catch (Exception e) {
			LOGGER.warn("Could not rollback database configurations for tenant {} due to error",
					tenantInfo.getTenantId(), e);
		}
	}

	private void removeConfigurations(TenantInfo tenantInfo) {
		securityContextManager.initializeTenantContext(tenantInfo.getTenantId());
		try {
			configurationManagement.removeConfiguration(auditConfiguration.getDatabaseAddressConfiguration());
			configurationManagement.removeConfiguration(auditConfiguration.getDatabasePortConfiguration());
			configurationManagement.removeConfiguration(auditConfiguration.getDatabaseDialectConfiguration());
			configurationManagement.removeConfiguration(auditConfiguration.getDatabaseNameConfiguration());
		} finally {
			securityContextManager.endContextExecution();
		}
	}

	private static String getDsName(TenantInfo tenantInfo) {
		return tenantInfo.getTenantId() + AUDIT_SUFFIX;
	}

	private static String getDatabaseName(TenantInfo tenantInfo) {
		return createUserNameFromTenantId(tenantInfo) + AUDIT_SUFFIX;
	}

}
