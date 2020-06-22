package com.sirmaenterprise.sep.bpm.camunda.configuration;

import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.configuration.ConfigurationException;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationGroupDefinition;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.ConverterContext;
import com.sirma.itt.seip.configuration.convert.ConverterException;
import com.sirma.itt.seip.configuration.convert.GroupConverterContext;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Configuration provider for Camunda related configurations. Provides base configurations for db
 * access, process engine access and so on. Some of the configurations could be accessed only in
 * tenant mode, otherwise exception will be generated.
 *
 * @author bbanchev
 */
@Singleton
public class CamundaConfigurationImpl implements CamundaConfiguration {

	@ConfigurationPropertyDefinition(sensitive = true, name = "processes.camunda.datasource.name", label = "Tenant aware datasource name for camunda.")
	private static final String DATASOURCE_NAME = "processes.camunda.datasource.name";
	@Inject
	@Configuration(DATASOURCE_NAME)
	private ConfigurationProperty<String> dsName;

	@ConfigurationPropertyDefinition(name = "processes.camunda.engine.name", label = "Tenant aware engine name for Camunda.")
	private static final String ENGINE_NAME = "processes.camunda.engine.name";
	@ConfigurationPropertyDefinition(defaultValue = "postgresql", label = "Default Camunda database provider dialect.", system = true, sensitive = true)
	private static final String CAMUNDA_DATABASE_DIALECT = "camunda.db.dialect";
	@ConfigurationPropertyDefinition(defaultValue = "5432", sensitive = true, type = Integer.class, label = "Port number for the camunda database")
	private static final String CAMUNDA_DATABASE_PORT = "camunda.db.port";
	@ConfigurationPropertyDefinition(defaultValue = "localhost", sensitive = true, label = "Host name for the camunda database")
	private static final String CAMUNDA_DATABASE_HOST = "camunda.db.host";

	@ConfigurationGroupDefinition(properties = { CAMUNDA_DATABASE_HOST, CAMUNDA_DATABASE_PORT,
			CAMUNDA_DATABASE_DIALECT }, type = URI.class)
	private static final String DATABASE_ADDRESS = "camunda.db.address";
	@ConfigurationPropertyDefinition(sensitive = true, label = "Camunda database name")
	private static final String CAMUNDA_DATABASE_NAME = "camunda.db.name";
	@ConfigurationPropertyDefinition(sensitive = true, label = "Admin username for the camunda database")
	private static final String CAMUNDA_DATABASE_ADMIN_USERNAME = "camunda.db.admin.username";
	@ConfigurationPropertyDefinition(sensitive = true, label = "Admin password for the camunda database")
	private static final String CAMUNDA_DATABASE_ADMIN_PASSWORD = "camunda.db.admin.password"; //NOSONAR

	@ConfigurationPropertyDefinition(defaultValue = "3",type = Integer.class, sensitive = true, label = "Indicates the maximum time in minutes a connection may be idle before being closed")
	private static final String CAMUNDA_IDLE_TIMEOUT_MINUTES = "camunda.db.connection.timeout.idleTimeoutMinutes";
	@ConfigurationPropertyDefinition(defaultValue = "60",type = Integer.class, sensitive = true, label = "Common query timeout for all connections if nothing else is set")
	private static final String CAMUNDA_QUERY_TIMEOUT = "camunda.db.connection.timeout.queryTimeout";
	@ConfigurationPropertyDefinition(defaultValue = "4", type = Integer.class, sensitive = true, label = "How may times to try to reconnect to the database before failing")
	private static final String CAMUNDA_ALLOCATION_RETRY = "camunda.db.connection.timeout.allocationRetry";
	@ConfigurationPropertyDefinition(defaultValue = "2500", type = Integer.class, sensitive = true, label = "How much time to wait before trying to reconnect to the database")
	private static final String CAMUNDA_ALLOCATION_RETRY_WAIT = "camunda.db.connection.timeout.allocationRetryWaitMillis";

