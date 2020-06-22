package com.sirma.itt.seip.tenant.db;

import static com.sirma.itt.seip.tenant.db.DbProvisioning.createUserPasswordForTenantId;
import static com.sirma.itt.seip.tenant.db.DbProvisioning.getModel;
import static com.sirma.itt.seip.tenant.db.DbProvisioning.toDbAddressUrl;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.db.Configuration;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.db.DatabaseSettings;
import com.sirma.itt.seip.db.DatasourceModel;
import com.sirma.itt.seip.db.DatasourceProvisioner;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.wizard.SubsystemTenantAddressProvider;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;
import com.sirma.itt.seip.util.CDI;

/**
 * Provides base functionality required to implement database tenant provisioning. The class
 * provides methods to create and remove of database and datasources in Wildfly server.
 *
 * @author BBonev
 */
public abstract class BaseRelationalDbProvisioning {
	private static final String ADDRESS = "address";

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	protected DatasourceProvisioner datasourceProvisioner;

	@Inject
	protected BeanManager beanManager;

	@Inject
	protected ConfigurationManagement configurationManagement;

	@Inject
	protected SecurityContextManager securityContextManager;

	@Inject
	protected DbProvisioning databaseProvisioner;

	private SubsystemTenantAddressProvider addressProvider;

	@PostConstruct
	protected void initialize() {
		// ensure loaded JDBC driver
		try {
			DriverManager.registerDriver((Driver) Class.forName("org.postgresql.Driver").newInstance());
		} catch (Exception e) {
			LOGGER.warn("Failed to load Driver: org.postgresql.Driver. Probably tenant creation will fail!", e);
		}
		String dialect = getDbDialect() + getSubsystemSuffix();
		addressProvider = CDI.instantiateBean(dialect, SubsystemTenantAddressProvider.class, beanManager);
		if (addressProvider == null) {
			throw new EmfRuntimeException("Could not find address provider for " + dialect);
		}
	}

	/**
	 * Provision the database, datasource and all needed configurations.
	 *
	 * @param model
	 *            the initial model - will be converted to a {@link DatasourceModel}
	 * @param tenantInfo
	 *            the tenant info
	 * @param contextToUse
	 *            the context to use
	 * @param relationalContext
	 *            the relational context - will be initialised based on the datasource model and the
	 *            context to use
	 * @throws RollbackedException
	 *             if an exception occurs during the db creation
	 */
	public void provision(Map<String, Serializable> model, TenantInfo tenantInfo, TenantRelationalContext contextToUse,
			TenantRelationalContext relationalContext) throws RollbackedException {
		DatasourceModel datasourceModel = convertModel(model, tenantInfo, contextToUse);
		initRelationalContext(model, datasourceModel, tenantInfo, contextToUse, relationalContext);
		if (!SecurityContext.isDefaultTenant(tenantInfo.getTenantId())) {
			URI address = addressProvider.provideAddressForNewTenant(Objects.toString(model.get(ADDRESS), null));
			// If the database already exists we don't want to do any of that. The datasource will
			// be provisioned by the datasource restore mechanism and we don't need to insert the
			// configurations anew.
			if (!databaseProvisioner.databaseExists(toDbAddressUrl(address), datasourceModel.getDatabaseName(),
					getDbUser(), getDbPass())) {
				if (contextToUse == null) {
					createDatabase(address, datasourceModel, getDbUser(), getDbPass());
				}

				datasourceProvisioner.createXaDatasource(datasourceModel);

				patchDatabase(tenantInfo);

				insertConfigurations(tenantInfo.getTenantId(), getConfigurations(tenantInfo, relationalContext,
						contextToUse));
			}
		}
	}

