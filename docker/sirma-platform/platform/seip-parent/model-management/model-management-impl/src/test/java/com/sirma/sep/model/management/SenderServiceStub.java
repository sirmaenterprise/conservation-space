package com.sirma.sep.model.management;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;

import java.io.Serializable;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

import org.junit.Assert;

import com.sirmaenterprise.sep.jms.api.SendOptions;
import com.sirmaenterprise.sep.jms.api.SenderService;

/**
 * Stub for {@link SenderService}. Provides means of registering handlers on queue destinations
 * to be executed synchronously on message send to that destination.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 02/08/2018
 */
public class SenderServiceStub {

	private final SenderService senderService;

	public SenderServiceStub(SenderService senderService) {
		this.senderService = senderService;
	}

	public void registerSyncQueueHandler(String destination, MessageConsumer messageConsumer) {
		doAnswer(a -> {
			String body = a.getArgumentAt(1, String.class);
			SendOptions options = a.getArgumentAt(2, SendOptions.class);
			Message message = buildMessage(body, options.getProperties(), null);
			consumeSync(messageConsumer, message);
			return null;
		}).when(senderService).sendText(eq(destination), anyString(), any(SendOptions.class));

		doAnswer(a -> {
			String body = a.getArgumentAt(1, String.class);
			Message message = buildMessage(body, null, null);
			consumeSync(messageConsumer, message);
			return null;
		}).when(senderService).sendText(eq(destination), anyString());
	}

	public void registerQueueHandler(String destination, MessageConsumer messageConsumer) {
		doAnswer(a -> {
			String body = a.getArgumentAt(1, String.class);
			SendOptions options = a.getArgumentAt(2, SendOptions.class);
			Message message = buildMessage(body, options.getProperties(), null);
			consumeAsync(messageConsumer, message);
			return null;
		}).when(senderService).sendText(eq(destination), anyString(), any(SendOptions.class));

		doAnswer(a -> {
			String body = a.getArgumentAt(1, String.class);
			Message message = buildMessage(body, null, null);
			consumeAsync(messageConsumer, message);
			return null;
		}).when(senderService).sendText(eq(destination), anyString());
	}

	public void registerQueueHandler(String destination, MessageConsumer messageConsumer, String replyTo,
			MessageConsumer replyConsumer) {
		doAnswer(a -> {
			String body = a.getArgumentAt(1, String.class);
			SendOptions options = a.getArgumentAt(2, SendOptions.class);
			Message message = buildMessage(body, options.getProperties(), replyTo);
			consumeAsync(messageConsumer, message);
			return null;
		}).when(senderService).sendText(eq(destination), anyString(), any(SendOptions.class));

		doAnswer(a -> {
			String body = a.getArgumentAt(1, String.class);
			Message message = buildMessage(body, null, replyTo);
			consumeAsync(messageConsumer, message);
			return null;
		}).when(senderService).sendText(eq(destination), anyString());

		doAnswer(a -> {
			String body = a.getArgumentAt(1, String.class);
			Message message = buildMessage(body, null, null);
			consumeAsync(replyConsumer, message);
			return null;
		}).when(senderService).send(eq(new DummyDestination(replyTo)), any());
	}

	private Message buildMessage(Object body, Map<String, Serializable> properties, String replyTo) throws
			JMSException {
		MessageFake message = new MessageFake();
		message.setBody(body);
		if (replyTo != null) {
			message.setJMSReplyTo(new DummyDestination(replyTo));
		}
		if (properties != null) {
			message.properties.putAll(properties);
		}
		return message;
	}