	@ConfigurationGroupDefinition(properties = { CAMUNDA_QUERY_TIMEOUT, CAMUNDA_ALLOCATION_RETRY,
			CAMUNDA_ALLOCATION_RETRY_WAIT }, type = ConnectionTimeoutSettings.class)
	private static final String CAMUNDA_CONNECTION_TIMEOUT = "camunda.db.connection.timeout";

	@ConfigurationPropertyDefinition(defaultValue = "org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLValidConnectionChecker", sensitive = true, label = "Vendor specific implementation to check if connection is valid")
	private static final String CAMUNDA_VALID_CONNECTION_CHECKER = "camunda.db.connection.validation.validConnectionChecker";
	@ConfigurationPropertyDefinition(defaultValue = "org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLExceptionSorter", sensitive = true, label = "Vendor specific implementation to check if exception is fatal and the connection cannot be used")
	private static final String CAMUNDA_EXCEPTION_SORTER = "camunda.db.connection.validation.exceptionSorter";
	@ConfigurationPropertyDefinition(defaultValue = "true", type = Boolean.class, sensitive = true, label = "Enables connection validation before returning a connection from the connection pool. Cannot be enabled if background validation is enabled")
	private static final String CAMUNDA_VALIDATE_ON_MATCH = "camunda.db.connection.validation.validateOnMatch";
	@ConfigurationPropertyDefinition(defaultValue = "0", type = Integer.class, sensitive = true, label = "If value greater than 0 is set enables background validation. Value will be ignored until validate on match is enabled!")
	private static final String CAMUNDA_BACKGROUND_VALIDATION_MILLIS = "camunda.db.connection.validation.backgroundValidationMillis";
	@ConfigurationPropertyDefinition(defaultValue = "false", type = Boolean.class, sensitive = true, label = "Whether fail a connection allocation on the first connection if it is invalid (true) or keep trying until the pool is exhausted of all potential connections (false) default false")
	private static final String CAMUNDA_USE_FAST_FAIL = "camunda.db.connection.validation.useFastFail";
	@ConfigurationPropertyDefinition(defaultValue = "SELECT 1", sensitive = true, label = "Query to use to check if connection is valid. Not applicable if connection checker is set")
	private static final String CAMUNDA_CHECK_VALID_CONNECTION_SQL = "camunda.db.connection.validation.checkValidConnectionSql";

	@ConfigurationGroupDefinition(
			properties = { CAMUNDA_VALID_CONNECTION_CHECKER, CAMUNDA_EXCEPTION_SORTER, CAMUNDA_VALIDATE_ON_MATCH,
					CAMUNDA_BACKGROUND_VALIDATION_MILLIS, CAMUNDA_USE_FAST_FAIL, CAMUNDA_CHECK_VALID_CONNECTION_SQL },
			type = ConnectionValidationSettings.class)
	private static final String CAMUNDA_CONNECTION_VALIDATION = "camunda.db.connection.validation";

