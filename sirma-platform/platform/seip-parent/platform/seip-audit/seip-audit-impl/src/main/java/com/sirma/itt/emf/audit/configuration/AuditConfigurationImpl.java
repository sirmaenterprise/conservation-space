/**
 *
 */
package com.sirma.itt.emf.audit.configuration;

import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationGroupDefinition;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.ConverterContext;
import com.sirma.itt.seip.configuration.convert.ConverterException;
import com.sirma.itt.seip.configuration.convert.GroupConverterContext;
import com.sirma.itt.seip.db.ConfigurationDatasourceProvisioner;
import com.sirma.itt.seip.db.DatabaseSettings;
import com.sirma.itt.seip.db.Datasources;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Default audit database configuration to produce data sources to audit database.
 *
 * @author BBonev
 */
@Singleton
class AuditConfigurationImpl implements AuditConfiguration {

	@ConfigurationPropertyDefinition(defaultValue = "localhost", sensitive = true, label = "Host name for the audit database")
	private static final String AUDIT_DATABASE_HOST = "audit.db.host";
	@ConfigurationPropertyDefinition(defaultValue = "5432", sensitive = true, type = Integer.class, label = "Port number for the audit database")
	private static final String AUDIT_DATABASE_PORT = "audit.db.port";
	@ConfigurationPropertyDefinition(defaultValue = "postgresql", sensitive = true, label = "Audit database dialect")
	private static final String AUDIT_DATABASE_DIALECT = "audit.db.dialect";
	@ConfigurationPropertyDefinition(sensitive = true, label = "Audit database name")
	private static final String AUDIT_DATABASE_NAME = "audit.db.name";

	@ConfigurationPropertyDefinition(sensitive = true, label = "Admin username for the audit database")
	private static final String AUDIT_DATABASE_ADMIN_USERNAME = "audit.db.admin.username";
	@ConfigurationPropertyDefinition(sensitive = true, label = "Admin password for the audit database")
	private static final String AUDIT_DATABASE_ADMIN_PASSWORD = "audit.db.admin.password"; //NOSONAR

	@ConfigurationPropertyDefinition(defaultValue = "http", sensitive = true, label = "Audit solr server protocol.")
	private static final String SOLR_AUDIT_SERVER_PROTOCOL = "solr.audit.host.protocol";
	@ConfigurationPropertyDefinition(defaultValue = "localhost", sensitive = true, label = "Audit solr server ip or name.")
	private static final String SOLR_AUDIT_SERVER_HOST = "solr.audit.host.servername";
	@ConfigurationPropertyDefinition(defaultValue = "8984", sensitive = true, type = Integer.class, label = "Audit solr server port.")
	private static final String SOLR_AUDIT_SERVER_PORT = "solr.audit.host.serverport";
	@ConfigurationPropertyDefinition(sensitive = true, label = "Solr server core to use for audit.")
	private static final String SOLR_AUDIT_SERVER_CORE = "solr.audit.host.core";
	@ConfigurationPropertyDefinition(sensitive = true, label = "Solr server core to use for audit.")
	private static final String SOLR_RECENT_ACTIVITIES_SERVER_CORE = "solr.audit.recentActivities.host.core";
	@ConfigurationPropertyDefinition(defaultValue = "5000", sensitive = true, type = Integer.class, label = "Audit solr socket timeout. If its set \"-1\" will disable setting the timeout.")
	private static final String SOLR_AUDIT_SOCKET_TIMEOUT = "solr.audit.socket.timeout";

	@ConfigurationPropertyDefinition(defaultValue = "recent.activities.", label = "Solr server core to use for audit.")
	private static final String RECENT_ACTIVITIES_LABEL_PREFIX = "recent.activities.label.prefix";

