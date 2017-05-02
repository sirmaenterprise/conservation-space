package com.sirma.itt.seip.tenant.db;

import static com.sirma.itt.seip.tenant.db.DbProvisioning.createUserNameFromTenantId;
import static com.sirma.itt.seip.tenant.db.DbProvisioning.createUserPasswordForTenantId;
import static com.sirma.itt.seip.tenant.db.DbProvisioning.toDbAddressUrl;
import static com.sirma.itt.seip.wildfly.cli.db.WildFlyDatasourceProvisioning.addUserAndPassword;
import static com.sirma.itt.seip.wildfly.cli.db.WildFlyDatasourceProvisioning.createXaDatasource;
import static com.sirma.itt.seip.wildfly.cli.db.WildFlyDatasourceProvisioning.removeDatasource;

import java.io.Serializable;
import java.net.URI;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.db.Configuration;
import com.sirma.itt.seip.db.DatabaseConfiguration;
import com.sirma.itt.seip.db.patch.PatchService;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.wizard.SubsystemTenantAddressProvider;
import com.sirma.itt.seip.util.CDI;

/**
 * The RelationDbProvisioning is service that creates/manipulates all related db operation for tenant.
 *
 * @author bbanchev
 * @author BBonev
 */
@ApplicationScoped
public class RelationDbProvisioning extends BaseRelationalDbProvisioning {
	private static final Logger LOGGER = LoggerFactory.getLogger(RelationDbProvisioning.class);
	/** The db dialect. */
	@Inject
	@com.sirma.itt.seip.configuration.annotation.Configuration
	@ConfigurationPropertyDefinition(name = "system.db.dialect", defaultValue = "postgresql", label = "Default database provider dialect.", system = true, sensitive = true)
	protected ConfigurationProperty<String> dbDialect;
	/** The db user. */
	@Inject
	@com.sirma.itt.seip.configuration.annotation.Configuration
	@ConfigurationPropertyDefinition(name = "system.db.user", defaultValue = "admin", label = "Default host user for database.", system = true, sensitive = true)
	protected ConfigurationProperty<String> dbUser;
	/** The db pass. */
	@Inject
	@com.sirma.itt.seip.configuration.annotation.Configuration
	@ConfigurationPropertyDefinition(name = "system.db.pass", defaultValue = "admin", label = "Default host passwordd for database.", system = true, sensitive = true)
	protected ConfigurationProperty<String> dbPass;

	@Inject
	private PatchService patchDbService;

	private SubsystemTenantAddressProvider addressProvider;

	@Inject
	private DatabaseConfiguration databaseConfiguration;

	@Inject
	private DbProvisioning dbProvisioning;

