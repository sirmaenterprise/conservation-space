package com.sirmaenterprise.sep.jms.provision;

import java.util.Collection;
import java.util.Optional;

import com.sirma.itt.seip.exception.RollbackedException;

/**
 * Jms Provisioner responsible for provisioning whatever configurations the server needs for
 * activating it's jms system.
 *
 * @author nvelkov
 */
public interface JmsProvisioner {

	/**
	 * Provision the messaging extension and subsystem when starting the server. This needs to occur
	 * before everything else because if a reload is required and something else is running, it
	 * might lead to unexpected behavior.
	 *
	 * @param model the jms subsystem model
	 * @throws RollbackedException if an exception has occurred while executing the provisioning steps
	 */
	void provisionSubsystem(JmsSubsystemModel model) throws RollbackedException;

	/**
	 * Provision the given destination to the underlying jms system. The destination address must be in the following
	 * format: java:/jms.queue.{queueName} or java:/jms.topic.{topicName}. For each destination, two more queues will
	 * be added - one dead letter queue and one expiry queue, respectively named {destinationName}_DLQ
	 * and {destinationName}_EQ.
	 *
	 * @param destinationDefinition the definition of the destination to be provisioned
	 */
	void provisionDestination(DestinationDefinition destinationDefinition) throws RollbackedException;

	/**
	 * Resolve JMS destination and it's address settings
	 *
	 * @param address the address name
	 * @return the found destination definition
	 * @throws RollbackedException in case of error during information retrieval
	 */
	Optional<DestinationDefinition> resolve(String address) throws RollbackedException;

	/**
	 * Resolve all valid address settings found in the JMS sub system
	 *
	 * @return the list of all address definitions
	 * @throws RollbackedException in case of error during information retrieval
	 */
	Collection<DestinationDefinition> resolveAll() throws RollbackedException;
}
