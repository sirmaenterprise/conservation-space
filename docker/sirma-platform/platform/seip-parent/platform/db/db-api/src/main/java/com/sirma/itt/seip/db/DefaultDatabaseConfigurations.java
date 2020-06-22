package com.sirma.itt.seip.db;

import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationGroupDefinition;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.ConverterException;
import com.sirma.itt.seip.configuration.convert.GroupConverterContext;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Default implementation of {@link DatabaseConfiguration}.
 *
 * @author BBonev
 */
@Singleton
class DefaultDatabaseConfigurations implements DatabaseConfiguration {

	@ConfigurationPropertyDefinition(defaultValue = "localhost", sensitive = true, label = "Host name for the operational database")
	private static final String DATABASE_HOST = "relational.db.host";
	@ConfigurationPropertyDefinition(defaultValue = "5432", sensitive = true, type = Integer.class, label = "Port number for the operational database")
	private static final String DATABASE_PORT = "relational.db.port";
	@ConfigurationPropertyDefinition(defaultValue = "postgresql", sensitive = true, label = "Operational database dialect")
	private static final String DATABASE_DIALECT = "relational.db.dialect";

	@ConfigurationGroupDefinition(properties = { DATABASE_HOST, DATABASE_PORT, DATABASE_DIALECT }, type = URI.class)
	private static final String DATABASE_ADDRESS = "relational.db.address";
	@ConfigurationPropertyDefinition(label = "Operational database name")
	private static final String DATABASE_NAME = "relational.db.name";

	@ConfigurationPropertyDefinition(sensitive = true, label = "Admin username for the operational database")
	private static final String ADMIN_USERNAME = "relational.db.admin.username";
	@ConfigurationPropertyDefinition(sensitive = true, label = "Admin password for the operational database")
	private static final String ADMIN_PASSWORD = "relational.db.admin.password"; //NOSONAR

	@ConfigurationPropertyDefinition(defaultValue = "3",type = Integer.class, sensitive = true, label = "Indicates the maximum time in minutes a connection may be idle before being closed")
	private static final String IDLE_TIMEOUT_MINUTES = "relational.db.connection.timeout.idleTimeoutMinutes";
	@ConfigurationPropertyDefinition(defaultValue = "60",type = Integer.class, sensitive = true, label = "Common query timeout for all connections if nothing else is set")
	private static final String QUERY_TIMEOUT = "relational.db.connection.timeout.queryTimeout";
	@ConfigurationPropertyDefinition(defaultValue = "4", type = Integer.class, sensitive = true, label = "How may times to try to reconnect to the database before failing")
	private static final String ALLOCATION_RETRY = "relational.db.connection.timeout.allocationRetry";
	@ConfigurationPropertyDefinition(defaultValue = "2500", type = Integer.class, sensitive = true, label = "How much time to wait before trying to reconnect to the database")
	private static final String ALLOCATION_RETRY_WAIT = "relational.db.connection.timeout.allocationRetryWaitMillis";

	@ConfigurationGroupDefinition(
			properties = { IDLE_TIMEOUT_MINUTES, QUERY_TIMEOUT, ALLOCATION_RETRY, ALLOCATION_RETRY_WAIT },
			type = DatabaseSettings.ConnectionTimeoutSettings.class)
	private static final String CONNECTION_TIMEOUT = "relational.db.connection.timeout";

	@ConfigurationPropertyDefinition(defaultValue = "org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLValidConnectionChecker", sensitive = true, label = "Vendor specific implementation to check if connection is valid")
	private static final String VALID_CONNECTION_CHECKER = "relational.db.connection.validation.validConnectionChecker";
	@ConfigurationPropertyDefinition(defaultValue = "org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLExceptionSorter", sensitive = true, label = "Vendor specific implementation to check if exception is fatal and the connection cannot be used")
	private static final String EXCEPTION_SORTER = "relational.db.connection.validation.exceptionSorter";
	@ConfigurationPropertyDefinition(defaultValue = "true", type = Boolean.class, sensitive = true, label = "Enables connection validation before returning a connection from the connection pool. Cannot be enabled if background validation is enabled")
	private static final String VALIDATE_ON_MATCH = "relational.db.connection.validation.validateOnMatch";
	@ConfigurationPropertyDefinition(defaultValue = "0", type = Integer.class, sensitive = true, label = "If value greater than 0 is set enables background validation. Value will be ignored until validate on match is enabled!")
	private static final String BACKGROUND_VALIDATION_MILLIS = "relational.db.connection.validation.backgroundValidationMillis";
	@ConfigurationPropertyDefinition(defaultValue = "false", type = Boolean.class, sensitive = true, label = "Whether fail a connection allocation on the first connection if it is invalid (true) or keep trying until the pool is exhausted of all potential connections (false) default false")
	private static final String USE_FAST_FAIL = "relational.db.connection.validation.useFastFail";
	@ConfigurationPropertyDefinition(defaultValue = "SELECT 1", sensitive = true, label = "Query to use to check if connection is valid. Not applicable if connection checker is set")
	private static final String CHECK_VALID_CONNECTION_SQL = "relational.db.connection.validation.checkValidConnectionSql";

