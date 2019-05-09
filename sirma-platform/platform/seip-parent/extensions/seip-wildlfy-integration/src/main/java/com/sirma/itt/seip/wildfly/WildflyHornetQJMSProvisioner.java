package com.sirma.itt.seip.wildfly;

import static org.jboss.as.controller.client.helpers.ClientConstants.COMPOSITE;
import static org.jboss.as.controller.client.helpers.ClientConstants.OP;
import static org.jboss.as.controller.client.helpers.ClientConstants.OP_ADDR;
import static org.jboss.as.controller.client.helpers.ClientConstants.READ_RESOURCE_OPERATION;
import static org.jboss.as.controller.client.helpers.ClientConstants.STEPS;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.runtime.boot.DeploymentException;
import com.sirmaenterprise.sep.jms.annotations.AddressFullMessagePolicyType;
import com.sirmaenterprise.sep.jms.annotations.DestinationType;
import com.sirmaenterprise.sep.jms.provision.DestinationDefinition;
import com.sirmaenterprise.sep.jms.provision.JmsProvisioner;
import com.sirmaenterprise.sep.jms.provision.JmsSubsystemModel;

/**
 * Provisioner responsible for provisioning everything needed by wildfly for JMS. The provisioning needs to be ran
 * before anything else in the system because if the JMS subsystem is not already configured a reload of the server
 * might need to occur. Calls all required wildfly cli commands needed for the successful configuration of the jms
 * subsystem in the currently running wildfly profile. The cli commands that might be run during the provisioning:
 * <ul>
 * <li>/extension=org.jboss.as.messaging:add() - requires server reload</li>
 * <li>/subsystem=messaging:add - requires server reload</li>
 * <li>/subsystem=messaging/hornetq-server=default:add</li>
 * <li>/subsystem=messaging/hornetq-server=default/:write-attribute - requires server reload</li>
 * <li>/subsystem=messaging/hornetq-server=default/address-setting={queueName}:add</li>
 * <li>/subsystem=messaging/hornetq-server=default/{connector/acceptor}={connectorName}:add</li>
 * <li>/subsystem=messaging/hornetq-server=default/{connectionFactory/pooledConnectionFactory}={connectionFactoryName}:add</li>
 * <li>/subsystem=messaging/hornetq-server=default/security-setting={queueName}:add()</li>
 * <li>/subsystem=messaging/hornetq-server=default/jms-queue={queueName}:add()</li>
 * </ul>
 *
 * @author nvelkov
 * @see <a href="http://stackoverflow.com/questions/24921640/how-to-set-up-messaging-subsystem-using-cli-in-wildfly">How
 * to set up messaging subsystem using CLI in Wildfly</a>
 */
@ApplicationScoped
public class WildflyHornetQJMSProvisioner implements JmsProvisioner {

	private static final String MESSAGING_SOCKET_NAME = "messaging";

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String JMS_QUEUE = "jms-queue";
	private static final String JMS_TOPIC = "jms-topic";
	private static final String PARAM = "param";
	private static final String SOCKET_BINDING = "socket-binding";
	private static final String SERVER_ID = "server-id";
	private static final String POOLED_CONNECTION_FACTORY = "pooled-connection-factory";
	private static final String IN_VM_CONNECTION_FACTORY = "InVmConnectionFactory";
	private static final String CONNECTION_FACTORY = "connection-factory";
	private static final String TRANSACTION = "transaction";
	private static final String ENTRIES = "entries";
	private static final String CONNECTOR = "connector";
	private static final String LOCAL_CONNECTOR_NAME = "local-broker";
	private static final String RELOAD_OPERATION = "reload";
	private static final String MESSAGING_EXTENSION = "org.jboss.as.messaging";

	private static final String DEFAULT_DLQUEUE_ADDRESS = "java:/jms.queue.DLQ";
	private static final String CLIENT_ID_PROPERTY = "client-id";
	private static final String ADDRESS_SETTING = "address-setting";
	private static final String PATH = "path";
	private static final String HORNETQ_SHARED_DIR = "hornetq.shared.dir";

	private final WildflyControllerService controller;
	private final WildflyCLIUtil cliUtil;

	/**
	 * Instantiate provisioner
	 *
	 * @param controller the controller to use
	 */
	@Inject
	public WildflyHornetQJMSProvisioner(WildflyControllerService controller) {
		this.controller = controller;
		cliUtil = new WildflyCLIUtil(controller);
	}

