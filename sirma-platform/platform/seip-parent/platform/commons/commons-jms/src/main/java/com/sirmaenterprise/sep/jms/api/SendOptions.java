package com.sirmaenterprise.sep.jms.api;

import static com.sirma.itt.seip.collections.CollectionUtils.isNotEmpty;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.jms.CompletionListener;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.Message;

import com.sirmaenterprise.sep.jms.convert.MapMessageWriter;
import com.sirmaenterprise.sep.jms.convert.MessageWriter;
import com.sirmaenterprise.sep.jms.convert.ObjectMessageWriter;
import com.sirmaenterprise.sep.jms.security.SecurityMode;

/**
 * Configuration object that can be used to augment the JMS message sending by providing additional configurations to
 * the send process.<br>The default values are enough for sending durable messages and using the default data
 * converters.<p>Passing instance of this class to one of the send methods of the {@link SenderService} or
 * {@link MessageSender} will override any default properties and thay will affect only that send message</p>The
 * defined configurations are properties and options set to the {@link javax.jms.JMSProducer} and {@link Message}
 * instance itself.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 18/05/2017
 */
public class SendOptions {

	private MessageWriter customWriter;
	private long deliveryDelay = Message.DEFAULT_DELIVERY_DELAY;
	private long timeToLive = Message.DEFAULT_TIME_TO_LIVE;
	private int deliveryMode = Message.DEFAULT_DELIVERY_MODE;
	private int priority = Message.DEFAULT_PRIORITY;
	private CompletionListener completionListener;
	private Map<String, Serializable> properties;
	private String correlationId;
	private boolean sendCorrelationId = true;
	private String replyTo;
	private String jmsType;
	private SecurityMode securityMode = SecurityMode.DEFAULT;

	/**
	 * Create an options object using it's default values and configurations.
	 *
	 * @return a instance with default properties.
	 */
	public static SendOptions create() {
		return new SendOptions();
	}

	/**
	 * Initialize new {@code SendOptions} instance by copying all properties from the source instance.
	 *
	 * @param options the source instance to copy. If null a default properties will be returned.
	 * @return send options copy
	 */
	public static SendOptions from(SendOptions options) {
		SendOptions copy = create();
		copy.priority = options.getPriority();
		copy.customWriter = options.getWriter();
		copy.timeToLive = options.getTimeToLive();
		copy.jmsType = options.getJmsType();
		copy.replyTo = options.getReplyTo();
		copy.completionListener = options.getCompletionListener();
		copy.deliveryMode = options.getDeliveryMode();
		copy.deliveryDelay = options.getDeliveryDelay();
		copy.sendCorrelationId = options.isCorrelationIdSendAllowed();
		copy.correlationId = options.getCorrelationId();
		copy.securityMode = options.getSecurityMode();
		if (isNotEmpty(options.getProperties())) {
			copy.getOrCreateProperties().putAll(options.getProperties());
		}
		return copy;
	}

	/**
	 * Adds generic message writer that will be used when using the generic send methods and data need to be
	 * converted before sending.
	 *
	 * @param writer the writer to set, if null will remove any previously set writer
	 * @param <D> the data type
	 * @param <M> the message type
	 * @return current instance for chaining
	 * @see SenderService#send(String, Object, MessageWriter, SendOptions)
	 * @see MessageSender#send(Object)
	 * @see MessageSender#send(Object, SendOptions)
	 * @see MessageSender#send(Object, MessageWriter, SendOptions)
	 */
	public <D, M extends Message> SendOptions withWriter(MessageWriter<D, M> writer) {
		this.customWriter = writer;
		return this;
	}

	/**
	 * Adds a specialized message writer that produces {@link javax.jms.MapMessage} for the input data. The writer
	 * will be used when any of the sendMap methods are used
	 *
	 * @param writer the writer to set, if null will reset the writer to it's default instance
	 * {@link MapMessageWriter#instance()}
	 * @param <D> the source data type
	 * @return current instance for chaining
	 */
	public <D> SendOptions withMapWriter(MapMessageWriter<D> writer) {
		customWriter = writer;
		return this;
	}