	/**
	 * Rollback db creation.
	 *
	 * @param relationalContext
	 *            the relational context
	 * @param tenantInfo
	 *            the tenant info
	 * @param contextToUse
	 *            the context to use
	 * @param allowDatabaseDrop
	 *            the allow database drop
	 */
	public void rollback(TenantRelationalContext relationalContext, TenantRelationalContext contextToUse,
			TenantInfo tenantInfo, boolean allowDatabaseDrop) {
		if (relationalContext == null) {
			return;
		}
		if (!SecurityContext.isDefaultTenant(tenantInfo.getTenantId())) {
			// this is not to drop the database if reused
			if (allowDatabaseDrop && relationalContext.getServerAddress() != null) {
				try {
					String databaseName = getDatabaseName(tenantInfo);
					databaseProvisioner.dropDatabase(databaseName, relationalContext.getAccessUser(),
							relationalContext.getServerAddress(), getDbUser(), getDbPass());
				} catch (Exception e) {
					LOGGER.warn("Could not rollback database creation for tenant {} due to error {}",
							tenantInfo.getTenantId(), e.getMessage());
					LOGGER.trace("Could not rollback database creation for tenant {} due to error",
							tenantInfo.getTenantId(), e);
				}
			}
			try {
				if (relationalContext.getDatasourceName() != null) {
					datasourceProvisioner.removeDatasource(relationalContext.getDatasourceName());
				}
			} catch (Exception e) {
				LOGGER.warn("Could not remove datasource for tenant {} due to error {}", tenantInfo.getTenantId(),
						e.getMessage());
				LOGGER.trace("Could not remove datasource for tenant {} due to error", tenantInfo.getTenantId(), e);
			}
		}
		try {
			removeConfigurations(tenantInfo.getTenantId(), getConfigurations(tenantInfo, relationalContext,
					contextToUse));
		} catch (Exception e) {
			LOGGER.warn("Could not rollback database configurations for tenant {} due to error {}",
					tenantInfo.getTenantId(), e.getMessage());
			LOGGER.trace("Could not rollback database configurations for tenant {} due to error",
					tenantInfo.getTenantId(), e);
		}
	}

	/**
	 * Create the database.
	 *
	 * @param address
	 *            address of the database
	 * @param model
	 *            the datasource model
	 * @param dbUser
	 *            the database user
	 * @param dbPass
	 *            the database pass
	 * @throws RollbackedException
	 *             if an exception occurs while creating
	 */
	private void createDatabase(URI address, DatasourceModel model, String dbUser, String dbPass)
			throws RollbackedException {
		String jdbcURL = toDbAddressUrl(address);
		String databaseUsername = model.getUsername();

		String groupName = databaseUsername + "_group";
		String databasePassword = model.getPassword();

		databaseProvisioner.createDatabase(jdbcURL, databaseUsername, dbUser, dbPass, groupName, databaseUsername,
				databasePassword);
	}

	/**
	 * Convert the provided model to a {@link DatasourceModel}. The datasource model may contain
	 * different data based on if there is a context to use specified.
	 *
	 * @param model
	 *            the initial model
	 * @param tenantInfo
	 *            the tenant info
	 * @param contextToUse
	 *            the context to use
	 * @return the {@link DatasourceModel}
	 */
	private DatasourceModel convertModel(Map<String, Serializable> model, TenantInfo tenantInfo,
			TenantRelationalContext contextToUse) {
		URI address;
		String databaseName;
		String username;
		String password;
		DatasourceModel datasourceModel = getModel(model);
		if (contextToUse != null) {
			address = contextToUse.getServerAddress();
			databaseName = contextToUse.getDatabaseName();
			username = contextToUse.getAccessUser();
			password = contextToUse.getAccessUserPassword();
		} else {
			address = addressProvider.provideAddressForNewTenant(Objects.toString(model.get(ADDRESS), null));
			databaseName = getDatabaseName(tenantInfo);
			username = getDatabaseName(tenantInfo);
			password = createUserPasswordForTenantId(tenantInfo);
		}
		String datasourceName = getDatasourceName(tenantInfo);
		datasourceModel.setUsername(username);
		datasourceModel.setPassword(password);
		datasourceModel.setPoolName("pool_" + datasourceName);
		datasourceModel.setJndiName("java:jboss/datasources/" + datasourceName);
		datasourceModel.setDriverName(address.getScheme());
		datasourceModel.setDatabaseHost(address.getHost());
		datasourceModel.setDatabasePort(address.getPort());
		datasourceModel.setDatabaseName(databaseName);
		datasourceModel.setDatasourceName(datasourceName);
		datasourceModel.setValidateOnMatch(true);
		if ("postgresql".equals(address.getScheme())) {
			datasourceModel.setExceptionSorter("org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLExceptionSorter");
			datasourceModel.setValidConnectionChecker("org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLValidConnectionChecker");
		}
		datasourceModel.setIdleTimeoutMinutes(3);
		datasourceModel.setQueryTimeoutSeconds(60);
		datasourceModel.setAllocationRetries(4);
		datasourceModel.setAllocationRetryWaitMillis(2500);

		datasourceModel.setPrefill(true);
		datasourceModel.setFlushStrategy(DatabaseSettings.PoolFlushStrategy.ALL_INVALID_IDLE_CONNECTIONS.toString());
		datasourceModel.setUseStrictMin(false);
		datasourceModel.setInitialPoolSize(10);
		datasourceModel.setMinPoolSize(10);
		datasourceModel.setMaxPoolSize(100);
		return datasourceModel;
	}

