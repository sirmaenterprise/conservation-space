package com.sirma.itt.seip.wildfly.cli.db;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.wildfly.WildflyControllerService;

/**
 * Helper class for managing data sources in WildFly server.
 *
 * @author BBonev
 * @author bbanchev
 */
public class WildFlyDatasourceProvisioning {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	/** Key in the provisioning model to map a db user password. */
	public static final String DB_PASSWORD = "dbPassword";
	/** Key in the provisioning model to map a db user name. */
	public static final String DB_USER = "dbUser";

	private static final String DATASOURCES = "datasources";
	private static final String XA_DATA_SOURCE = "xa-data-source";
	private static final String SUBSYSTEM = "subsystem";
	private static final String FAILURE_DESCRIPTION = "failure-description";
	private static final String OUTCOME = "outcome";
	private static final String XA_DATABASE_NAME = "DatabaseName";
	private static final String XA_PORT_NUMBER = "PortNumber";
	private static final String XA_SERVER_NAME = "ServerName";

	/**
	 * Instantiates a new wild fly datasource provisioning.
	 */
	private WildFlyDatasourceProvisioning() {
		// utility class
	}

	/**
	 * Adds the user and password to the model
	 *
	 * @param model
	 *            the model
	 * @param dbUserName
	 *            the db user name
	 * @param password
	 *            the password
	 */
	public static void addUserAndPassword(Map<String, Serializable> model, String dbUserName, String password) {
		model.put(DB_USER, dbUserName);
		model.put(DB_PASSWORD, password);
	}

	/**
	 * Null safe value.
	 *
	 * @param value
	 *            the value
	 * @param defaultValue
	 *            the default value
	 * @return the string
	 */
	static String nullSafeValue(Serializable value, String defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		return String.valueOf(value);
	}

	/**
	 * Gets the xa data source property.
	 *
	 * @param controller
	 *            the controller
	 * @param datasourceName
	 *            the datasource name
	 * @param property
	 *            the property
	 * @return the xa datasource property
	 */
	public static String getXaDataSourceProperty(WildflyControllerService controller, String datasourceName,
			String property) {
		ModelNode root = new ModelNode();
		root.get(ClientConstants.OP).set(ClientConstants.READ_RESOURCE_OPERATION);
		ModelNode address = root.get(ClientConstants.OP_ADDR);
		addXaDataSourcePropertyAddress(address, datasourceName, property);

		try {
			ModelNode response = controller.execute(root);
			if ("success".equals(response.get(OUTCOME).asString())) {
				return response.get("result").get("value").asString();
			}
			LOGGER.debug("Could not query property {} from DS {} due to {}", property, datasourceName,
					response.get(FAILURE_DESCRIPTION).asString());
		} catch (RollbackedException e) {
			LOGGER.warn("Could not execute query due to", e);
		}
		return null;
	}

	/**
	 * Gets the XA data source server name property
	 *
	 * @param controller
	 *            the controller
	 * @param datasourceName
	 *            the datasource name
	 * @return the xa datasource server name
	 */
	public static String getXaDataSourceServerName(WildflyControllerService controller, String datasourceName) {
		return getXaDataSourceProperty(controller, datasourceName, XA_SERVER_NAME);
	}

	/**
	 * Gets the XA data source port property
	 *
	 * @param controller
	 *            the controller
	 * @param datasourceName
	 *            the datasource name
	 * @return the xa datasource port
	 */
	public static String getXaDataSourcePort(WildflyControllerService controller, String datasourceName) {
		return getXaDataSourceProperty(controller, datasourceName, XA_PORT_NUMBER);
	}

	/**
	 * Gets the XA data source database property
	 *
	 * @param controller
	 *            the controller
	 * @param datasourceName
	 *            the datasource name
	 * @return the xa datasource database
	 */
	public static String getXaDataSourceDatabase(WildflyControllerService controller, String datasourceName) {
		return getXaDataSourceProperty(controller, datasourceName, XA_DATABASE_NAME);
	}