	/**
	 * Adds a specialized message writer that produces {@link javax.jms.ObjectMessage} for the input data. The writer
	 * will be used when any of the sendObject methods are used
	 *
	 * @param writer the writer to set, if null will reset the writer to it's default instance
	 * {@link ObjectMessageWriter#instance()}
	 * @param <D> the source data type
	 * @return current instance for chaining
	 * @see SenderService#sendObject(String, Object, ObjectMessageWriter)
	 * @see SenderService#sendObject(String, Object, ObjectMessageWriter, SendOptions)
	 * @see MessageSender#sendObject(Object, ObjectMessageWriter)
	 * @see MessageSender#sendObject(Object, ObjectMessageWriter, SendOptions)
	 */
	public <D extends Serializable> SendOptions withObjectWriter(ObjectMessageWriter<D> writer) {
		customWriter = writer;
		return this;
	}

	/**
	 * Specifies a delay in the message delivery based on the message send time in milliseconds. The message will be delivered after
	 * the given time elapses.
	 *
	 * @param deliveryDelay the delay in milliseconds before the message to be delivered
	 * @return current instance for chaining
	 * @see javax.jms.JMSProducer#setDeliveryDelay(long)
	 */
	public SendOptions delayWith(long deliveryDelay) {
		this.deliveryDelay = deliveryDelay;
		return this;
	}

	/**
	 * Specifies a delay in the message delivery based on the message send time. The message will be delivered after
	 * the given time elapses.
	 *
	 * @param deliveryDelay the delay in milliseconds before the message to be delivered
	 * @param unit the delivery delay unit
	 * @return current instance for chaining
	 * @see javax.jms.JMSProducer#setDeliveryDelay(long)
	 */
	public SendOptions delayWith(long deliveryDelay, TimeUnit unit) {
		return delayWith(unit.toMillis(deliveryDelay));
	}

	/**
	 * The time in milliseconds that the send message should be considered valid. The time starts when the message is
	 * actually send and not calling the send method. After the given time expire the message will be moved to the
	 * configured expiration queue.
	 *
	 * @param timeToLive the time in milliseconds that specifies the message time to live
	 * @return current instance for chaining
	 * @see javax.jms.JMSProducer#setTimeToLive(long)
	 */
	public SendOptions expireAfter(long timeToLive) {
		this.timeToLive = timeToLive;
		return this;
	}

	/**
	 * The time in that the send message should be considered valid. The time starts when the message is
	 * actually send and not calling the send method. After the given time expire the message will be moved to the
	 * configured expiration queue.
	 *
	 * @param timeToLive the time that specifies the message time to live
	 * @param unit the time to live parameter time unit
	 * @return current instance for chaining
	 * @see javax.jms.JMSProducer#setTimeToLive(long)
	 */
	public SendOptions expireAfter(long timeToLive, TimeUnit unit) {
		return expireAfter(unit.toMillis(timeToLive));
	}

	/**
	 * Set the message priority relative to the other send messages.
	 *
	 * @param priority the message priority to set. Allowed values 0 to 9
	 * @return current instance for chaining
	 * @see javax.jms.JMSProducer#setPriority(int)
	 * @see javax.jms.Message#DEFAULT_PRIORITY
	 */
	public SendOptions withPriority(int priority) {
		if (priority < 0 || priority > 9) {
			throw new IllegalArgumentException("Priority should be value between 0 and 9. Passed: " + priority);
		}
		this.priority = priority;
		return this;
	}

	/**
	 * Specifies that the sent messages should be persisted before delivered to the receiver.
	 *
	 * @return current instance for chaining
	 * @see javax.jms.DeliveryMode#PERSISTENT
	 * @see javax.jms.JMSProducer#setDeliveryMode(int)
	 */
	public SendOptions persistent() {
		this.deliveryMode = DeliveryMode.PERSISTENT;
		return this;
	}

	/**
	 * Specifies that the sent messages are not required to be persisted before delivered to the receiver.
	 *
	 * @return current instance for chaining
	 * @see javax.jms.DeliveryMode#NON_PERSISTENT
	 * @see javax.jms.JMSProducer#setDeliveryMode(int)
	 */
	public SendOptions nonPersistent() {
		this.deliveryMode = DeliveryMode.NON_PERSISTENT;
		return this;
	}