	@ConfigurationGroupDefinition(properties = { AUDIT_DATABASE_HOST, AUDIT_DATABASE_PORT, AUDIT_DATABASE_DIALECT }, type = URI.class)
	private static final String DATABASE_ADDRESS = "audit.db.address";
	@ConfigurationGroupDefinition(properties = { SOLR_AUDIT_SERVER_PROTOCOL, SOLR_AUDIT_SERVER_HOST,
			SOLR_AUDIT_SERVER_PORT }, type = String.class)
	private static final String SOLR_EXTERNAL_ADMIN_ADDRESS = "solr.audit.external.admin.address";
	@ConfigurationGroupDefinition(properties = { SOLR_AUDIT_SERVER_PROTOCOL, SOLR_AUDIT_SERVER_HOST,
			SOLR_AUDIT_SERVER_PORT }, type = String.class)
	private static final String SOLR_ADMIN_ADDRESS = "solr.audit.admin.address";
	@ConfigurationGroupDefinition(properties = { SOLR_AUDIT_SERVER_PROTOCOL, SOLR_AUDIT_SERVER_HOST,
			SOLR_AUDIT_SERVER_PORT }, type = String.class)
	private static final String SOLR_ADDRESS = "solr.audit.address";
	/** The Solr address. */
	@ConfigurationGroupDefinition(properties = { SOLR_AUDIT_SERVER_PROTOCOL, SOLR_AUDIT_SERVER_HOST,
			SOLR_AUDIT_SERVER_PORT, SOLR_AUDIT_SERVER_CORE, SOLR_AUDIT_SOCKET_TIMEOUT }, type = SolrClient.class)
	private static final String SOLR_AUDIT_ADDRESS = "audit.solr.address";
	@ConfigurationGroupDefinition(properties = { SOLR_AUDIT_SERVER_PROTOCOL, SOLR_AUDIT_SERVER_HOST,
			SOLR_AUDIT_SERVER_PORT, SOLR_RECENT_ACTIVITIES_SERVER_CORE,
			SOLR_AUDIT_SOCKET_TIMEOUT }, type = SolrClient.class)
	private static final String SOLR_RECENT_ACTIVITIES_ADDRESS = "audit.recentActivities.solr.address";

	@ConfigurationPropertyDefinition(defaultValue = "3",type = Integer.class, sensitive = true, label = "Indicates the maximum time in minutes a connection may be idle before being closed")
	private static final String  AUDIT_IDLE_TIMEOUT_MINUTES = "audit.db.connection.timeout.idleTimeoutMinutes";
	@ConfigurationPropertyDefinition(defaultValue = "60",type = Integer.class, sensitive = true, label = "Common query timeout for all connections if nothing else is set")
	private static final String AUDIT_QUERY_TIMEOUT = "audit.db.connection.timeout.queryTimeout";
	@ConfigurationPropertyDefinition(defaultValue = "4", type = Integer.class, sensitive = true, label = "How may times to try to reconnect to the database before failing")
	private static final String AUDIT_ALLOCATION_RETRY = "audit.db.connection.timeout.allocationRetry";
	@ConfigurationPropertyDefinition(defaultValue = "2500", type = Integer.class, sensitive = true, label = "How much time to wait before trying to reconnect to the database")
	private static final String AUDIT_ALLOCATION_RETRY_WAIT = "audit.db.connection.timeout.allocationRetryWaitMillis";

	@ConfigurationGroupDefinition(properties = { AUDIT_QUERY_TIMEOUT, AUDIT_ALLOCATION_RETRY,
			AUDIT_ALLOCATION_RETRY_WAIT }, type = DatabaseSettings.ConnectionTimeoutSettings.class)
	private static final String AUDIT_CONNECTION_TIMEOUT = "audit.db.connection.timeout";

