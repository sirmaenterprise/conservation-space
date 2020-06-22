package com.sirmaenterprise.sep.jms.convert;

import java.io.InputStream;

import javax.jms.BytesMessage;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;

/**
 * A bytes message writer that produces {@link BytesMessage} instances. Used for streaming large
 * messages. Since HornetQ supports sending and receiving of huge messages, even when running with
 * limited memory, the inputstream will be put in the object's body with a special hornetq key that
 * will enable hornetq processing.
 * 
 * @author nvelkov
 * @param <D>
 *            the data type
 * @see https://docs.jboss.org/hornetq/2.2.5.Final/user-manual/en/html/large-messages.html
 */
public interface BytesMessageWriter<D> extends MessageWriter<D, BytesMessage> {

	/**
	 * Default {@link BytesMessageWriter} instance that writes {@link InputStream} entries to
	 * {@link BytesMessage}<br>
	 *
	 * @param <D>
	 *            the data type
	 * @param <M>
	 *            message type
	 * @return a writer that can write only map instances.
	 */
	static <D, M extends Message> MessageWriter<D, M> instance() {
		return DefaultBytesMessageWriter.INSTANCE;
	}

	@Override
	default BytesMessage write(D data, JMSContext context) throws JMSException {
		BytesMessage streamMessage = context.createBytesMessage();
		write(data, streamMessage);
		return streamMessage;
	}

	/**
	 * Writes the data as an object property in the message.
	 * 
	 * @param data
	 *            the data to be written
	 * @param message
	 *            the message in which to write the data
	 * @throws JMSException
	 *             if the JMS provider fails to set the property due to some internal error.
	 */
	void write(D data, BytesMessage message) throws JMSException;

	/**
	 * Default implementation that writes {@link InputStream} to {@link BytesMessage}.
	 * 
	 * @author nvelkov
	 */
	class DefaultBytesMessageWriter implements BytesMessageWriter<InputStream> {
		static final MessageWriter INSTANCE = new DefaultBytesMessageWriter();

		@Override
		public void write(InputStream data, BytesMessage message) throws JMSException {
			message.setObjectProperty("JMS_HQ_InputStream", data);
		}
	}
}