	/**
	 * Set the desired JMS type defined by the {@link Message#setJMSType(String)}. <br>There is no default value set.
	 *
	 * @param jmsType the message type to set
	 * @return current instance for chaining
	 * @see Message#setJMSType(String)
	 */
	public SendOptions asJmsType(String jmsType) {
		this.jmsType = jmsType;
		return this;
	}

	/**
	 * Sets the JNDI destination of the replyTo queue.
	 *
	 * @param replyTo the reply queue JNDI
	 * @return current instance for chaining
	 * @see javax.jms.JMSProducer#setJMSReplyTo(Destination)
	 */
	public SendOptions replyTo(String replyTo) {
		this.replyTo = replyTo;
		return this;
	}

	/**
	 * Set the asynchronous {@link CompletionListener} to be called. Setting this method will also enable
	 * asynchronous message delivery for the send messages. <br> Note that by default messages are send synchronously
	 * at the end of the current transaction.
	 *
	 * @param completionListener the listener to be called with the result of message delivery status
	 * @return current instance for chaining
	 * @see javax.jms.JMSProducer#setAsync(CompletionListener)
	 */
	public SendOptions async(CompletionListener completionListener) {
		this.completionListener = completionListener;
		return this;
	}

	/**
	 * Add properties that should be added to each send message. These properties are added to the message header
	 * properties and does not affect the message body. Message headers cannot be modified using this method
	 * .<br>Note that the map values should be primitive types or String. Custom objects cannot be set and will
	 * result in {@link com.sirmaenterprise.sep.jms.exception.JmsRuntimeException} during message sending.
	 *
	 * @param customProperties the properties to add to each message.
	 * @return current instance for chaining
	 * @see Message#setObjectProperty(String, Object)
	 */
	public SendOptions withProperties(Map<String, Serializable> customProperties) {
		if (isNotEmpty(customProperties)) {
			getOrCreateProperties().putAll(customProperties);
		}
		return this;
	}

	/**
	 * Add property that should be added to each send message. These property is added to the message header
	 * properties and does not affect the message body. Message headers cannot be modified using this method
	 * .<br>Note that the value should be primitive type or String. Custom objects cannot be set and will
	 * result in {@link com.sirmaenterprise.sep.jms.exception.JmsRuntimeException} during message sending.
	 *
	 * @param propertyKey the property key to set
	 * @param value the value to set. Passing null for already set value will result in removing the previous set value
	 * @return current instance for chaining
	 * @see Message#setObjectProperty(String, Object)
	 */
	public SendOptions withProperty(String propertyKey, Serializable value) {
		if (propertyKey != null && value != null) {
			getOrCreateProperties().put(propertyKey, value);
		}
		return this;
	}

	private Map<String, Serializable> getOrCreateProperties() {
		if (properties == null) {
			properties = new HashMap<>();
		}
		return properties;
	}

	public Map<String, Serializable> getProperties() {
		if (properties == null) {
			return Collections.emptyMap();
		}
		return properties;
	}

	/**
	 * Enable JMS Correlation ID setting. If no custom id is set using the {@link #withCorrelationId(String)} will
	 * result in sending a default id as {@link com.sirma.itt.seip.security.context.SecurityContext#getRequestId()}.
	 * <br>By default the correlation id sending is enabled.
	 *
	 * @return current instance for chaining
	 * @see javax.jms.JMSProducer#setJMSCorrelationID(String)
	 */
	public SendOptions withCorrelationId() {
		sendCorrelationId = true;
		return this;
	}

	/**
	 * Set the correlation id to be used when sending messages. All messages send after this property is set will
	 * have the same correlation id. When used this method will override the default correlation id set. <br>Default
	 * correlation id is {@link com.sirma.itt.seip.security.context.SecurityContext#getRequestId()}.<br>Note that
	 * calling this method will automatically enable correlation id sending even if it was disabled with
	 * {@link #noCorrelationId()}.
	 *
	 * @param correlationId the correlation id to use. Passing null value will fall back to the default value
	 * @return current instance for chaining
	 * @see javax.jms.JMSProducer#setJMSCorrelationID(String)
	 */
	public SendOptions withCorrelationId(String correlationId) {
		this.correlationId = correlationId;
		return withCorrelationId();
	}