	@ConfigurationPropertyDefinition(defaultValue = "org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLValidConnectionChecker", sensitive = true, label = "Vendor specific implementation to check if connection is valid")
	private static final String AUDIT_VALID_CONNECTION_CHECKER = "audit.db.connection.validation.validConnectionChecker";
	@ConfigurationPropertyDefinition(defaultValue = "org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLExceptionSorter", sensitive = true, label = "Vendor specific implementation to check if exception is fatal and the connection cannot be used")
	private static final String AUDIT_EXCEPTION_SORTER = "audit.db.connection.validation.exceptionSorter";
	@ConfigurationPropertyDefinition(defaultValue = "true", type = Boolean.class, sensitive = true, label = "Enables connection validation before returning a connection from the connection pool. Cannot be enabled if background validation is enabled")
	private static final String AUDIT_VALIDATE_ON_MATCH = "audit.db.connection.validation.validateOnMatch";
	@ConfigurationPropertyDefinition(defaultValue = "0", type = Integer.class, sensitive = true, label = "If value greater than 0 is set enables background validation. Value will be ignored until validate on match is enabled!")
	private static final String AUDIT_BACKGROUND_VALIDATION_MILLIS = "audit.db.connection.validation.backgroundValidationMillis";
	@ConfigurationPropertyDefinition(defaultValue = "false", type = Boolean.class, sensitive = true, label = "Whether fail a connection allocation on the first connection if it is invalid (true) or keep trying until the pool is exhausted of all potential connections (false) default false")
	private static final String AUDIT_USE_FAST_FAIL = "audit.db.connection.validation.useFastFail";
	@ConfigurationPropertyDefinition(defaultValue = "SELECT 1", sensitive = true, label = "Query to use to check if connection is valid. Not applicable if connection checker is set")
	private static final String AUDIT_CHECK_VALID_CONNECTION_SQL = "audit.db.connection.validation.checkValidConnectionSql";

	@ConfigurationGroupDefinition(
			properties = { AUDIT_VALID_CONNECTION_CHECKER, AUDIT_EXCEPTION_SORTER, AUDIT_VALIDATE_ON_MATCH,
					AUDIT_BACKGROUND_VALIDATION_MILLIS, AUDIT_USE_FAST_FAIL, AUDIT_CHECK_VALID_CONNECTION_SQL },
			type = DatabaseSettings.ConnectionValidationSettings.class)
	private static final String AUDIT_CONNECTION_VALIDATION = "audit.db.connection.validation";

	@ConfigurationPropertyDefinition(defaultValue = "5", type = Integer.class, sensitive = true, label = "The minimum number of connections a pool should hold")
	private static final String AUDIT_MIN_POOL_SIZE = "audit.db.connection.pool.minPoolSize";
	@ConfigurationPropertyDefinition(defaultValue = "5", type = Integer.class, sensitive = true, label = "The initial number of connections a pool should hold")
	private static final String AUDIT_INITIAL_POOL_SIZE = "audit.db.connection.pool.initialPoolSize";
	@ConfigurationPropertyDefinition(defaultValue = "100", type = Integer.class, sensitive = true, label = "the maximum number of connections for a pool. No more connections will be created in each sub-pool")
	private static final String AUDIT_MAX_POOL_SIZE = "audit.db.connection.pool.maxPoolSize";
	@ConfigurationPropertyDefinition(defaultValue = "true", type = Boolean.class, sensitive = true, label = "Whether to attempt to prefill the connection pool")
	private static final String AUDIT_PREFILL_POOL = "audit.db.connection.pool.prefill";
	@ConfigurationPropertyDefinition(defaultValue = "false", type = Boolean.class, sensitive = true, label = "Define if the relational.db.connection.pool.minPoolSize should be considered a strictly")
	private static final String AUDIT_USE_STRICT_MIN = "audit.db.connection.pool.useStrictMin";
	@ConfigurationPropertyDefinition(defaultValue = "InvalidIdleConnections", sensitive = true, label = "Specifies how the pool should be flush in case of an error. Valid values are: FailingConnectionOnly (default), InvalidIdleConnections, IdleConnections, Gracefully, EntirePool, AllInvalidIdleConnections, AllIdleConnections, AllGracefully, AllConnections")
	private static final String AUDIT_POOL_FLUSH_STRATEGY = "audit.db.connection.pool.flushStrategy";

	@ConfigurationGroupDefinition(
			properties = { AUDIT_MIN_POOL_SIZE, AUDIT_INITIAL_POOL_SIZE, AUDIT_MAX_POOL_SIZE, AUDIT_PREFILL_POOL,
					AUDIT_USE_STRICT_MIN, AUDIT_POOL_FLUSH_STRATEGY }, type = DatabaseSettings.ConnectionPoolSettings.class)
	private static final String AUDIT_CONNECTION_POOLING = "audit.db.connection.pool";