	@Override
	@PostConstruct
	protected void initialize() {
		super.initialize();

		addressProvider = CDI.instantiateBean(dbDialect.get(), SubsystemTenantAddressProvider.class, beanManager);
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
	 * @param context
	 *            the context
	 * @throws RollbackedException
	 *             the rollbacked exception on patch error
	 */
	public void provision(Map<String, Serializable> model, TenantInfo tenantInfo, TenantRelationalContext context)
			throws RollbackedException {
		if (!SecurityContext.isDefaultTenant(tenantInfo.getTenantId())) {
			URI address = addressProvider.provideAddressForNewTenant(Objects.toString(model.get("address"), null));
			createDatabase(address, model, tenantInfo, context);
			context.setServerAddress(address);

			createXaDatasource(controller, address, getDatabaseName(tenantInfo), model, tenantInfo.getTenantId());

			context.setDatasourceName(tenantInfo.getTenantId());

			patchDatabase(tenantInfo);
		} else {
			URI address = getDataSourceAddress(tenantInfo.getTenantId(), dbDialect.get());
			context.setServerAddress(address);
		}
		insertConfigurations(context, tenantInfo);
	}

	private void patchDatabase(TenantInfo tenantInfo) throws RollbackedException {
		securityContextManager.initializeTenantContext(tenantInfo.getTenantId());
		try {
			patchDbService.patchSchema();
		} finally {
			securityContextManager.endContextExecution();
		}
	}

	private void insertConfigurations(TenantRelationalContext context, TenantInfo tenantInfo) {
		URI address = context.getServerAddress();
		String dbName = context.getDatabaseName();
		securityContextManager.initializeTenantContext(tenantInfo.getTenantId());
		try {
			Configuration host = new Configuration(databaseConfiguration.getDatabaseAddressConfiguration(),
					address.getHost(), tenantInfo.getTenantId());
			Configuration port = new Configuration(databaseConfiguration.getDatabasePortConfiguration(),
					Integer.toString(address.getPort()), tenantInfo.getTenantId());
			Configuration dialect = new Configuration(databaseConfiguration.getDatabaseDialectConfiguration(),
					dbDialect.get(), tenantInfo.getTenantId());
			Configuration name = new Configuration(databaseConfiguration.getDatabaseNameConfiguration(), dbName,
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
	 *            is the db address
	 * @param model
	 *            the model of data to use for creation - db name, etc.
	 * @param tenantInfo
	 *            is the tenant context info
	 * @param context
	 *            is db creation context
	 * @throws SQLException
	 *             the SQL exception
	 */
	private void createDatabase(URI address, Map<String, Serializable> model, TenantInfo tenantInfo,
			TenantRelationalContext context) throws RollbackedException {
		String name = getDatabaseName(tenantInfo);
		String jdbcURL = toDbAddressUrl(address);
		String dbUserName = getDatabaseName(tenantInfo);

		String groupName = dbUserName + "_group";
		String password = createUserPasswordForTenantId(tenantInfo);
		addUserAndPassword(model, dbUserName, password);

		dbProvisioning.createDatabase(jdbcURL, name, dbUser.get(), dbPass.get(), groupName, dbUserName, password);

		context.setAccessUser(dbUserName);
		context.setAccessUserPassword(password);
		context.setDatabaseName(name);
	}

	private static String getDatabaseName(TenantInfo tenantInfo) {
		return createUserNameFromTenantId(tenantInfo);
	}

	/**
	 * Rollback database creation.
	 *
	 * @param relationalContext
	 *            the relational context
	 * @param tenantInfo
	 *            the tenant info
	 */
	public void rollback(TenantRelationalContext relationalContext, TenantInfo tenantInfo) {
		try {
			removeConfigurations(tenantInfo);
		} catch (Exception e) {
			LOGGER.warn("Could not rollback database configurations for tenant {} due to error",
					tenantInfo.getTenantId(), e);
		}
		if (relationalContext == null) {
			return;
		}
		if (!SecurityContext.isDefaultTenant(tenantInfo.getTenantId())) {
			try {
				if (relationalContext.getDatasourceName() != null) {
					removeDatasource(controller, relationalContext.getDatasourceName());
				}
			} catch (Exception e) {
				LOGGER.warn("Could not remove datasource for tenant {} due to error", tenantInfo.getTenantId(), e);
			}
			try {
				if (relationalContext.getServerAddress() != null) {
					dbProvisioning.dropDatabase(relationalContext.getDatabaseName(), relationalContext.getAccessUser(),
							relationalContext.getServerAddress(), dbUser.get(), dbPass.get());
				}
			} catch (Exception e) {
				LOGGER.warn("Could not rollback database creation for tenant {} due to error", tenantInfo.getTenantId(),
						e);
			}
		}
	}

	private void removeConfigurations(TenantInfo tenantInfo) {
		securityContextManager.initializeTenantContext(tenantInfo.getTenantId());
		try {
			configurationManagement.removeConfiguration(databaseConfiguration.getDatabaseAddressConfiguration());
			configurationManagement.removeConfiguration(databaseConfiguration.getDatabasePortConfiguration());
			configurationManagement.removeConfiguration(databaseConfiguration.getDatabaseDialectConfiguration());
			configurationManagement.removeConfiguration(databaseConfiguration.getDatabaseNameConfiguration());
		} finally {
			securityContextManager.endContextExecution();
		}
	}

}
