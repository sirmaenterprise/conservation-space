package com.sirma.itt.seip.db;

import java.lang.invoke.MethodHandles;
import java.util.Objects;

import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.exception.RollbackedException;

/**
 * Datasource provisioner that recreates the datasource from set configurations if not found.
 *
 * @author nvelkov
 */
public class ConfigurationDatasourceProvisioner {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private DatasourceProvisioner provisioner;

	/**
	 * Lookup data source using jndi context. If the data source is not found, try to recreate it if the correct
	 * configurations are set.
	 *
	 * @param jndiName
	 *            the jndi name
	 * @param settingsSource
	 *            the settings used to recreate the datasource if not found
	 * @return the data source
	 */
	public DataSource lookupDataSource(String jndiName, DatabaseSettings settingsSource) {
		try {
			InitialContext context = new InitialContext();
			return (DataSource) context.lookup(jndiName);
		} catch (NamingException e) {
			String dsName = jndiName.substring(jndiName.lastIndexOf('/') + 1);
			LOGGER.trace("Couldn't lookup datasource for " + dsName + ". Attempting to recreate it.", e);
			DatasourceModel model = new DatasourceModel();
			model.setPoolName("pool_" + dsName);
			model.setDriverName(settingsSource.getDatabaseDialectConfiguration().get());
			model.setJndiName(jndiName);
			model.setUsername(settingsSource.getAdminUsernameConfiguration().get());
			model.setPassword(settingsSource.getAdminPasswordConfiguration().get());
			model.setDatabaseHost(settingsSource.getDatabaseHostConfiguration().get());
			model.setDatabasePort(settingsSource.getDatabasePortConfiguration().get());
			model.setDatabaseName(settingsSource.getDatabaseNameConfiguration().get());
			model.setDatasourceName(dsName);

			DatabaseSettings.ConnectionPoolSettings poolSettings = settingsSource.getConnectionPoolSettings()
					.computeIfNotSet(DatabaseSettings.ConnectionPoolSettings::new);
			model.setMinPoolSize(poolSettings.getMinPoolSize());
			model.setMaxPoolSize(poolSettings.getMaxPoolSize());
			model.setInitialPoolSize(poolSettings.getInitialPoolSize());
			model.setUseStrictMin(poolSettings.isUseStrictMin());
			model.setFlushStrategy(Objects.toString(poolSettings.getFlushStrategy(), null));
			model.setPrefill(poolSettings.isPrefill());

			DatabaseSettings.ConnectionTimeoutSettings timeoutSettings = settingsSource.getConnectionTimeoutSettings()
					.computeIfNotSet(DatabaseSettings.ConnectionTimeoutSettings::new);
			model.setAllocationRetries(timeoutSettings.getAllocationRetry());
			model.setAllocationRetryWaitMillis(timeoutSettings.getAllocationRetryWaitMillis());
			model.setQueryTimeoutSeconds(timeoutSettings.getQueryTimeout());
			model.setIdleTimeoutMinutes(timeoutSettings.getIdleTimeoutMinutes());

			DatabaseSettings.ConnectionValidationSettings validationSettings = settingsSource.getConnectionValidationSettings()
					.computeIfNotSet(DatabaseSettings.ConnectionValidationSettings::new);
			model.setValidConnectionSQL(validationSettings.getCheckValidConnectionSql());
			model.setValidateOnMatch(validationSettings.isValidateOnMatch());
			model.setValidConnectionChecker(validationSettings.getValidConnectionChecker());
			model.setExceptionSorter(validationSettings.getExceptionSorter());
			model.setBackgroundValidation(validationSettings.isBackgroundValidation());
			model.setBackgroundValidationMillis(validationSettings.getBackgroundValidationMillis());
			model.setUseFastFail(validationSettings.isUseFastFail());

			try {
				provisioner.createXaDatasource(model);
				return lookupDataSource(jndiName, settingsSource);
			} catch (RollbackedException e1) {
				LOGGER.error("Couldn't recreate datasource from provided parameters", e1);
			}
		}
		return null;
	}

}