	/**
	 * Init the relational context used for database creation and configuration insertion, based on
	 * whether there is context to use.
	 *
	 * @param model
	 *            the initial model
	 * @param datasourceModel
	 *            the datasource model
	 * @param tenantInfo
	 *            the tenant info
	 * @param contextToUse
	 *            the context to use
	 * @param relationalContext
	 *            the relational context to init
	 */
	private void initRelationalContext(Map<String, Serializable> model, DatasourceModel datasourceModel,
			TenantInfo tenantInfo, TenantRelationalContext contextToUse, TenantRelationalContext relationalContext) {
		URI address;
		String datasourceName = getDatasourceName(tenantInfo);
		if (!SecurityContext.isDefaultTenant(tenantInfo.getTenantId())) {
			String databaseName;
			if (contextToUse != null) {
				address = contextToUse.getServerAddress();
				databaseName = contextToUse.getDatabaseName();
			} else {
				address = addressProvider.provideAddressForNewTenant(Objects.toString(model.get(ADDRESS), null));
				databaseName = getDatabaseName(tenantInfo);
			}
			relationalContext.setAccessUser(datasourceModel.getUsername());
			relationalContext.setAccessUserPassword(datasourceModel.getPassword());
			relationalContext.setServerAddress(address);
			relationalContext.setDatabaseName(databaseName);
			relationalContext.setDatasourceName(datasourceName);
		} else {
			// for default tenant read configurations for DS
			address = getDataSourceAddress(datasourceName, getDbDialect());
			relationalContext.setServerAddress(address);
			relationalContext.setDatasourceName(datasourceName);
			String database = datasourceProvisioner.getXaDataSourceDatabase(datasourceName);
			relationalContext.setDatabaseName(database);
		}
	}

	private void insertConfigurations(String tenantId, Collection<Configuration> configurations) {
		securityContextManager.initializeTenantContext(tenantId);
		try {
			configurationManagement.addConfigurations(configurations);
		} finally {
			securityContextManager.endContextExecution();
		}
	}

	private URI getDataSourceAddress(String dsName, String dialect) {
		String serverName = datasourceProvisioner.getXaDataSourceServerName(dsName);
		String port = datasourceProvisioner.getXaDataSourcePort(dsName);
		if (serverName == null || port == null) {
			throw new TenantCreationException("Could not find XA data source for tenant " + dsName);
		}
		return URI.create(dialect + "://" + serverName + ":" + port);
	}

	private void removeConfigurations(String tenantId, Collection<Configuration> configurations) {
		securityContextManager.initializeTenantContext(tenantId);
		try {
			for (Configuration configuration : configurations) {
				configurationManagement.removeConfiguration(configuration.getConfigurationKey());
			}
		} finally {
			securityContextManager.endContextExecution();
		}
	}

	/**
	 * Get the database user.
	 *
	 * @return the database user
	 */
	protected abstract String getDbUser();

	/**
	 * Get the database password.
	 *
	 * @return the database password
	 */
	protected abstract String getDbPass();

	/**
	 * Get the datasource name.
	 *
	 * @param tenantInfo
	 *            the tenant info
	 * @return the datasource name
	 */
	protected abstract String getDatasourceName(TenantInfo tenantInfo);

	/**
	 * Get the database name.
	 *
	 * @param tenantInfo
	 *            the tenant info
	 * @return the database name
	 */
	protected abstract String getDatabaseName(TenantInfo tenantInfo);

	/**
	 * Get the database dialect.
	 *
	 * @return the database dialect
	 */
	protected abstract String getDbDialect();

	/**
	 * Get the subsystem suffix.
	 *
	 * @return the subsystem suffix
	 */
	protected abstract String getSubsystemSuffix();

	/**
	 * Patch the database.
	 *
	 * @param tenantInfo
	 *            the tenant info
	 * @throws RollbackedException
	 *             if an exception occurs while patching the database
	 */
	protected abstract void patchDatabase(TenantInfo tenantInfo) throws RollbackedException;

	/**
	 * Get the configurations used by the subsystem. The same implementation will be used for
	 * inserting and removing configuration in case of a rollback.
	 *
	 * @param tenantInfo
	 *            the tenant info
	 * @param context
	 *            the context
	 * @param contextToUse
	 *            the context to use
	 * @return the configurations
	 */
	protected abstract Collection<Configuration> getConfigurations(TenantInfo tenantInfo,
			TenantRelationalContext context, TenantRelationalContext contextToUse);
}