	@Override
	public void provisionSubsystem(JmsSubsystemModel model) throws RollbackedException {
		boolean reloadRequired = false;
		ModelNode root = new ModelNode();
		root.get(OP).set(COMPOSITE);
		ModelNode steps = root.get(STEPS);

		if (isMessagingExtensionUndefined()) {
			addMessagingExtension();
			reloadRequired = true;
		}

		if (isMessagingSubsystemUndefined()) {
			addMessagingSubsystem(steps, model);
			reloadRequired = true;
		} else {
			reloadRequired |= isMessagingSubsystemChanged(steps, model);
		}

		addAcceptors(steps, model);

		if (isQueueUndefined(DEFAULT_DLQUEUE_ADDRESS)) {
			addQueueProvisioningStep(steps.add(), DEFAULT_DLQUEUE_ADDRESS);
		}

		if (steps.hasDefined(0)) {
			LOGGER.trace("Executing Wildfly JMS Provisioning steps:{}", root);
			ModelNode result = controller.execute(root);

			LOGGER.trace("Result from Wildfly JMS Provisioning steps:{}", result);
			if (WildflyCLIUtil.isSuccessful(result) && reloadRequired) {
				LOGGER.warn("Messaging subsystem needs to be registered at boot time. Reloading server.");
				throw new DeploymentException();
			}
		}
	}

	private boolean isMessagingSubsystemChanged(ModelNode steps, JmsSubsystemModel model) {
		ModelNode modelNode = new ModelNode();
		ModelNode address = addHornetQServerPath(modelNode);
		address.add(POOLED_CONNECTION_FACTORY, LOCAL_CONNECTOR_NAME);

		boolean hasChanges;
		Optional<ModelNode> change = cliUtil.checkOrUpdateValue(CLIENT_ID_PROPERTY, model.getTopicsClientId(),
				node -> addHornetQServerPath(node).add(POOLED_CONNECTION_FACTORY, LOCAL_CONNECTOR_NAME));
		change.ifPresent(steps::add);
		hasChanges = change.isPresent();

		change = cliUtil.checkOrUpdateValue(CLIENT_ID_PROPERTY, model.getTopicsClientId(),
				node -> addHornetQServerPath(node).add(CONNECTION_FACTORY, IN_VM_CONNECTION_FACTORY));
		change.ifPresent(steps::add);
		hasChanges |= change.isPresent();
		// add checks for other properties and should be updated during startup

		if (hasChanges) {
			ModelNode reload = steps.add();
			reload.get(OP).set(RELOAD_OPERATION);
			return true;
		}
		return false;
	}

	@Override
	public void provisionDestination(DestinationDefinition definition) throws RollbackedException {
		if (definition.getType() == DestinationType.QUEUE) {
			provisionDestination(definition, this::isQueueUndefined,
					WildflyHornetQJMSProvisioner::addQueueProvisioningStep);
		} else {
			provisionDestination(definition, this::isTopicUndefined,
					WildflyHornetQJMSProvisioner::addTopicProvisioningStep);
		}
	}

	@Override
	public Optional<DestinationDefinition> resolve(String address) throws RollbackedException {
		ModelNode node = new ModelNode();
		String queueName = address.substring(address.indexOf('/') + 1);
		ModelNode hornetQServerAddress = getHornetQServerPathOperation(node, ClientConstants.READ_RESOURCE_OPERATION);
		hornetQServerAddress.add(ADDRESS_SETTING, queueName);
		ModelNode execute = controller.execute(node);
		if (!WildflyCLIUtil.isSuccessful(execute)) {
			return Optional.empty();
		}
		ModelNode result = execute.get("result");
		DestinationDefinition definition = new DestinationDefinition()
				.setAddress(queueName)
				.setAddressFullPolicy(AddressFullMessagePolicyType.valueOf(result.get("address-full-policy").asString()))
				.setDeadLetterAddress(result.get("dead-letter-address").asString())
				.setExpiryAddress(result.get("expiry-address").asString())
				.setExpiryDelay(result.get("expiry-delay").asLong())
				.setLastValueQueue(result.get("last-value-queue").asBoolean())
				.setMaxRedeliveryAttempts(result.get("max-delivery-attempts").asInt())
				.setMaxRedeliveryDelay(result.get("max-redelivery-delay").asLong())
				.setMaxSize(result.get("max-size-bytes").asInt())
				.setMessageCounterHistoryDayLimit(result.get("message-counter-history-day-limit").asInt())
				.setPageMaxCacheSize(result.get("page-max-cache-size").asInt())
				.setPageSize(result.get("page-size-bytes").asLong())
				.setRedeliveryDelay(result.get("redelivery-delay").asLong())
				.setRedeliveryMultiplier(result.get("redelivery-multiplier").asDouble())
				.setType(address.toLowerCase().contains("topic") ? DestinationType.TOPIC : DestinationType.QUEUE);
		return Optional.of(definition);
	}