	@Inject
	@Configuration(SOLR_ADDRESS)
	private ConfigurationProperty<String> solrAddress;
	@Inject
	@Configuration(SOLR_AUDIT_ADDRESS)
	private ConfigurationProperty<SolrClient> client;
	@Inject
	@Configuration(SOLR_RECENT_ACTIVITIES_ADDRESS)
	private ConfigurationProperty<SolrClient> recentActivitiesClient;
	@Inject
	@Configuration(SOLR_EXTERNAL_ADMIN_ADDRESS)
	private ConfigurationProperty<String> solrExternalAdminAddress;
	@Inject
	@Configuration(SOLR_ADMIN_ADDRESS)
	private ConfigurationProperty<String> solrAdminAddress;

	@Inject
	@Configuration(DATABASE_ADDRESS)
	private ConfigurationProperty<URI> dbAddress;

	@Inject
	@Configuration(RECENT_ACTIVITIES_LABEL_PREFIX)
	private ConfigurationProperty<String> recentActivitiesLabelPrefix;

	@Inject
	@Configuration(AUDIT_DATABASE_ADMIN_USERNAME)
	private ConfigurationProperty<String> auditDbAdminUsername;

	@Inject
	@Configuration(AUDIT_DATABASE_ADMIN_PASSWORD)
	private ConfigurationProperty<String> auditDbAdminPassword;

	@Inject
	@Configuration(AUDIT_DATABASE_DIALECT)
	private ConfigurationProperty<String> auditDatabaseDialect;

	@Inject
	@Configuration(AUDIT_DATABASE_HOST)
	private ConfigurationProperty<String> auditDatabaseHost;

	@Inject
	@Configuration(AUDIT_DATABASE_PORT)
	private ConfigurationProperty<Integer> auditDatabasePort;

	@Inject
	@Configuration(AUDIT_DATABASE_NAME)
	private ConfigurationProperty<String> auditDatabaseName;

	@Inject
	@Configuration(AUDIT_CONNECTION_POOLING)
	private ConfigurationProperty<ConnectionPoolSettings> connectionPoolSettings;

	@Inject
	@Configuration(AUDIT_CONNECTION_TIMEOUT)
	private ConfigurationProperty<ConnectionTimeoutSettings> connectionTimeoutSettings;

	@Inject
	@Configuration(AUDIT_CONNECTION_VALIDATION)
	private ConfigurationProperty<ConnectionValidationSettings> connectionValidationSettings;

	@Inject
	private SecurityContext securityContext;

	@Inject
	private ConfigurationDatasourceProvisioner datasourceProvisioner;

	@ConfigurationConverter(SOLR_ADMIN_ADDRESS)
	static String buildSolrAdmin(GroupConverterContext context) {
		return buildSolrAddressInternal(context) + "/admin";
	}

	@ConfigurationConverter(SOLR_ADDRESS)
	static String buildSolrAddress(GroupConverterContext context) {
		return buildSolrAddressInternal(context);
	}

	@ConfigurationConverter(SOLR_EXTERNAL_ADMIN_ADDRESS)
	static String buildExternalSolrAdmin(GroupConverterContext context) {
		String protocol = context.get(SOLR_AUDIT_SERVER_PROTOCOL);
		String host = context.get(SOLR_AUDIT_SERVER_HOST);
		Integer port = context.get(SOLR_AUDIT_SERVER_PORT);
		return new StringBuilder()
				.append(protocol)
					.append("://")
					.append(host)
					.append(":")
					.append(port)
					.append("/external-admin")
					.toString();
	}

	@ConfigurationConverter(DATABASE_ADDRESS)
	static URI buildDbAddress(GroupConverterContext context) {
		String dialect = context.get(AUDIT_DATABASE_DIALECT);
		String host = context.get(AUDIT_DATABASE_HOST);
		Integer port = context.get(AUDIT_DATABASE_PORT);

		try {
			return new URI("jdbc:" + dialect, null, host, port.intValue(), null, null, null);
		} catch (URISyntaxException e) {
			throw new ConverterException(e);
		}
	}