	/**
	 * Creates the xa datasource.
	 *
	 * @param controller
	 *            the controller
	 * @param addressUri
	 *            the address uri
	 * @param databaseName
	 *            the database name
	 * @param model
	 *            the model
	 * @param name
	 *            the db name as tenant.id
	 * @throws RollbackedException
	 *             the exception
	 */
	public static void createXaDatasource(WildflyControllerService controller, URI addressUri, String databaseName,
			Map<String, Serializable> model, String name) throws RollbackedException {

		ModelNode root = new ModelNode();

		root.get(ClientConstants.OP).set(ClientConstants.COMPOSITE);

		ModelNode steps = root.get(ClientConstants.STEPS);
		ModelNode request = steps.add();

		request.get(ClientConstants.OP).set(ClientConstants.ADD);
		ModelNode address = request.get(ClientConstants.OP_ADDR);
		address.add(SUBSYSTEM, DATASOURCES);
		address.add(XA_DATA_SOURCE, name);

		request.get("pool-name").set("pool_" + name);
		request.get("driver-name").set(addressUri.getScheme());
		request.get("jndi-name").set("java:jboss/datasources/" + name);
		request.get("user-name").set(model.get(DB_USER).toString());
		request.get("password").set(model.get(DB_PASSWORD).toString());
		request.get("use-java-context").set(nullSafeValue(model.get("use-java-context"), "true"));
		request.get("use-ccm").set(nullSafeValue(model.get("use-ccm"), "true"));
		request.get("min-pool-size").set(nullSafeValue(model.get("min-pool-size"), "5"));
		request.get("max-pool-size").set(nullSafeValue(model.get("max-pool-size"), "100"));
		request.get("pool-prefill").set(nullSafeValue(model.get("pool-prefill"), "false"));
		request.get("check-valid-connection-sql").set("SELECT 1");
		request.get("allocation-retry").set("1");
		request.get("prepared-statements-cache-size").set("32");
		request.get("share-prepared-statements").set("true");

		// to add a xa-datasource-properties that need to be requested and set
		// one by one in a
		// composite operation

		ModelNode properties = steps.add();
		properties.get(ClientConstants.OP).set(ClientConstants.ADD);
		address = properties.get(ClientConstants.OP_ADDR);
		addXaDataSourcePropertyAddress(address, name, XA_SERVER_NAME);
		properties.get(ClientConstants.VALUE).set(addressUri.getHost());

		properties = steps.add();
		properties.get(ClientConstants.OP).set(ClientConstants.ADD);
		address = properties.get(ClientConstants.OP_ADDR);
		addXaDataSourcePropertyAddress(address, name, XA_PORT_NUMBER);
		properties.get(ClientConstants.VALUE).set(Integer.toString(addressUri.getPort()));

		properties = steps.add();
		properties.get(ClientConstants.OP).set(ClientConstants.ADD);
		address = properties.get(ClientConstants.OP_ADDR);
		addXaDataSourcePropertyAddress(address, name, XA_DATABASE_NAME);
		properties.get(ClientConstants.VALUE).set(databaseName);

		ModelNode result = controller.execute(root);
		if ("failed".equals(result.get(OUTCOME).asString())) {
			throw new RollbackedException(result.get(FAILURE_DESCRIPTION).asString());
		}
	}

	private static void addXaDataSourcePropertyAddress(ModelNode addressNode, String dsName, String dsProperty) {
		addressNode.add(SUBSYSTEM, DATASOURCES);
		addressNode.add(XA_DATA_SOURCE, dsName);
		addressNode.add("xa-datasource-properties", dsProperty);
	}

	/**
	 * Removes the datasource for the given tenant.
	 *
	 * @param controller
	 *            the controller
	 * @param datasource
	 *            the datasource
	 * @throws RollbackedException
	 *             the rollbacked exception
	 */
	public static void removeDatasource(WildflyControllerService controller, String datasource)
			throws RollbackedException {
		ModelNode root = new ModelNode();

		root.get(ClientConstants.OP).set(ClientConstants.REMOVE_OPERATION);
		ModelNode address = root.get(ClientConstants.OP_ADDR);
		address.add(SUBSYSTEM, DATASOURCES);
		address.add(XA_DATA_SOURCE, datasource);

		ModelNode result = controller.execute(root);
		if ("failed".equals(result.get(OUTCOME).asString())) {
			throw new RollbackedException(result.get(FAILURE_DESCRIPTION).asString());
		}
	}

	/**
	 * Gets the data sources.
	 *
	 * @param controller
	 *            the controller
	 * @return the data sources
	 * @throws RollbackedException
	 *             the exception
	 */
	public static List<ModelNode> listDataSources(WildflyControllerService controller) throws RollbackedException {

		final ModelNode request = new ModelNode();
		request.get(ClientConstants.OP).set(ClientConstants.READ_RESOURCE_OPERATION);
		request.get("recursive").set(true);
		request.get(ClientConstants.OP_ADDR).add(SUBSYSTEM, DATASOURCES);

		final ModelNode response = controller.execute(request);

		ModelNode modelNode = response.get(ClientConstants.RESULT);
		return modelNode.get(XA_DATA_SOURCE).asList();
	}

}
