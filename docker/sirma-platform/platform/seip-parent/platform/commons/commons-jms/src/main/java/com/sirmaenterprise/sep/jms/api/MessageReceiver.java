package com.sirmaenterprise.sep.jms.api;

/**
 * Defines message receiver that will provide a message to a message consumer and message is read
 * from a specific destination.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 13/05/2017
 */
public interface MessageReceiver {
	/**
	 * Read a message from the destination if such is available. If the destination is empty then
	 * the method will return without calling the consumer.
	 *
	 * @param messageConsumer
	 *            the consumer to call if message is available.
	 * @return the message receiver response
	 */
	MessageReceiverResponse readMessage(MessageConsumer messageConsumer);

	/**
	 * Reads a message from the destination if such is available. If the destination is empty will
	 * wait for message to be available. Note that this method does not support retries if message
	 * processing fails as transaction cannot be started and kept open for a long period of time.
	 *
	 * @param messageConsumer
	 *            to call when the message arrives.
	 * @return the message receiver response
	 */
	MessageReceiverResponse waitMessage(MessageConsumer messageConsumer);

	/**
	 * Reads a message from the destination if such is available. If the destination is empty will
	 * wait for message to be available the specified time in milliseconds before returning. The
	 * requested timeout should be less then transaction timeout threshold. Otherwise the
	 * transaction will be aborted when the message arrives and the consumer will not be called.
	 *
	 * @param messageConsumer
	 *            to call when the message arrives.
	 * @param timeoutInMilliseconds
	 *            the time in milliseconds to wait for messages before returning. Suggested value is
	 *            {@code 2000}
	 * @return the message receiver response
	 */
	MessageReceiverResponse waitMessage(MessageConsumer messageConsumer, long timeoutInMilliseconds);
}
