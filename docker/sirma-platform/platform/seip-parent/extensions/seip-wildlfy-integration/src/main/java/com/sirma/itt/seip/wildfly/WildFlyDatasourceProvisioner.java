package com.sirma.itt.seip.wildfly;

import java.lang.invoke.MethodHandles;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.db.DatasourceModel;
import com.sirma.itt.seip.db.DatasourceProvisioner;
import com.sirma.itt.seip.exception.RollbackedException;

/**
 * Helper class for managing data sources in WildFly server.
 *
 * @author BBonev
 * @author bbanchev
 */
@Singleton
public class WildFlyDatasourceProvisioner implements DatasourceProvisioner {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	/** Key in the provisioning model to map a db user password. */
	public static final String DB_PASSWORD = "dbPassword"; // NOSONAR
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

	@Inject
	private WildflyControllerService controller;

	@Override
	public String getXaDataSourceServerName(String datasourceName) {
		return getXaDataSourceProperty(datasourceName, XA_SERVER_NAME);
	}

	@Override
	public String getXaDataSourcePort(String datasourceName) {
		return getXaDataSourceProperty(datasourceName, XA_PORT_NUMBER);
	}

	@Override
	public String getXaDataSourceDatabase(String datasourceName) {
		return getXaDataSourceProperty(datasourceName, XA_DATABASE_NAME);
	}

	@Override
	public void createXaDatasource(DatasourceModel model) throws RollbackedException {

		ModelNode root = new ModelNode();

		root.get(ClientConstants.OP).set(ClientConstants.COMPOSITE);

		ModelNode steps = root.get(ClientConstants.STEPS);
		ModelNode request = steps.add();

		request.get(ClientConstants.OP).set(ClientConstants.ADD);
		ModelNode address = request.get(ClientConstants.OP_ADDR);
		address.add(SUBSYSTEM, DATASOURCES);
		address.add(XA_DATA_SOURCE, model.getDatasourceName());

		addProperty(request,"pool-name", model.getPoolName());
		addProperty(request,"driver-name", model.getDriverName());
		addProperty(request,"jndi-name", model.getJndiName());
		addProperty(request,"user-name", model.getUsername());
		addProperty(request,"password", model.getPassword());
		addProperty(request,"use-java-context", model.isUseJavaContext());
		addProperty(request,"use-ccm", model.isUseCCM());

		addPropertyIf(request,"initial-pool-size", model.getInitialPoolSize(), isPositive());
		addProperty(request,"min-pool-size", model.getMinPoolSize());
		addProperty(request,"max-pool-size", model.getMaxPoolSize());
		addProperty(request, "pool-prefill", model.isPoolPrefill());
		addPropertyIf(request, "use-strict-min", model.isUseStrictMin(), isTrue());
		addPropertyIf(request, "flush-strategy", model.getFlushStrategy(), isSet());

		addPropertyIf(request, "valid-connection-checker-class-name", model.getValidConnectionChecker(), isSet());
		if (!StringUtils.isNotBlank(model.getValidConnectionChecker())) {
			addProperty(request, "check-valid-connection-sql", model.getValidConnectionSQL());
		}
		addPropertyIf(request, "exception-sorter-class-name", model.getExceptionSorter(), isSet());
		addPropertyIf(request, "validate-on-match", model.isValidateOnMatch(), isTrue());
		addPropertyIf(request, "background-validation", model.isBackgroundValidation(), isTrue());
		addPropertyIf(request, "background-validation-millis", model.getBackgroundValidationMillis(), isPositive());
		addPropertyIf(request, "use-fast-fail", model.isUseFastFail(), isTrue());
		addPropertyIf(request, "query-timeout", model.getQueryTimeoutSeconds(), isPositive());
		addPropertyIf(request, "idle-timeout-minutes", model.getIdleTimeoutMinutes(), isPositive());

		addProperty(request,"allocation-retry", model.getAllocationRetries());
		addProperty(request,"allocation-retry-wait-millis", model.getAllocationRetryWaitMillis());
		addProperty(request,"prepared-statements-cache-size", model.getPreparedStatementCacheSize());
		addProperty(request,"share-prepared-statements", model.isSharePreparedStatements());

		// to add a xa-datasource-properties that need to be requested and set
		// one by one in a
		// composite operation

		ModelNode step = steps.add();
		ModelNode propertiesAddress = getXaDataSourceAddress(step, ClientConstants.ADD,
				model.getDatasourceName());
		addXaDataSourcePropertyAddress(propertiesAddress, XA_SERVER_NAME);
		addProperty(step, ClientConstants.VALUE, model.getDatabaseHost());

		step = steps.add();
		propertiesAddress = getXaDataSourceAddress(step, ClientConstants.ADD, model.getDatasourceName());
		addXaDataSourcePropertyAddress(propertiesAddress, XA_PORT_NUMBER);
		addProperty(step, ClientConstants.VALUE, Integer.toString(model.getDatabasePort()));

		step = steps.add();
		propertiesAddress = getXaDataSourceAddress(step, ClientConstants.ADD, model.getDatasourceName());
		addXaDataSourcePropertyAddress(propertiesAddress, XA_DATABASE_NAME);
		addProperty(step, ClientConstants.VALUE, model.getDatabaseName());

		ModelNode result = controller.execute(root);
		if ("failed".equals(result.get(OUTCOME).asString())) {
			throw new RollbackedException(result.get(FAILURE_DESCRIPTION).asString());
		}
	}

