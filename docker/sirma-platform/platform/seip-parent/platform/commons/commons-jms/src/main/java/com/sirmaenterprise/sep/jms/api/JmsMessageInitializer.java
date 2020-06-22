package com.sirmaenterprise.sep.jms.api;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * JMS message builder that can be used for message population. The {@link #initialize(Message)} method will be called
 * with the message instance to populate with data and/or properties.<br>This interface is intended to allow lambda
 * use when simple message building is needed when sending messages.
 *
 * @param <M> the message type
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 19/05/2017
 */
@FunctionalInterface
public interface JmsMessageInitializer<M extends Message> {

	/**
	 * Populate the given message with properties and/or payload
	 *
	 * @param message the message to prepare
	 * @throws JMSException when manipulating the message properties and/or payload
	 */
	void initialize(M message) throws JMSException;
}