	/**
	 * Disable JMS Correlation ID setting and not even a default correlation id will be send.
	 *
	 * @return current instance for chaining
	 * @see javax.jms.JMSProducer#setJMSCorrelationID(String)
	 */
	public SendOptions noCorrelationId() {
		correlationId = null;
		sendCorrelationId = false;
		return this;
	}

	/**
	 * Specifies the security mode at the receiver side to match the user that sends the messages
	 *
	 * @return current instance for chaining
	 * @see SecurityMode#DEFAULT
	 */
	public SendOptions asCurrentUser() {
		securityMode = SecurityMode.DEFAULT;
		return this;
	}

	/**
	 * Specifies the security mode at the receiver side to be the system scope (no concrete tenant)
	 *
	 * @return current instance for chaining
	 * @see SecurityMode#SYSTEM
	 */
	public SendOptions asSystem() {
		securityMode = SecurityMode.SYSTEM;
		return this;
	}

	/**
	 * Specifies the security mode at the receiver side to be the system user with admin permissions in the same
	 * tenant that is the user that sends the messages.
	 *
	 * @return current instance for chaining
	 * @see SecurityMode#TENANT_ADMIN
	 */
	public SendOptions asTenantAdmin() {
		securityMode = SecurityMode.TENANT_ADMIN;
		return this;
	}

	@SuppressWarnings("unchecked")
	public <D, M extends Message> MessageWriter<D, M> getWriter() {
		return customWriter;
	}

	public long getDeliveryDelay() {
		return deliveryDelay;
	}

	public long getTimeToLive() {
		return timeToLive;
	}

	public CompletionListener getCompletionListener() {
		return completionListener;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public boolean isCorrelationIdSendAllowed() {
		return sendCorrelationId;
	}

	public int getDeliveryMode() {
		return deliveryMode;
	}

	public int getPriority() {
		return priority;
	}

	public String getReplyTo() {
		return replyTo;
	}

	public String getJmsType() {
		return jmsType;
	}

	public SecurityMode getSecurityMode() {
		return securityMode;
	}

	@Override
	public String toString() {
		return new StringBuilder(360)
				.append("SendOptions{")
				.append("customWriter=").append(customWriter)
				.append(", deliveryDelay=").append(deliveryDelay)
				.append(", timeToLive=").append(timeToLive)
				.append(", deliveryMode=").append(deliveryMode)
				.append(", priority=").append(priority)
				.append(", completionListener=").append(completionListener)
				.append(", properties=").append(properties)
				.append(", correlationId='").append(correlationId).append('\'')
				.append(", sendCorrelationId=").append(sendCorrelationId)
				.append(", replyTo='").append(replyTo).append('\'')
				.append(", jmsType='").append(jmsType).append('\'')
				.append(", securityMode=").append(securityMode)
				.append('}')
				.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof SendOptions)) {
			return false;
		}
		SendOptions that = (SendOptions) o;
		return deliveryDelay == that.deliveryDelay &&
				timeToLive == that.timeToLive &&
				deliveryMode == that.deliveryMode &&
				priority == that.priority &&
				sendCorrelationId == that.sendCorrelationId &&
				Objects.equals(customWriter, that.customWriter) &&
				Objects.equals(completionListener, that.completionListener) &&
				Objects.equals(properties, that.properties) &&
				Objects.equals(correlationId, that.correlationId) &&
				Objects.equals(replyTo, that.replyTo) &&
				Objects.equals(jmsType, that.jmsType) &&
				securityMode == that.securityMode;
	}

	@Override
	public int hashCode() {
		return Objects.hash(customWriter, deliveryDelay, timeToLive, deliveryMode, priority, completionListener,
				properties, correlationId, sendCorrelationId, replyTo, jmsType, securityMode);
	}
}
