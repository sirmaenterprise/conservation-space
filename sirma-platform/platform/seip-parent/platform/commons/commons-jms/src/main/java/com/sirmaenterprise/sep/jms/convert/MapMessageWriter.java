package com.sirmaenterprise.sep.jms.convert;

import java.io.Serializable;
import java.util.Map;

import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

/**
 * Special message writer that produces {@link MapMessage} instances. The extension defines a convenience method for
 * simplified and type safe writer implementations that produce only {@link MapMessage}s.
 * <br>A default implementation is a writer that puts all {@link Map} entries to the result message.
 *
 * @param <D> the input data type that can be handled by the writer instance
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 19/05/2017
 */
public interface MapMessageWriter<D> extends MessageWriter<D, MapMessage> {

	/**
	 * Default {@link MapMessageWriter} instance that writes {@link Map} entries to {@link MapMessage}<br>If
	 * the data is not a {@link Map} instance then {@link IllegalArgumentException} will be thrown
	 *
	 * @param <D> the data type
	 * @param <M> message type
	 * @return a writer that can write only map instances.
	 */
	@SuppressWarnings("unchecked")
	static <D, M extends Message> MessageWriter<D, M> instance() {
		return (MessageWriter<D, M>) MapMessageWriter.DefaultMapMessageWriter.INSTANCE;
	}

	@Override
	default MapMessage write(D data, JMSContext context) throws JMSException {
		MapMessage mapMessage = context.createMapMessage();
		write(data, mapMessage);
		return mapMessage;
	}

	/**
	 * Do the actual data writing to the given {@link MapMessage}
	 *
	 * @param data the data to write to the message
	 * @param message the message that will be send and need to be filled
	 * @throws JMSException when error occurs while manipulating the message or the data types written to the message
	 * are invalid. For more information check the {@link MapMessage} documentation
	 * @see MapMessage
	 */
	void write(D data, MapMessage message) throws JMSException;

	/**
	 * Default implementation that writes a {@link Map} entries to a {@link MapMessage}
	 *
	 * @author BBonev
	 */
	class DefaultMapMessageWriter implements MapMessageWriter<Object> {
		static final MessageWriter INSTANCE = new DefaultMapMessageWriter();

		@Override
		public void write(Object data, MapMessage message) throws JMSException {
			if (data instanceof Map) {
				for (Map.Entry<String, Serializable> entry :((Map<String, Serializable>) data).entrySet()) {
					message.setObject(entry.getKey(), entry.getValue());
				}
			} else {
				throw new IllegalArgumentException("Cannot writer non java.util.Map instances");
			}
		}
	}
}