	private Predicate<String> isSet() {
		return StringUtils::isNotBlank;
	}

	private Predicate<Boolean> isTrue() {
		return value -> value;
	}

	private IntPredicate isPositive() {
		return value -> value > 0;
	}

	private static void addProperty(ModelNode node, String property, int value) {
		node.get(property).set(value);
	}

	private static void addProperty(ModelNode node, String property, boolean value) {
		node.get(property).set(value);
	}

	private static void addPropertyIf(ModelNode node, String property, boolean value, Predicate<Boolean> test) {
		if (test.test(value)) {
			addProperty(node, property, value);
		}
	}

	private static void addPropertyIf(ModelNode node, String property, int value, IntPredicate test) {
		if (test.test(value)) {
			addProperty(node, property, value);
		}
	}

	private static void addProperty(ModelNode node, String property, String value) {
		node.get(property).set(value);
	}
	private static void addPropertyIf(ModelNode node, String property, String value, Predicate<String> test) {
		if (test.test(value)) {
			addProperty(node, property, value);
		}
	}

	@Override
	public void removeDatasource(String datasource) throws RollbackedException {
		if (datasourceExists(datasource)) {

			ModelNode root = new ModelNode();

			root.get(ClientConstants.OP).set(ClientConstants.REMOVE_OPERATION);
			ModelNode address = root.get(ClientConstants.OP_ADDR);
			address.add(SUBSYSTEM, DATASOURCES);
			address.add(XA_DATA_SOURCE, datasource);

			ModelNode result = controller.execute(root);
			if ("failed".equals(result.get(OUTCOME).asString())) {
				throw new RollbackedException(result.get(FAILURE_DESCRIPTION).asString());
			}
		} else {
			LOGGER.info("Datasource with id {} doesn't exist. Nothing to delete.", datasource);
		}
	}

	private boolean datasourceExists(String datasourceName) {
		ModelNode root = new ModelNode();
		getXaDataSourceAddress(root, ClientConstants.READ_RESOURCE_OPERATION, datasourceName);
		try {
			ModelNode response = controller.execute(root);
			return "success".equals(response.get(OUTCOME).asString());
		} catch (RollbackedException e) {
			LOGGER.warn("Could not execute query due to", e);
		}
		return false;
	}

	/**
	 * Gets the xa data source property.
	 *
	 * @param datasourceName
	 *            the datasource name
	 * @param property
	 *            the property
	 * @return the xa datasource property
	 */
	private String getXaDataSourceProperty(String datasourceName, String property) {
		ModelNode root = new ModelNode();
		ModelNode address = getXaDataSourceAddress(root, ClientConstants.READ_RESOURCE_OPERATION, datasourceName);
		addXaDataSourcePropertyAddress(address, property);

		try {
			ModelNode response = controller.execute(root);
			if ("success".equals(response.get(OUTCOME).asString())) {
				return response.get(ClientConstants.RESULT).get(ClientConstants.VALUE).asString();
			}
			LOGGER.debug("Could not query property {} from DS {} due to {}", property, datasourceName,
					response.get(FAILURE_DESCRIPTION).asString());
		} catch (RollbackedException e) {
			LOGGER.warn("Could not execute query due to", e);
		}
		return null;
	}

	private static ModelNode getXaDataSourceAddress(ModelNode node, String operation, String datasourceName) {
		node.get(ClientConstants.OP).set(operation);
		ModelNode address = node.get(ClientConstants.OP_ADDR);
		address.add(SUBSYSTEM, DATASOURCES);
		address.add(XA_DATA_SOURCE, datasourceName);
		return address;
	}

	private static void addXaDataSourcePropertyAddress(ModelNode addressNode, String dsProperty) {
		addressNode.add("xa-datasource-properties", dsProperty);
	}

}