	@ConfigurationConverter(AUDIT_CONNECTION_VALIDATION)
	static ConnectionValidationSettings buildConnectionValidationSettings(GroupConverterContext context) {
		ConnectionValidationSettings settings = new ConnectionValidationSettings();
		settings.setValidateOnMatch(context.get(AUDIT_VALIDATE_ON_MATCH));
		if (!settings.isValidateOnMatch()) {
			settings.setBackgroundValidationMillis(context.get(AUDIT_BACKGROUND_VALIDATION_MILLIS));
		}
		settings.setUseFastFail(context.get(AUDIT_USE_FAST_FAIL));
		settings.setCheckValidConnectionSql(context.get(AUDIT_CHECK_VALID_CONNECTION_SQL));
		settings.setExceptionSorter(context.get(AUDIT_EXCEPTION_SORTER));
		settings.setValidConnectionChecker(context.get(AUDIT_VALID_CONNECTION_CHECKER));
		return settings;
	}

	@ConfigurationConverter(AUDIT_CONNECTION_TIMEOUT)
	static ConnectionTimeoutSettings buildConnectionTimeoutSettings(GroupConverterContext context) {
		ConnectionTimeoutSettings settings = new ConnectionTimeoutSettings();
		settings.setQueryTimeout(context.get(AUDIT_QUERY_TIMEOUT));
		settings.setAllocationRetry(context.get(AUDIT_ALLOCATION_RETRY));
		settings.setIdleTimeoutMinutes(context.get(AUDIT_IDLE_TIMEOUT_MINUTES));
		settings.setAllocationRetryWaitMillis(context.get(AUDIT_ALLOCATION_RETRY_WAIT));
		return settings;
	}

	@ConfigurationConverter(AUDIT_CONNECTION_POOLING)
	static ConnectionPoolSettings buildConnectionPoolSettings(GroupConverterContext context) {
		ConnectionPoolSettings settings = new ConnectionPoolSettings();
		settings.setInitialPoolSize(context.get(AUDIT_INITIAL_POOL_SIZE));
		settings.setMinPoolSize(context.get(AUDIT_MIN_POOL_SIZE));
		settings.setMaxPoolSize(context.get(AUDIT_MAX_POOL_SIZE));
		settings.setPrefill(context.get(AUDIT_PREFILL_POOL));
		settings.setUseStrictMin(context.get(AUDIT_USE_STRICT_MIN));
		settings.setFlushStrategy(DatabaseSettings.PoolFlushStrategy.parse(context.get(AUDIT_POOL_FLUSH_STRATEGY)));
		return settings;
	}

	/**
	 * Creates the solr client to old audit core.
	 *
	 * @param context
	 *            the context
	 * @param securityContext
	 *            the security context
	 * @return the solr client
	 */
	@ConfigurationConverter(SOLR_AUDIT_ADDRESS)
	static SolrClient createMainSolrClient(GroupConverterContext context, SecurityContext securityContext) {
		return createAuditClient(context, SOLR_AUDIT_SERVER_CORE);
	}

	/**
	 * Creates the recent activities solr client.
	 *
	 * @param context
	 *            the context
	 * @return the solr client
	 */
	@ConfigurationConverter(SOLR_RECENT_ACTIVITIES_ADDRESS)
	static SolrClient createRecentActivitiesSolrClient(GroupConverterContext context) {
		return createAuditClient(context, SOLR_RECENT_ACTIVITIES_SERVER_CORE);
	}

	@ConfigurationConverter(SOLR_AUDIT_SERVER_CORE)
	static String buildAuditCoreName(ConverterContext converterContext, SecurityContext securityContext) {
		if (StringUtils.isBlank(converterContext.getRawValue())) {
			return securityContext.getCurrentTenantId().replaceAll("\\.", "_") + "_audit";
		}
		return converterContext.getRawValue();
	}

	@ConfigurationConverter(SOLR_RECENT_ACTIVITIES_SERVER_CORE)
	static String buildRecentActivitiesCoreName(ConverterContext converterContext, SecurityContext securityContext) {
		if (StringUtils.isBlank(converterContext.getRawValue())) {
			return securityContext.getCurrentTenantId().replaceAll("\\.", "_") + "_recentActivities";
		}
		return converterContext.getRawValue();
	}

