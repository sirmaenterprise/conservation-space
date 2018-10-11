package com.sirmaenterprise.sep.jms.convert;

import java.io.Serializable;

import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;

/**
 * Special message writer that produces {@link ObjectMessage} instances. The extension defines a convenience method for
 * simplified and type safe writer implementations that produce only {@link ObjectMessage}s.
 * <br>A default implementation is a writer that sets the given data as body to the message without any modifications.
 *
 * @param <D> the input data type that can be handled by the writer instance
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 19/05/2017
 */
public interface ObjectMessageWriter<D> extends MessageWriter<D, ObjectMessage> {

	/**
	 * Default write implementation that writes serializable data to the result message without any changes.<br>If
	 * the data is not serializable then {@link IllegalArgumentException} will be thrown
	 *
	 * @param <D> the data type
	 * @param <M> message type
	 * @return a default object writer implementation
	 */
	@SuppressWarnings("unchecked")
	static <D, M extends Message> MessageWriter<D, M> instance() {
		return (MessageWriter<D, M>) DefaultObjectMessageWriter.INSTANCE;
	}

	@Override
	default ObjectMessage write(D data, JMSContext context) throws JMSException {
		ObjectMessage objectMessage = context.createObjectMessage();
		write(data, objectMessage);
		return objectMessage;
	}

	/**
	 * Do the actual data writing to the given {@link ObjectMessage}
	 *
	 * @param data the data to write to the message
	 * @param message the message that will be send and need to be filled
	 * @throws JMSException when error occurs while manipulating the message or the data types written to the message
	 * are invalid. For more information check the {@link ObjectMessage} documentation
	 * @see ObjectMessage
	 */
	void write(D data, ObjectMessage message) throws JMSException;

	/**
	 * Default implementation that writes a {@link Serializable} instance to a {@link ObjectMessage}. If the passed
	 * data is not serializable a {@link IllegalArgumentException} will be thrown.
	 *
	 * @author BBonev
	 */
	class DefaultObjectMessageWriter implements ObjectMessageWriter<Object> {
		static final MessageWriter INSTANCE = new DefaultObjectMessageWriter();

		@Override
		public void write(Object data, ObjectMessage message) throws JMSException {
			if (data instanceof Serializable) {
				message.setObject((Serializable) data);
			} else {
				throw new IllegalArgumentException(ObjectMessage.class + " payload should be serializable");
			}
		}
	}
}