	@ConfigurationPropertyDefinition(defaultValue = "5", type = Integer.class, sensitive = true, label = "The minimum number of connections a pool should hold")
	private static final String CAMUNDA_MIN_POOL_SIZE = "camunda.db.connection.pool.minPoolSize";
	@ConfigurationPropertyDefinition(defaultValue = "5", type = Integer.class, sensitive = true, label = "The initial number of connections a pool should hold")
	private static final String CAMUNDA_INITIAL_POOL_SIZE = "camunda.db.connection.pool.initialPoolSize";
	@ConfigurationPropertyDefinition(defaultValue = "100", type = Integer.class, sensitive = true, label = "the maximum number of connections for a pool. No more connections will be created in each sub-pool")
	private static final String CAMUNDA_MAX_POOL_SIZE = "camunda.db.connection.pool.maxPoolSize";
	@ConfigurationPropertyDefinition(defaultValue = "true", type = Boolean.class, sensitive = true, label = "Whether to attempt to prefill the connection pool")
	private static final String CAMUNDA_PREFILL_POOL = "camunda.db.connection.pool.prefill";
	@ConfigurationPropertyDefinition(defaultValue = "false", type = Boolean.class, sensitive = true, label = "Define if the relational.db.connection.pool.minPoolSize should be considered a strictly")
	private static final String CAMUNDA_USE_STRICT_MIN = "camunda.db.connection.pool.useStrictMin";
	@ConfigurationPropertyDefinition(defaultValue = "InvalidIdleConnections", sensitive = true, label = "Specifies how the pool should be flush in case of an error. Valid values are: FailingConnectionOnly (default), InvalidIdleConnections, IdleConnections, Gracefully, EntirePool, AllInvalidIdleConnections, AllIdleConnections, AllGracefully, AllConnections")
	private static final String CAMUNDA_POOL_FLUSH_STRATEGY = "camunda.db.connection.pool.flushStrategy";

	@ConfigurationGroupDefinition(
			properties = { CAMUNDA_MIN_POOL_SIZE, CAMUNDA_INITIAL_POOL_SIZE, CAMUNDA_MAX_POOL_SIZE, CAMUNDA_PREFILL_POOL,
					CAMUNDA_USE_STRICT_MIN, CAMUNDA_POOL_FLUSH_STRATEGY }, type = ConnectionPoolSettings.class)
	private static final String CAMUNDA_CONNECTION_POOLING = "camunda.db.connection.pool";

	@Inject
	@Configuration(DATABASE_ADDRESS)
	private ConfigurationProperty<URI> address;

	@Inject
	@Configuration(CAMUNDA_DATABASE_ADMIN_USERNAME)
	private ConfigurationProperty<String> camundaAdminUsername;

	@Inject
	@Configuration(CAMUNDA_DATABASE_ADMIN_PASSWORD)
	private ConfigurationProperty<String> camundaAdminPassword;

	@Inject
	@Configuration(CAMUNDA_DATABASE_HOST)
	private ConfigurationProperty<String> camundaDatabaseHost;

	@Inject
	@Configuration(CAMUNDA_DATABASE_PORT)
	private ConfigurationProperty<Integer> camundaDatabasePort;

	@Inject
	@Configuration(CAMUNDA_DATABASE_NAME)
	private ConfigurationProperty<String> camundaDatabaseName;

	@Inject
	@Configuration(ENGINE_NAME)
	private ConfigurationProperty<String> engineName;

	@Inject
	@Configuration(CAMUNDA_DATABASE_DIALECT)
	private ConfigurationProperty<String> camundaDatabaseDialect;

	@Inject
	@Configuration(CAMUNDA_CONNECTION_POOLING)
	private ConfigurationProperty<ConnectionPoolSettings> connectionPoolSettings;

	@Inject
	@Configuration(CAMUNDA_CONNECTION_TIMEOUT)
	private ConfigurationProperty<ConnectionTimeoutSettings> connectionTimeoutSettings;

	@Inject
	@Configuration(CAMUNDA_CONNECTION_VALIDATION)
	private ConfigurationProperty<ConnectionValidationSettings> connectionValidationSettings;


	@ConfigurationConverter(DATABASE_ADDRESS)
	static URI buildDbAddress(GroupConverterContext context) {
		String dialect = context.get(CAMUNDA_DATABASE_DIALECT);
		String host = context.get(CAMUNDA_DATABASE_HOST);
		Integer port = context.get(CAMUNDA_DATABASE_PORT);

		try {
			return new URI("jdbc:" + dialect, null, host, port.intValue(), null, null, null);
		} catch (URISyntaxException e) {
			throw new ConverterException(e);
		}
	}
	
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