	@Override
	public Collection<DestinationDefinition> resolveAll() throws RollbackedException {
		ModelNode node = new ModelNode();
		getHornetQServerPathOperation(node, ClientConstants.READ_RESOURCE_OPERATION);
		ModelNode execute = controller.execute(node);
		if (!WildflyCLIUtil.isSuccessful(execute)) {
			return Collections.emptyList();
		}
		ModelNode result = execute.get("result");
		ModelNode addressSetting = result.get(ADDRESS_SETTING);
		return addressSetting.keys().stream()
				.map(this::getDestinationDefinition)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	private DestinationDefinition getDestinationDefinition(String address) {
		try {
			return resolve(address).orElse(null);
		} catch (RollbackedException e) {
			LOGGER.warn("Could not resolve address settings for {}", address, e);
			return null;
		}
	}

	private void provisionDestination(DestinationDefinition definition,
			Predicate<String> destinationFilter, BiConsumer<ModelNode, String> destinationConsumer)
			throws RollbackedException {
		if (destinationFilter.test(definition.getAddress())) {
			ModelNode root = new ModelNode();
			root.get(OP).set(COMPOSITE);
			ModelNode steps = root.get(STEPS);

			destinationConsumer.accept(steps.add(), definition.getAddress());
			// Dead letter and expiration destinations are always queues.
			addQueueProvisioningStep(steps.add(), definition.getDeadLetterAddress());
			addQueueProvisioningStep(steps.add(), definition.getExpiryAddress());
			provisionDestinationSettings(steps.add(), definition);
			controller.execute(root);
		} else {
			// for existing destinations update the address settings
			removeDestinationSettings(definition);
			ModelNode root = new ModelNode();
			provisionDestinationSettings(root, definition);
			controller.execute(root);
		}
	}

	/**
	 * Provision wildfly's messaging subsystem. The following configurations will be provisioned to wildfly's
	 * configurations:
	 * <ul>
	 * <li>Messaging extensions - needed for the subsystem</li>
	 * <li>HornetQ subsystem</li>
	 * <li>Messaging subsystem configurations with default values</li>
	 * <li>Messaging subsystem security configurations</li>
	 * <li>Local connectors</li>
	 * <li>Local connection factories</li>
	 * <li>MDB Container</li>
	 * </ul>
	 *
	 * @param steps the execution steps
	 * @param model the model
	 */
	private static void addMessagingSubsystem(ModelNode steps, JmsSubsystemModel model) {
		LOGGER.info("JMS subsystem not found. It will be added to the currently running wildfly profile.");

		// Add the messaging subsystem and hornetq paths. Those need to be in different steps because all subsequent
		// element additions must have their parents created first.
		ModelNode addSubsystemStep = steps.add();
		addSubsystemStep.get(ClientConstants.OP).set(ClientConstants.ADD);
		addMessagingSubsystemPath(addSubsystemStep);

		ModelNode addHornetQServerStep = steps.add();
		getHornetQServerPathOperation(addHornetQServerStep, ClientConstants.ADD);

		// Writing attributes to the hornetQ system requires a server reload.
		writeHornetQAttribute(steps.add(), "jmx-management-enabled", String.valueOf(model.isJmxManagementEnabled()));
		writeHornetQAttribute(steps.add(), "persistence-enabled", String.valueOf(model.isPersistenceEnabled()));
		writeHornetQAttribute(steps.add(), "security-enabled", String.valueOf(model.isSecurityEnabled()));
		writeHornetQAttribute(steps.add(), "journal-file-size", Integer.toString(model.getJournalFileSize()));

		// set custom persistent location for JMS data if configured
		String persistenceLocation = model.getPersistenceStoreLocation();
		if (StringUtils.isNotBlank(persistenceLocation)) {
			WildflyCLIUtil.addPathConfiguration(steps, HORNETQ_SHARED_DIR, persistenceLocation);
			writeHornetQDirectory(steps, "paging-directory", "paging", HORNETQ_SHARED_DIR);
			writeHornetQDirectory(steps, "bindings-directory", "bindings", HORNETQ_SHARED_DIR);
			writeHornetQDirectory(steps, "journal-directory", "journal", HORNETQ_SHARED_DIR);
			writeHornetQDirectory(steps, "large-messages-directory", "large-messages", HORNETQ_SHARED_DIR);
		}

		addSecurityConfigurations(steps);

		addConnectors(steps);

		addConnectionFactories(steps, model);

		addMDBContainer(steps);

		ModelNode reload = steps.add();
		reload.get(OP).set(RELOAD_OPERATION);
	}

	/**
	 * Add the MDB container provisioning to the execution steps.
	 *
	 * @param steps the execution steps
	 */
	private static void addMDBContainer(ModelNode steps) {
		ModelNode writeDefaultMDBInstancePoolStep = steps.add();
		addMdbContainerAddress(writeDefaultMDBInstancePoolStep);
		WildflyCLIUtil.writeAttribute(writeDefaultMDBInstancePoolStep, "default-mdb-instance-pool",
				"mdb-strict-max-pool");

		ModelNode writeDefaultResourceAdapterStep = steps.add();
		addMdbContainerAddress(writeDefaultResourceAdapterStep);
		WildflyCLIUtil.writeAttribute(writeDefaultResourceAdapterStep, "default-resource-adapter-name", "hornetq-ra");
	}

	/**
	 * Add the JMS connection factories provisioning to the execution steps. Two default connection factories are
	 * provisioned - Two for the local connector (one normal and one pooled).
	 *
	 * @param steps the execution steps.
	 * @param model the source configuration model
	 */
	private static void addConnectionFactories(ModelNode steps, JmsSubsystemModel model) {
		ModelNode addInVMConnectionFactoryStep = steps.add();
		ModelNode inVMConnectionFactoryAddress = getHornetQServerPathOperation(addInVMConnectionFactoryStep,
				ClientConstants.ADD);
		inVMConnectionFactoryAddress.add(CONNECTION_FACTORY, IN_VM_CONNECTION_FACTORY);
		if (model.getTopicsClientId() != null) {
			addInVMConnectionFactoryStep.get(CLIENT_ID_PROPERTY).set(model.getTopicsClientId());
		}
		addInVMConnectionFactoryStep.get(CONNECTOR).get(LOCAL_CONNECTOR_NAME);
		addInVMConnectionFactoryStep.get(ENTRIES).get(0).set("java:/ConnectionFactory");

		ModelNode addInVMPooledConnectionFactoryStep = steps.add();
		ModelNode inVMPooledConnectionFactoryAddress = getHornetQServerPathOperation(addInVMPooledConnectionFactoryStep,
				ClientConstants.ADD);
		inVMPooledConnectionFactoryAddress.add(POOLED_CONNECTION_FACTORY, LOCAL_CONNECTOR_NAME);
		if (model.getTopicsClientId() != null) {
			addInVMPooledConnectionFactoryStep.get(CLIENT_ID_PROPERTY).set(model.getTopicsClientId());
		}
		addInVMPooledConnectionFactoryStep.get(TRANSACTION).set("xa");
		addInVMPooledConnectionFactoryStep.get(CONNECTOR).get(LOCAL_CONNECTOR_NAME);
		addInVMPooledConnectionFactoryStep.get(ENTRIES).get(0).set("java:jboss/DefaultJMSConnectionFactory");
	}

	/**
	 * Add the connectors provisioning to the execution steps. Two default connectors are provisioned:
	 * <ul>
	 * <li>Local connector</li>
	 * <li>Http connector</li>
	 * </ul>
	 *
	 * @param steps the execution steps
	 * @see <a href="https://docs.jboss.org/author/display/WFLY10/Messaging+configuration">Connectors</a>
	 */
	private static void addConnectors(ModelNode steps) {
		ModelNode addInVMConnectorStep = steps.add();
		ModelNode inVMConnectorAddress = getHornetQServerPathOperation(addInVMConnectorStep, ClientConstants.ADD);
		inVMConnectorAddress.add("in-vm-connector", LOCAL_CONNECTOR_NAME);
		addInVMConnectorStep.get(SERVER_ID).set(0);

		ModelNode addHttpConnectorStep = steps.add();
		ModelNode httpConnectorAddress = getHornetQServerPathOperation(addHttpConnectorStep, ClientConstants.ADD);
		httpConnectorAddress.add("http-connector", "http-connector-throughput");
		addHttpConnectorStep.get(SOCKET_BINDING).set("http");
		addHttpConnectorStep.get(PARAM).get("http-upgrade-endpoint").set("http-acceptor-throughput");
		addHttpConnectorStep.get(PARAM).get("batch-delay").set(50);
	}

	private void addAcceptors(ModelNode steps, JmsSubsystemModel model) {
		ModelNode addInVMAcceptorStep = new ModelNode();
		ModelNode inVMAcceptorAddress = getHornetQServerPathOperation(addInVMAcceptorStep, ClientConstants.ADD);
		inVMAcceptorAddress.add("in-vm-acceptor", LOCAL_CONNECTOR_NAME);
		addInVMAcceptorStep.get(SERVER_ID).set(0);
		cliUtil.addIfNotAlreadyDefined(steps, addInVMAcceptorStep);

		ModelNode addHttpAcceptorStep = new ModelNode();
		ModelNode httpAcceptorAddress = getHornetQServerPathOperation(addHttpAcceptorStep, ClientConstants.ADD);
		httpAcceptorAddress.add("http-acceptor", "http-acceptor-throughput");
		addHttpAcceptorStep.get("http-listener").set("default");
		ModelNode httpAcceptorParam = addHttpAcceptorStep.get(PARAM);
		httpAcceptorParam.get("direct-deliver").set(false);
		httpAcceptorParam.get("batch-delay").set(model.getHttpBatchDelay());
		cliUtil.addIfNotAlreadyDefined(steps, addHttpAcceptorStep);

		ModelNode addMessagingSocketStep = new ModelNode();
		addMessagingSocketStep.get(ClientConstants.OP).set(ClientConstants.ADD);
		ModelNode messagingSocketPath = addMessagingSocketStep.get(OP_ADDR);
		messagingSocketPath.add(ClientConstants.SOCKET_BINDING_GROUP, "standard-sockets")
				.add(ClientConstants.SOCKET_BINDING, MESSAGING_SOCKET_NAME);
		addMessagingSocketStep.get("port").set(model.getRemoteAcceptorPort());
		cliUtil.addIfNotAlreadyDefined(steps, addMessagingSocketStep);

		ModelNode addRemoteAcceptorStep = new ModelNode();
		ModelNode remoteAcceptorAddress = getHornetQServerPathOperation(addRemoteAcceptorStep, ClientConstants.ADD);
		remoteAcceptorAddress.add("remote-acceptor", "netty");
		addRemoteAcceptorStep.get(ClientConstants.SOCKET_BINDING).set(MESSAGING_SOCKET_NAME);
		cliUtil.addIfNotAlreadyDefined(steps, addRemoteAcceptorStep);
	}

	/**
	 * Add the security configurations provisioning to the execution steps.
	 *
	 * @param steps the execution steps
	 */
	private static void addSecurityConfigurations(ModelNode steps) {
		ModelNode addSecurityStep = steps.add();
		ModelNode securityAddress = getHornetQServerPathOperation(addSecurityStep, ClientConstants.ADD);
		securityAddress.add("security-setting", "#");

		ModelNode addGuestRoleSecurityStep = steps.add();
		ModelNode guestRoleSecurityAddress = getHornetQServerPathOperation(addGuestRoleSecurityStep,
				ClientConstants.ADD);
		guestRoleSecurityAddress.add("security-setting", "#");
		guestRoleSecurityAddress.add("role", "guest");
		addGuestRoleSecurityStep.get("consume").set(true);
		addGuestRoleSecurityStep.get("create-durable-queue").set(true);
		addGuestRoleSecurityStep.get("create-non-durable-queue").set(true);
		addGuestRoleSecurityStep.get("delete-durable-queue").set(false);
		addGuestRoleSecurityStep.get("delete-non-durable-queue").set(false);
		addGuestRoleSecurityStep.get("manage").set(false);
		addGuestRoleSecurityStep.get("send").set(true);
	}

	/**
	 * Provision the messaging extension. This requires a reload.
	 *
	 * @throws RollbackedException if an exception has occurred while executing the provisioning steps
	 */
	private void addMessagingExtension() throws RollbackedException {
		LOGGER.info("JMS extension not found. It will be added to the currently running wildfly profile.");
		ModelNode addExtensionStep = new ModelNode();
		addExtensionStep.get(ClientConstants.OP).set(ClientConstants.ADD);
		ModelNode address = addExtensionStep.get(OP_ADDR);
		address.add(ClientConstants.EXTENSION, MESSAGING_EXTENSION);
		controller.execute(addExtensionStep);
	}

	/**
	 * Add queue provisioning to the execution steps.
	 *
	 * @param step the execution step
	 * @param queueAddress the queue address.
	 */
	private static void addQueueProvisioningStep(ModelNode step, String queueAddress) {
		addDestinationProvisioningStep(step, queueAddress, JMS_QUEUE);
	}

	/**
	 * Add topic provisioning to the execution steps.
	 *
	 * @param step the execution step
	 * @param topicAddress the topic address.
	 */
	private static void addTopicProvisioningStep(ModelNode step, String topicAddress) {
		addDestinationProvisioningStep(step, topicAddress, JMS_TOPIC);
	}

	private static void addDestinationProvisioningStep(ModelNode step, String destinationAddress,
			String destinationType) {
		LOGGER.info("Adding destination - {} - {} to messaging subsystem", destinationAddress, destinationType);
		String destinationName = destinationAddress.substring(destinationAddress.lastIndexOf('.') + 1);
		ModelNode hornetQServerAddress = getHornetQServerPathOperation(step, ClientConstants.ADD);
		hornetQServerAddress.add(destinationType, destinationName);
		step.get(ENTRIES).get(0).set(destinationAddress);
	}

	private void removeDestinationSettings(DestinationDefinition definition) throws RollbackedException {
		// for queue in format java:/jms.queue.SomeQueueName
		// removes settings with address: jms.queue.SomeQueueName
		ModelNode node = new ModelNode();
		String queueAddress = definition.getAddress();
		String queueName = queueAddress.substring(queueAddress.indexOf('/') + 1);
		ModelNode hornetQServerAddress = getHornetQServerPathOperation(node, ClientConstants.REMOVE_OPERATION);
		hornetQServerAddress.add(ADDRESS_SETTING, queueName);
		ModelNode execute = controller.execute(node);

		// old 'wrong' format of settings address: SomeQueueName
		// cleans old invalid configurations. after we insert the new format this bellow should not be called
		if (!WildflyCLIUtil.isSuccessful(execute)) {
			node = new ModelNode();
			queueName = queueAddress.substring(queueAddress.lastIndexOf('.') + 1);
			hornetQServerAddress = getHornetQServerPathOperation(node, ClientConstants.REMOVE_OPERATION);
			hornetQServerAddress.add(ADDRESS_SETTING, queueName);
			controller.execute(node);
		}
	}

	/**
	 * Add address-settings provisioning to the execution steps.
	 */
	private static void provisionDestinationSettings(ModelNode step, DestinationDefinition definition) {
		String queueAddress = definition.getAddress();
		String queueName = queueAddress.substring(queueAddress.indexOf('/') + 1);
		ModelNode hornetQServerAddress = getHornetQServerPathOperation(step, ClientConstants.ADD);
		hornetQServerAddress.add(ADDRESS_SETTING, queueName);
		step.get("dead-letter-address").set(convertToAddressSettingsAddress(definition.getDeadLetterAddress()));
		step.get("expiry-address").set(convertToAddressSettingsAddress(definition.getExpiryAddress()));
		if (definition.getMaxSize() > 0) {
			step.get("max-size-bytes").set(definition.getMaxSize());
		}
		if (definition.getPageSize() > 0) {
			step.get("page-size-bytes").set(definition.getPageSize());
		}
		if (definition.getMessageCounterHistoryDayLimit() > 0) {
			step.get("message-counter-history-day-limit").set(definition.getMessageCounterHistoryDayLimit());
		}
		if (definition.getMaxRedeliveryAttempts() > 0) {
			step.get("max-delivery-attempts").set(definition.getMaxRedeliveryAttempts());
		}
		if (definition.getRedeliveryDelay() > 0) {
			step.get("redelivery-delay").set(definition.getRedeliveryDelay());
		}
		if (definition.getRedeliveryMultiplier() > 0) {
			step.get("redelivery-multiplier").set(definition.getRedeliveryMultiplier());
		}
		if (definition.getAddressFullPolicy() != null) {
			step.get("address-full-policy").set(definition.getAddressFullPolicy().toString());
		}
		if (definition.getExpiryDelay() > 0) {
			step.get("expiry-delay").set(definition.getExpiryDelay());
		}
		if (definition.getMaxRedeliveryDelay() > 0) {
			step.get("max-redelivery-delay").set(definition.getMaxRedeliveryDelay());
		}
		if (definition.getPageMaxCacheSize() > 0) {
			step.get("page-max-cache-size").set(definition.getPageMaxCacheSize());
		}
	}

	private static String convertToAddressSettingsAddress(String queueAddress) {
		return queueAddress.substring(queueAddress.indexOf('/') + 1);
	}

	private static void addMdbContainerAddress(ModelNode step) {
		ModelNode mdbContainerAddress = step.get(OP_ADDR);
		mdbContainerAddress.add(ClientConstants.SUBSYSTEM, "ejb3");
	}

	private static ModelNode getHornetQServerPathOperation(ModelNode step, String operation) {
		step.get(ClientConstants.OP).set(operation);
		return addHornetQServerPath(step);
	}

	private static void writeHornetQAttribute(ModelNode step, String name, String value) {
		LOGGER.info("Adding attribute to Messaging subsystem: {}={}", name, value);
		addHornetQServerPath(step);
		WildflyCLIUtil.writeAttribute(step, name, value);
	}

	private static void writeHornetQDirectory(ModelNode steps, String dirName, String path, String relativeTo) {
		LOGGER.info("Adding location attribute to Messaging subsystem: {} path={}, relativeTo={}", dirName, path, relativeTo);
		ModelNode step = steps.add();
		addHornetQServerPath(step).add(PATH, dirName);
		step.get(ClientConstants.OP).set(ClientConstants.ADD);
		step = steps.add();
		addHornetQServerPath(step).add(PATH, dirName);
		WildflyCLIUtil.writeAttribute(step, PATH, path);
		if (relativeTo != null) {
			step = steps.add();
			addHornetQServerPath(step).add(PATH, dirName);
			WildflyCLIUtil.writeAttribute(step, "relative-to", relativeTo);
		}
	}

	private static ModelNode addHornetQServerPath(ModelNode node) {
		ModelNode messagingSubsystemPath = addMessagingSubsystemPath(node);
		return messagingSubsystemPath.add("hornetq-server", "default");
	}

	private static ModelNode addMessagingSubsystemPath(ModelNode node) {
		ModelNode rootPath = node.get(OP_ADDR);
		return rootPath.add(ClientConstants.SUBSYSTEM, MESSAGING_SOCKET_NAME);
	}

	private boolean isMessagingExtensionUndefined() throws RollbackedException {
		ModelNode root = new ModelNode();

		root.get(OP).set(READ_RESOURCE_OPERATION);
		ModelNode address = root.get(OP_ADDR);
		address.add(ClientConstants.EXTENSION, MESSAGING_EXTENSION);

		ModelNode result = controller.execute(root);
		return !WildflyCLIUtil.isSuccessful(result);
	}

	private boolean isMessagingSubsystemUndefined() throws RollbackedException {
		ModelNode root = new ModelNode();

		root.get(OP).set(READ_RESOURCE_OPERATION);
		addMessagingSubsystemPath(root);
		ModelNode result = controller.execute(root);
		return !WildflyCLIUtil.isSuccessful(result);
	}

	private boolean isQueueUndefined(String queueAddress) {
		return isDestinationUndefined(queueAddress, JMS_QUEUE);
	}

	private boolean isTopicUndefined(String topicAddress) {
		return isDestinationUndefined(topicAddress, JMS_TOPIC);
	}

	private boolean isDestinationUndefined(String destinationAddress, String destinationType) {
		ModelNode root = new ModelNode();
		root.get(OP).set(READ_RESOURCE_OPERATION);
		String destinationName = destinationAddress.substring(destinationAddress.lastIndexOf('.') + 1);
		ModelNode destinationAddressNode = getHornetQServerPathOperation(root, ClientConstants.READ_RESOURCE_OPERATION);
		destinationAddressNode.add(destinationType, destinationName);
		return cliUtil.isAddressUndefined(destinationAddressNode);

	}

}