	private static SolrClient createAuditClient(GroupConverterContext context, String coreName) {
		HttpSolrClient client = createClientFromConfiguration(context, coreName);

		// for some reason if this is not set the average solr query takes 500+
		// milliseconds
		ConfigurationProperty<Integer> timeout = context.getValue(SOLR_AUDIT_SOCKET_TIMEOUT);
		if (timeout.isSet() && timeout.get().intValue() != -1) {
			client.setSoTimeout(timeout.get().intValue());
		}
		return client;
	}

	static HttpSolrClient createClientFromConfiguration(GroupConverterContext context, String coreName) {
		String core = context.get(coreName);
		String address = buildSolrAddressInternal(context) + "/" + core;
		return new HttpSolrClient(address);
	}

	private static String buildSolrAddressInternal(GroupConverterContext context) {
		String protocol = context.get(SOLR_AUDIT_SERVER_PROTOCOL);
		String host = context.get(SOLR_AUDIT_SERVER_HOST);
		Integer port = context.get(SOLR_AUDIT_SERVER_PORT);
		return new StringBuilder()
				.append(protocol)
					.append("://")
					.append(host)
					.append(":")
					.append(port)
					.append("/solr")
					.toString();
	}

	@Override
	public String getDataSourceJndi() {
		return Datasources.forTenant(securityContext.getCurrentTenantId()) + "_audit";
	}

	@Override
	public DataSource getDataSource() {
		return datasourceProvisioner.lookupDataSource(getDataSourceJndi(), this);
	}

	@Override
	public String getSolrHostConfigurationName() {
		return SOLR_AUDIT_SERVER_HOST;
	}

	@Override
	public String getSolrPortConfigurationName() {
		return SOLR_AUDIT_SERVER_PORT;
	}

	@Override
	public String getSolrProtocolConfigurationName() {
		return SOLR_AUDIT_SERVER_PROTOCOL;
	}

	@Override
	public String getSolrTimeoutConfigurationName() {
		return SOLR_AUDIT_SOCKET_TIMEOUT;
	}

	@Override
	public String getSolrCoreConfigurationName() {
		return SOLR_AUDIT_SERVER_CORE;
	}

	@Override
	public String getRecentActivitiesSolrCoreConfigurationName() {
		return SOLR_RECENT_ACTIVITIES_SERVER_CORE;
	}

	@Override
	public String getDatabaseAddressConfigurationName() {
		return AUDIT_DATABASE_HOST;
	}

	@Override
	public ConfigurationProperty<URI> getDatabaseUri() {
		return dbAddress;
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
	public ConfigurationProperty<SolrClient> getSolrClient() {
		return client;
	}

	@Override
	public ConfigurationProperty<String> getSolrAddress() {
		return solrAddress;
	}

	@Override
	public ConfigurationProperty<String> getSolrExternalAdminAddress() {
		return solrExternalAdminAddress;
	}

	@Override
	public ConfigurationProperty<String> getSolrAdminAddress() {
		return solrAdminAddress;
	}

	@Override
	public ConfigurationProperty<SolrClient> getRecentActivitiesSolrClient() {
		return recentActivitiesClient;
	}

	@Override
	public ConfigurationProperty<String> getRecentActivitiesLabelPrefix() {
		return recentActivitiesLabelPrefix;
	}

	@Override
	public ConfigurationProperty<String> getDatabaseDialectConfiguration() {
		return auditDatabaseDialect;
	}

	@Override
	public ConfigurationProperty<String> getAdminUsernameConfiguration() {
		return auditDbAdminUsername;
	}

	@Override
	public ConfigurationProperty<String> getAdminPasswordConfiguration() {
		return auditDbAdminPassword;
	}

	@Override
	public ConfigurationProperty<String> getDatabaseHostConfiguration() {
		return auditDatabaseHost;
	}

	@Override
	public ConfigurationProperty<String> getDatabaseNameConfiguration() {
		return auditDatabaseName;
	}

	@Override
	public ConfigurationProperty<Integer> getDatabasePortConfiguration() {
		return auditDatabasePort;
	}
}
