/**
 *
 */
package com.sirma.itt.emf.audit.configuration;

import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.configuration.ConfigurationException;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationGroupDefinition;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.ConverterContext;
import com.sirma.itt.seip.configuration.convert.ConverterException;
import com.sirma.itt.seip.configuration.convert.GroupConverterContext;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Default audit database configuration to produce data sources to audit database.
 *
 * @author BBonev
 */
@Singleton
class AuditConfigurationImpl implements AuditConfiguration {

	@ConfigurationPropertyDefinition(defaultValue = "localhost", sensitive = true, label = "Host name for the audit database")
	private static final String DATABASE_HOST = "audit.db.host";
	@ConfigurationPropertyDefinition(defaultValue = "5432", sensitive = true, type = Integer.class, label = "Port number for the audit database")
	private static final String DATABASE_PORT = "audit.db.port";
	@ConfigurationPropertyDefinition(defaultValue = "postgresql", sensitive = true, label = "Audit database dialect")
	private static final String DATABASE_DIALECT = "audit.db.dialect";
	@ConfigurationPropertyDefinition(sensitive = true, label = "Audit database name")
	private static final String DATABASE_NAME = "audit.db.name";

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

	@ConfigurationGroupDefinition(properties = { DATABASE_HOST, DATABASE_PORT, DATABASE_DIALECT }, type = URI.class)
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
	private SecurityContext securityContext;

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
		String dialect = context.get(DATABASE_DIALECT);
		String host = context.get(DATABASE_HOST);
		Integer port = context.get(DATABASE_PORT);

		try {
			return new URI("jdbc:" + dialect, null, host, port.intValue(), null, null, null);
		} catch (URISyntaxException e) {
			throw new ConverterException(e);
		}
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
		if (StringUtils.isNullOrEmpty(converterContext.getRawValue())) {
			return securityContext.getCurrentTenantId().replaceAll("\\.", "_") + "_audit";
		}
		return converterContext.getRawValue();
	}

	@ConfigurationConverter(SOLR_RECENT_ACTIVITIES_SERVER_CORE)
	static String buildRecentActivitiesCoreName(ConverterContext converterContext, SecurityContext securityContext) {
		if (StringUtils.isNullOrEmpty(converterContext.getRawValue())) {
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
		return DbDao.DATASOURCE_PREFIX + securityContext.getCurrentTenantId() + "_audit";
	}

	@Override
	public DataSource getDataSource() {
		return lookupDataSource(getDataSourceJndi());
	}

	static DataSource lookupDataSource(String jndiName) {
		try {
			InitialContext context = new InitialContext();
			return (DataSource) context.lookup(jndiName);
		} catch (NamingException e) {
			throw new ConfigurationException(e);
		}
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
	public String getDatabaseAddressConfiguration() {
		return DATABASE_HOST;
	}

	@Override
	public String getDatabasePortConfiguration() {
		return DATABASE_PORT;
	}

	@Override
	public String getDatabaseDialectConfiguration() {
		return DATABASE_DIALECT;
	}

	@Override
	public String getDatabaseNameConfiguration() {
		return DATABASE_NAME;
	}

	@Override
	public ConfigurationProperty<URI> getDatabaseUri() {
		return dbAddress;
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

	/**
	 * Getter method for recentActivitiesLabelPrefix.
	 *
	 * @return the recentActivitiesLabelPrefix
	 */
	@Override
	public ConfigurationProperty<String> getRecentActivitiesLabelPrefix() {
		return recentActivitiesLabelPrefix;
	}
}