	@ConfigurationConverter(CAMUNDA_CONNECTION_TIMEOUT)
	static ConnectionTimeoutSettings buildConnectionTimeoutSettings(GroupConverterContext context) {
		ConnectionTimeoutSettings settings = new ConnectionTimeoutSettings();
		settings.setAllocationRetry(context.get(CAMUNDA_ALLOCATION_RETRY));
		settings.setIdleTimeoutMinutes(context.get(CAMUNDA_IDLE_TIMEOUT_MINUTES));
		settings.setQueryTimeout(context.get(CAMUNDA_QUERY_TIMEOUT));
		settings.setAllocationRetryWaitMillis(context.get(CAMUNDA_ALLOCATION_RETRY_WAIT));
		return settings;
	}

	@ConfigurationConverter(CAMUNDA_CONNECTION_VALIDATION)
	static ConnectionValidationSettings buildConnectionValidationSettings(GroupConverterContext context) {
		ConnectionValidationSettings settings = new ConnectionValidationSettings();
		settings.setValidateOnMatch(context.get(CAMUNDA_VALIDATE_ON_MATCH));
		if (!settings.isValidateOnMatch()) {
			settings.setBackgroundValidationMillis(context.get(CAMUNDA_BACKGROUND_VALIDATION_MILLIS));
		}
		settings.setExceptionSorter(context.get(CAMUNDA_EXCEPTION_SORTER));
		settings.setCheckValidConnectionSql(context.get(CAMUNDA_CHECK_VALID_CONNECTION_SQL));
		settings.setValidConnectionChecker(context.get(CAMUNDA_VALID_CONNECTION_CHECKER));
		settings.setUseFastFail(context.get(CAMUNDA_USE_FAST_FAIL));
		return settings;
	}

	@ConfigurationConverter(CAMUNDA_CONNECTION_POOLING)
	static ConnectionPoolSettings buildConnectionPoolSettings(GroupConverterContext context) {
		ConnectionPoolSettings settings = new ConnectionPoolSettings();
		settings.setFlushStrategy(PoolFlushStrategy.parse(context.get(CAMUNDA_POOL_FLUSH_STRATEGY)));
		settings.setInitialPoolSize(context.get(CAMUNDA_INITIAL_POOL_SIZE));
		settings.setMinPoolSize(context.get(CAMUNDA_MIN_POOL_SIZE));
		settings.setMaxPoolSize(context.get(CAMUNDA_MAX_POOL_SIZE));
		settings.setUseStrictMin(context.get(CAMUNDA_USE_STRICT_MIN));
		settings.setPrefill(context.get(CAMUNDA_PREFILL_POOL));
		return settings;
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

	@Override
	public ConfigurationProperty<String> getAdminUsernameConfiguration() {
		return camundaAdminUsername;
	}

	@Override
	public ConfigurationProperty<String> getAdminPasswordConfiguration() {
		return camundaAdminPassword;
	}

	@Override
	public ConfigurationProperty<String> getDatabaseHostConfiguration() {
		return camundaDatabaseHost;
	}

	@Override
	public ConfigurationProperty<String> getDatabaseNameConfiguration() {
		return camundaDatabaseName;
	}

	@Override
	public ConfigurationProperty<Integer> getDatabasePortConfiguration() {
		return camundaDatabasePort;
	}

	@Override
	public ConfigurationProperty<String> getDatabaseDialectConfiguration() {
		return camundaDatabaseDialect;
	}

	@Override
	public ConfigurationProperty<URI> getDatabaseUri() {
		return address;
	}

	@Override
	public ConfigurationProperty<ConnectionPoolSettings> getConnectionPoolSettings() {
		return connectionPoolSettings;
	}

	@Override
	public ConfigurationProperty<ConnectionTimeoutSettings> getConnectionTimeoutSettings() {
		return connectionTimeoutSettings;
	}

	@Override
	public ConfigurationProperty<ConnectionValidationSettings> getConnectionValidationSettings() {
		return connectionValidationSettings;
	}

}
