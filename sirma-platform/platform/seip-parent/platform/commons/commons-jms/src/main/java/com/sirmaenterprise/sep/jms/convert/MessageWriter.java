package com.sirmaenterprise.sep.jms.convert;

import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;

/**
 * Defines a generic message writer that can be called to convert/write a given data to any JMS {@link Message}
 * .<br>When called to the write will be passed a valid {@link JMSContext} that can be used to create a desired
 * message type. It should populate the message and return it as output of the method call.<br>The writer
 * implementation should be stateless and allowing concurrent invocations to the same instance.
 *
 * @param <D> the input data type that can be handled by the writer instance
 * @param <M> the output message type
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 18/05/2017
 */
public interface MessageWriter<D, M extends Message> {
	/**
	 * Convert and write a given data to a JMS {@link Message}. The desired message type could be created using the
	 * given {@link JMSContext}. <br> Not that the writer should not store or reuse the JMS context instance as it
	 * will be closed at the end of the active transaction.
	 *
	 * @param data the data to write to the result message
	 * @param context the context instance to use for message building
	 * @return the build message, if the method retuns null it will be considered an application error
	 * @throws JMSException in case the JMS api throws an error while using the context and/or message
	 */
	M write(D data, JMSContext context) throws JMSException;
}