	private void consumeAsync(MessageConsumer messageConsumer, Message message) {
		Thread currentThread = Thread.currentThread();
		Thread thread = new Thread(() -> {
			try {
				messageConsumer.accept(message);
			} catch (Exception e) {
				e.printStackTrace();
				// interrupt the calling thread so that if it waits for something that is not going to happen
				currentThread.interrupt();
				Assert.fail(e.getMessage());
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	private void consumeSync(MessageConsumer messageConsumer, Message message) {
		try {
			messageConsumer.accept(message);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public interface MessageConsumer {
		void accept(Message message) throws JMSException;
	}

	private class MessageFake implements Message {
		private Map<String, Object> properties = new HashMap<>();

		@Override
		public String getJMSMessageID() throws JMSException {
			return (String) properties.get("JMSMessageID");
		}

		@Override
		public void setJMSMessageID(String id) throws JMSException {
			properties.put("JMSMessageID", id);
		}

		@Override
		public long getJMSTimestamp() throws JMSException {
			return 0;
		}

		@Override
		public void setJMSTimestamp(long timestamp) throws JMSException {

		}

		@Override
		public byte[] getJMSCorrelationIDAsBytes() throws JMSException {
			return new byte[0];
		}

		@Override
		public void setJMSCorrelationIDAsBytes(byte[] correlationID) throws JMSException {

		}

		@Override
		public void setJMSCorrelationID(String correlationID) throws JMSException {

		}

		@Override
		public String getJMSCorrelationID() throws JMSException {
			return null;
		}

		@Override
		public Destination getJMSReplyTo() throws JMSException {
			return (Destination) properties.get("JMSReplyTo");
		}

		@Override
		public void setJMSReplyTo(Destination replyTo) throws JMSException {
			properties.put("JMSReplyTo", replyTo);
		}

		@Override
		public Destination getJMSDestination() throws JMSException {
			return null;
		}

		@Override
		public void setJMSDestination(Destination destination) throws JMSException {

		}

		@Override
		public int getJMSDeliveryMode() throws JMSException {
			return 0;
		}

		@Override
		public void setJMSDeliveryMode(int deliveryMode) throws JMSException {

		}

		@Override
		public boolean getJMSRedelivered() throws JMSException {
			return false;
		}

		@Override
		public void setJMSRedelivered(boolean redelivered) throws JMSException {

		}

		@Override
		public String getJMSType() throws JMSException {
			return null;
		}

		@Override
		public void setJMSType(String type) throws JMSException {

		}

		@Override
		public long getJMSExpiration() throws JMSException {
			return 0;
		}

		@Override
		public void setJMSExpiration(long expiration) throws JMSException {

		}

		@Override
		public long getJMSDeliveryTime() throws JMSException {
			return 0;
		}

		@Override
		public void setJMSDeliveryTime(long deliveryTime) throws JMSException {

		}

		@Override
		public int getJMSPriority() throws JMSException {
			return 0;
		}

		@Override
		public void setJMSPriority(int priority) throws JMSException {

		}

		@Override
		public void clearProperties() throws JMSException {

		}

		@Override
		public boolean propertyExists(String name) throws JMSException {
			return properties.containsKey(name);
		}

		@Override
		public boolean getBooleanProperty(String name) throws JMSException {
			return (boolean) properties.getOrDefault(name, false);
		}

		@Override
		public byte getByteProperty(String name) throws JMSException {
			return (byte) properties.getOrDefault(name, 0);
		}

		@Override
		public short getShortProperty(String name) throws JMSException {
			return (short) properties.getOrDefault(name, 0);
		}

		@Override
		public int getIntProperty(String name) throws JMSException {
			return (int) properties.getOrDefault(name, 0);
		}

		@Override
		public long getLongProperty(String name) throws JMSException {
			return (long) properties.getOrDefault(name, 0L);
		}

		@Override
		public float getFloatProperty(String name) throws JMSException {
			return (float) properties.getOrDefault(name, 0.0F);
		}

		@Override
		public double getDoubleProperty(String name) throws JMSException {
			return (double) properties.getOrDefault(name, 0.0);
		}

		@Override
		public String getStringProperty(String name) throws JMSException {
			return (String) properties.get(name);
		}

		@Override
		public Object getObjectProperty(String name) throws JMSException {
			return properties.get(name);
		}

		@Override
		public Enumeration getPropertyNames() throws JMSException {
			return Collections.emptyEnumeration();
		}

		@Override
		public void setBooleanProperty(String name, boolean value) throws JMSException {
			properties.put(name, value);
		}

		@Override
		public void setByteProperty(String name, byte value) throws JMSException {
			properties.put(name, value);
		}

		@Override
		public void setShortProperty(String name, short value) throws JMSException {
			properties.put(name, value);
		}

		@Override
		public void setIntProperty(String name, int value) throws JMSException {
			properties.put(name, value);
		}

		@Override
		public void setLongProperty(String name, long value) throws JMSException {
			properties.put(name, value);
		}

		@Override
		public void setFloatProperty(String name, float value) throws JMSException {
			properties.put(name, value);
		}

		@Override
		public void setDoubleProperty(String name, double value) throws JMSException {
			properties.put(name, value);
		}

		@Override
		public void setStringProperty(String name, String value) throws JMSException {
			properties.put(name, value);
		}

		@Override
		public void setObjectProperty(String name, Object value) throws JMSException {
			properties.put(name, value);
		}

		@Override
		public void acknowledge() throws JMSException {

		}

		@Override
		public void clearBody() throws JMSException {

		}

		@Override
		public <T> T getBody(Class<T> c) throws JMSException {
			return c.cast(properties.get("body"));
		}

		void setBody(Object body) {
			properties.put("body", body);
		}

		@Override
		public boolean isBodyAssignableTo(Class c) throws JMSException {
			return c.isInstance(properties.get("body"));
		}
	}

	private class DummyDestination implements Destination {
		private final String jndi;

		private DummyDestination(String jndi) {
			this.jndi = jndi;
		}

		@Override
		public String toString() {
			return jndi;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof DummyDestination)) {
				return false;
			}
			DummyDestination that = (DummyDestination) o;
			return Objects.equals(jndi, that.jndi);
		}

		@Override
		public int hashCode() {
			return Objects.hash(jndi);
		}
	}
}
