package com.sirmaenterprise.sep.jms.api;

import javax.jms.JMSContext;
import javax.jms.Message;

/**
 * Defines a generic message consumer that will be used to deliver {@link Message}s from {@link MessageReceiver}
 * instances.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 13/05/2017
 */
public interface MessageConsumer {

	/**
	 * Accepts the given message and the JMS context that was used for receiving the message
	 *
	 * @param message the received message. Never null
	 * @param context the receiving context. Never null
	 */
	void accept(Message message, JMSContext context);

	/**
	 * Expected message type if the consumer wishes a sub type of {@link Message}
	 *
	 * @return the message type class to accept
	 */
	default Class<? extends Message> getExpectedType() {
		return Message.class;
	}
}
