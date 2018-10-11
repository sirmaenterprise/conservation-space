package com.sirmaenterprise.sep.jms.provision;

import java.lang.invoke.MethodHandles;
import java.util.Map;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;
import com.sirma.itt.seip.tasks.TransactionMode;
import com.sirmaenterprise.sep.jms.impl.JmsDefinitionProvider;

/**
 * Initial jms provisioner that must be executed on server start before everything else and is
 * responsible for provisioning the jms subsystem and queues. <b> The subsystem won't be provisioned
 * if there are no queues defined. </b>
 *
 * @author nvelkov
 */
public class InitialJmsProvisioner {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private Instance<JmsProvisioner> jmsProvisionerInstance;

	@Inject
	private JmsDefinitionProvider jmsProvider;

	/**
	 * Provision the jms subsystem on server startup.
	 *
	 * @throws RollbackedException
	 *             if an exception has occurred while executing the provisioning steps
	 */
	@Startup(phase = StartupPhase.BEFORE_APP_START, order = -1000, transactionMode = TransactionMode.NOT_SUPPORTED)
	public void provisionJmsSubsystem() throws RollbackedException {
		if (jmsProvisionerInstance.isUnsatisfied()) {
			LOGGER.warn("No JMS Provisioner available. JMS subsystem won't be provisioned.");
			return;
		}

		Map<String, DestinationDefinition> destinationAddressToType = jmsProvider.getAddresses();
		if (CollectionUtils.isEmpty(destinationAddressToType)) {
			LOGGER.info("No JMS Consumers found. Messaging subsystem won't be provisioned.");
			return;
		}

		jmsProvisionerInstance.get().provisionSubsystem(new JmsSubsystemModel());
		JmsProvisioner jmsProvisioner = jmsProvisionerInstance.get();
		for (DestinationDefinition definition : destinationAddressToType.values()) {
			jmsProvisioner.provisionDestination(definition);
		}
	}
}