	@ConfigurationGroupDefinition(
			properties = { VALID_CONNECTION_CHECKER, EXCEPTION_SORTER, VALIDATE_ON_MATCH, BACKGROUND_VALIDATION_MILLIS,
					USE_FAST_FAIL, CHECK_VALID_CONNECTION_SQL }, type = DatabaseSettings.ConnectionValidationSettings.class)
	private static final String CONNECTION_VALIDATION = "relational.db.connection.validation";

	@ConfigurationPropertyDefinition(defaultValue = "10", type = Integer.class, sensitive = true, label = "The minimum number of connections a pool should hold")
	private static final String MIN_POOL_SIZE = "relational.db.connection.pool.minPoolSize";
	@ConfigurationPropertyDefinition(defaultValue = "10", type = Integer.class, sensitive = true, label = "The initial number of connections a pool should hold")
	private static final String INITIAL_POOL_SIZE = "relational.db.connection.pool.initialPoolSize";
	@ConfigurationPropertyDefinition(defaultValue = "100", type = Integer.class, sensitive = true, label = "the maximum number of connections for a pool. No more connections will be created in each sub-pool")
	private static final String MAX_POOL_SIZE = "relational.db.connection.pool.maxPoolSize";
	@ConfigurationPropertyDefinition(defaultValue = "true", type = Boolean.class, sensitive = true, label = "Whether to attempt to prefill the connection pool")
	private static final String PREFILL_POOL = "relational.db.connection.pool.prefill";
	@ConfigurationPropertyDefinition(defaultValue = "false", type = Boolean.class, sensitive = true, label = "Define if the relational.db.connection.pool.minPoolSize should be considered a strictly")
	private static final String USE_STRICT_MIN = "relational.db.connection.pool.useStrictMin";
	@ConfigurationPropertyDefinition(defaultValue = "InvalidIdleConnections", sensitive = true, label = "Specifies how the pool should be flush in case of an error. Valid values are: FailingConnectionOnly (default), InvalidIdleConnections, IdleConnections, Gracefully, EntirePool, AllInvalidIdleConnections, AllIdleConnections, AllGracefully, AllConnections")
	private static final String POOL_FLUSH_STRATEGY = "relational.db.connection.pool.flushStrategy";

	@ConfigurationGroupDefinition(
			properties = { MIN_POOL_SIZE, INITIAL_POOL_SIZE, MAX_POOL_SIZE, PREFILL_POOL, USE_STRICT_MIN,
					POOL_FLUSH_STRATEGY }, type = DatabaseSettings.ConnectionPoolSettings.class)
	private static final String CONNECTION_POOLING = "relational.db.connection.pool";

	@Inject
	@Configuration(DATABASE_ADDRESS)
	private ConfigurationProperty<URI> address;

	@Inject
	@Configuration(DATABASE_NAME)
	private ConfigurationProperty<String> databaseName;

	@Inject
	@Configuration(DATABASE_DIALECT)
	private ConfigurationProperty<String> databaseDialect;

	@Inject
	@Configuration(DATABASE_HOST)
	private ConfigurationProperty<String> databaseHost;

	@Inject
	@Configuration(DATABASE_PORT)
	private ConfigurationProperty<Integer> databasePort;

	@Inject
	@Configuration(ADMIN_USERNAME)
	private ConfigurationProperty<String> adminUsername;

	@Inject
	@Configuration(ADMIN_PASSWORD)
	private ConfigurationProperty<String> adminPassword;

	@Inject
	@Configuration(CONNECTION_POOLING)
	private ConfigurationProperty<ConnectionPoolSettings> connectionPoolSettings;

	@Inject
	@Configuration(CONNECTION_TIMEOUT)
	private ConfigurationProperty<ConnectionTimeoutSettings> connectionTimeoutSettings;

