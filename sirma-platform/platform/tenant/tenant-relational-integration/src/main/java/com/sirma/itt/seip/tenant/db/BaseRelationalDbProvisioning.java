package com.sirma.itt.seip.tenant.db;

import static com.sirma.itt.seip.wildfly.cli.db.WildFlyDatasourceProvisioning.getXaDataSourcePort;
import static com.sirma.itt.seip.wildfly.cli.db.WildFlyDatasourceProvisioning.getXaDataSourceServerName;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.sql.Driver;
import java.sql.DriverManager;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;
import com.sirma.itt.seip.wildfly.WildflyControllerService;

/**
 * Provides base functionality required to implement database tenant provisioning. The class provides methods to create
 * and remove of database and datasources in Wildfly server.
 *
 * @author BBonev
 */
public class BaseRelationalDbProvisioning {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	protected WildflyControllerService controller;

	@Inject
	protected BeanManager beanManager;
	@Inject
	protected ConfigurationManagement configurationManagement;
	@Inject
	protected SecurityContextManager securityContextManager;

	@PostConstruct
	@SuppressWarnings("static-method")
	protected void initialize() {
		// ensure loaded JDBC driver
		try {
			DriverManager.registerDriver((Driver) Class.forName("org.postgresql.Driver").newInstance());
		} catch (Exception e) {
			LOGGER.warn("Failed to load Driver: org.postgresql.Driver. Probably tenant creation will fail!", e);
		}
	}

	protected URI getDataSourceAddress(String dsName, String dialect) {
		String serverName = getXaDataSourceServerName(controller, dsName);
		String port = getXaDataSourcePort(controller, dsName);
		if (serverName == null || port == null) {
			throw new TenantCreationException("Could not find XA data source for tenant " + dsName);
		}
		return URI.create(dialect + "://" + serverName + ":" + port);
	}

	/**
	 * Checks if reusing database is allowed for current tenant data.
	 * 
	 * @param data
	 *            is the tenant info data to check
	 * @return false if parameter 'reuseDatabase' is not set or it is false
	 */

	public static boolean isDatabaseReused(TenantStepData data) {
		if (data == null || data.getProperties() == null) {
			return false;
		}
		Serializable reuseDatabase = data.getProperties().getOrDefault("reuseDatabase", "false");
		return Boolean.parseBoolean(reuseDatabase.toString());
	}

}