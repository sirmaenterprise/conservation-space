/**
 *
 */
package com.sirma.itt.seip.db;

import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.sirma.itt.seip.configuration.ConfigurationException;
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

	@Inject
	@Configuration(DATABASE_ADDRESS)
	private ConfigurationProperty<URI> address;

	@Inject
	private SecurityContext securityContext;

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
	 * Lookup data source using jndi context
	 *
	 * @param jndiName
	 *            the jndi name
	 * @return the data source
	 */
	static DataSource lookupDataSource(String jndiName) {
		try {
			InitialContext context = new InitialContext();
			return (DataSource) context.lookup(jndiName);
		} catch (NamingException e) {
			throw new ConfigurationException(e);
		}
	}

	@Override
	public String getCoreDataSourceJndi() {
		return DbDao.CORE_DATASOURCE_NAME;
	}

	@Override
	public DataSource getCoreDataSource() {
		// does not store data source instance
		return lookupDataSource(getCoreDataSourceJndi());
	}

	@Override
	public String getDataSourceJndi() {
		return DbDao.DATASOURCE_PREFIX + securityContext.getCurrentTenantId();
	}

	@Override
	public DataSource getDataSource() {
		// does not store data source instance
		return lookupDataSource(getDataSourceJndi());
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
		return address;
	}

}
