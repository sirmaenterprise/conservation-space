package com.sirmaenterprise.sep.jms.api;

import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.Message;

/**
 * Generic representation of receiver that need to receive JMS messages. This is the internal representation of a
 * consumer instance. Links the actual {@link MessageConsumer} with the receiver properties.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 16/05/2017
 */
public interface ReceiverDefinition {
	/**
	 * Defines the receiver destination JNDI name. This will be used to look up the JMS {@link javax.jms.Destination}
	 * that will be read
	 *
	 * @return the destination JNDI
	 */
	String getDestinationJndi();

	/**
	 * The maximum desired concurrent receivers to be active when waiting for messages. Note that the implementation
	 * could enforce some limit on the return  value from this method for performance reasons.
	 *
	 * @return the desired concurrency level
	 */
	int getConcurrenceLevel();

	/**
	 * A message selector to use when reading the {@link javax.jms.Destination}.
	 * <br> Check the {@link Message} documentation for more details on the selector format.
	 *
	 * @return an optional message selector. Empty string or null will be ignored
	 * @see Message
	 */
	String getSelector();

	/**
	 * The original message consumer that should receive the incoming messages from the defined destination and
	 * selector.
	 * @return the consumer instance, should not be null
	 */
	MessageConsumer getMessageConsumer();

	/**
	 * Create a {@link JMSConsumer} that can accept messages from the given destination. This is used so that different
	 * definitions to provide their own way to construct {@link JMSConsumer} instances.
	 * <br> The implementation should provide a selector if needed and all other configurations needed for topic reading.
	 *
	 * @param destination the resolved destination object that should be read. It should point to the same destination as
	 * the described in the current definition.
	 * @param jmsContext the {@link JMSContext} that should be used for message receiving
	 * @return a consumer that should be capable of reading the given destination.
	 */
	JMSConsumer createConsumer(Destination destination, JMSContext jmsContext);

	/**
	 * Can be used to specify the transaction timeout while processing the incoming messages in seconds. The default
	 * timeout is the global transaction timeout for the application JTA environment.
	 *
	 * @return the value to use for transaction timeout in seconds. If used {@code 0} then the default value will be used
	 */
	int getTransactionTimeout();
}