	@Inject
	@Configuration(CONNECTION_VALIDATION)
	private ConfigurationProperty<ConnectionValidationSettings> connectionValidationSettings;

	@Inject
	private SecurityContext securityContext;

	@Inject
	private ConfigurationDatasourceProvisioner datasourceProvisioner;

	@ConfigurationConverter(DATABASE_ADDRESS)
	static URI buildDbAddress(GroupConverterContext context) {
		String dialect = context.get(DATABASE_DIALECT);
		String host = context.get(DATABASE_HOST);
		Integer port = context.get(DATABASE_PORT);

		try {
			return new URI("jdbc:" + dialect, null, host, port, null, null, null);
		} catch (URISyntaxException e) {
			throw new ConverterException(e);
		}
	}

	@ConfigurationConverter(CONNECTION_TIMEOUT)
	static ConnectionTimeoutSettings buildConnectionTimeoutSettings(GroupConverterContext context) {
		ConnectionTimeoutSettings settings = new ConnectionTimeoutSettings();
		settings.setIdleTimeoutMinutes(context.get(IDLE_TIMEOUT_MINUTES));
		settings.setAllocationRetry(context.get(ALLOCATION_RETRY));
		settings.setAllocationRetryWaitMillis(context.get(ALLOCATION_RETRY_WAIT));
		settings.setQueryTimeout(context.get(QUERY_TIMEOUT));
		return settings;
	}

	@ConfigurationConverter(CONNECTION_VALIDATION)
	static ConnectionValidationSettings buildConnectionValidationSettings(GroupConverterContext context) {
		ConnectionValidationSettings settings = new ConnectionValidationSettings();
		settings.setValidateOnMatch(context.get(VALIDATE_ON_MATCH));
		if (!settings.isValidateOnMatch()) {
			settings.setBackgroundValidationMillis(context.get(BACKGROUND_VALIDATION_MILLIS));
		}
		settings.setCheckValidConnectionSql(context.get(CHECK_VALID_CONNECTION_SQL));
		settings.setExceptionSorter(context.get(EXCEPTION_SORTER));
		settings.setValidConnectionChecker(context.get(VALID_CONNECTION_CHECKER));
		settings.setUseFastFail(context.get(USE_FAST_FAIL));
		return settings;
	}

	@ConfigurationConverter(CONNECTION_POOLING)
	static ConnectionPoolSettings buildConnectionPoolSettings(GroupConverterContext context) {
		ConnectionPoolSettings settings = new ConnectionPoolSettings();
		settings.setFlushStrategy(DatabaseSettings.PoolFlushStrategy.parse(context.get(POOL_FLUSH_STRATEGY)));
		settings.setInitialPoolSize(context.get(INITIAL_POOL_SIZE));
		settings.setMinPoolSize(context.get(MIN_POOL_SIZE));
		settings.setMaxPoolSize(context.get(MAX_POOL_SIZE));
		settings.setPrefill(context.get(PREFILL_POOL));
		settings.setUseStrictMin(context.get(USE_STRICT_MIN));
		return settings;
	}

	@Override
	public String getCoreDataSourceJndi() {
		return Datasources.coreJndi();
	}

	@Override
	public DataSource getCoreDataSource() {
		// does not store data source instance
		return datasourceProvisioner.lookupDataSource(getCoreDataSourceJndi(), this);
	}

	@Override
	public String getDataSourceJndi() {
		return Datasources.forTenant(securityContext.getCurrentTenantId());
	}

	@Override
	public DataSource getDataSource() {
		// does not store data source instance
		return datasourceProvisioner.lookupDataSource(getDataSourceJndi(), this);
	}

	@Override
	public String getDatabaseAddressConfigurationName() {
		return DATABASE_HOST;
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

	@Override
	public ConfigurationProperty<String> getDatabaseDialectConfiguration() {
		return databaseDialect;
	}

	@Override
	public ConfigurationProperty<String> getAdminUsernameConfiguration() {
		return adminUsername;
	}

	@Override
	public ConfigurationProperty<String> getAdminPasswordConfiguration() {
		return adminPassword;
	}

	@Override
	public ConfigurationProperty<String> getDatabaseHostConfiguration() {
		return databaseHost;
	}

	@Override
	public ConfigurationProperty<String> getDatabaseNameConfiguration() {
		return databaseName;
	}

	@Override
	public ConfigurationProperty<Integer> getDatabasePortConfiguration() {
		return databasePort;
	}

}
