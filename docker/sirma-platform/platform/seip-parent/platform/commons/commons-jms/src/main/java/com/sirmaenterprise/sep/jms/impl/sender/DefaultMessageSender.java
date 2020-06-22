package com.sirmaenterprise.sep.jms.impl.sender;

import static com.sirma.itt.seip.collections.CollectionUtils.isNotEmpty;
import static com.sirma.itt.seip.util.EqualsHelper.getOrDefault;
import static com.sirmaenterprise.sep.jms.api.CommunicationConstants.AUTHENTICATED_USER_KEY;
import static com.sirmaenterprise.sep.jms.api.CommunicationConstants.EFFECTIVE_USER_KEY;
import static com.sirmaenterprise.sep.jms.api.CommunicationConstants.REQUEST_ID_KEY;
import static com.sirmaenterprise.sep.jms.api.CommunicationConstants.TENANT_ID_KEY;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.CachingSupplier;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirmaenterprise.sep.jms.api.JmsDestinationResolver;
import com.sirmaenterprise.sep.jms.api.JmsMessageInitializer;
import com.sirmaenterprise.sep.jms.api.MessageSender;
import com.sirmaenterprise.sep.jms.api.SendOptions;
import com.sirmaenterprise.sep.jms.convert.BytesMessageWriter;
import com.sirmaenterprise.sep.jms.convert.MapMessageWriter;
import com.sirmaenterprise.sep.jms.convert.MessageWriter;
import com.sirmaenterprise.sep.jms.convert.MessageWriters;
import com.sirmaenterprise.sep.jms.convert.ObjectMessageWriter;
import com.sirmaenterprise.sep.jms.exception.JmsRuntimeException;
import com.sirmaenterprise.sep.jms.exception.MissingMessageWriterException;
import com.sirmaenterprise.sep.jms.security.SecurityMode;

/**
 * Default implementation of {@link MessageSender}. The produced class should be closed in order to close the
 * underlying {@link JMSContext} and free any hold resources.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 20/05/2017
 */
class DefaultMessageSender implements MessageSender, Serializable {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final Supplier<Destination> destinationSupplier;
	private final transient Supplier<JMSContext> context;
	private final transient Supplier<JMSProducer> producer;
	private final transient SendOptions sendOptions;
	private final SecurityContext securityContext;
	private final transient SecurityContextManager securityContextManager;
	private final JmsDestinationResolver destinationResolver;
	private final transient MessageWriters messageWriters;

	private volatile boolean isClosed = false;

	/**
	 * Instantiate and configure sender.
	 *
	 * @param destinationJndi the sender destination JNDI name
	 * @param context a {@link JMSContext} supplier. The context will be resolved on first message send
	 * @param sendOptions the default send options to use. These options could be overridden when using send method
	 * that accept {@link SendOptions}
	 * @param securityContextManager The security context instance to use when sending messages, used for providing security
	 * information in the message headers and correlation id if enabled
	 * @param destinationResolver a destination resolver for the sender destination or the replyTo destination if
	 * applicable
	 * @param messageWriters message writers registry instance
	 */
	DefaultMessageSender(String destinationJndi, Supplier<JMSContext> context, SendOptions sendOptions,
			SecurityContextManager securityContextManager, JmsDestinationResolver destinationResolver,
			MessageWriters messageWriters) {
		this(new CachingSupplier<>(() -> destinationResolver.resolve(destinationJndi)), context, sendOptions,
				securityContextManager, destinationResolver, messageWriters);
	}

	/**
	 * Instantiate and configure sender.
	 *
	 * @param destinationProvider the sender destination JNDI name
	 * @param context a {@link JMSContext} supplier. The context will be resolved on first message send
	 * @param sendOptions the default send options to use. These options could be overridden when using send method
	 * that accept {@link SendOptions}
	 * @param securityContextManager The security context instance to use when sending messages, used for providing security
	 * information in the message headers and correlation id if enabled
	 * @param destinationResolver a destination resolver for the sender destination or the replyTo destination if
	 * applicable
	 * @param messageWriters message writers registry instance
	 */
	DefaultMessageSender(Supplier<Destination> destinationProvider, Supplier<JMSContext> context,
			SendOptions sendOptions, SecurityContextManager securityContextManager,
			JmsDestinationResolver destinationResolver, MessageWriters messageWriters) {
		this.destinationSupplier = destinationProvider;
		this.context = CachingSupplier.of(context);
		this.producer = () -> getContext().createProducer();
		this.sendOptions = getOrDefault(sendOptions, SendOptions.create());
		this.securityContext = securityContextManager.getCurrentContext();
		this.securityContextManager = securityContextManager;
		this.destinationResolver = destinationResolver;
		this.messageWriters = messageWriters;
	}

	@Override
	public void send(JmsMessageInitializer<Message> initializer, SendOptions options) {
		try {
			Message message = getContext().createMessage();
			initializer.initialize(message);
			doSend(message, options);
		} catch (JMSException e) {
			throw new JmsRuntimeException(e);
		}
	}

	@Override
	public <D> void send(D data, MessageWriter<D, ? extends Message> messageWriter, SendOptions options) {
		Objects.requireNonNull(data, "Cannot send null object to queue");

		MessageWriter<? super D, ? extends Message> writer = resolveWriter(data, messageWriter, options);
		try {
			Message message = writer.write(data, getContext());
			doSend(message, options);
		} catch (JMSException e) {
			throw new JmsRuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private <D, M extends Message> MessageWriter<? super D, M> resolveWriter(D data, MessageWriter<D, M>
			messageWriter, SendOptions options) {
		MessageWriter<? super D, M> writer = getOrDefault(messageWriter, options.getWriter());
		if (writer == null) {
			Class<D> type = (Class<D>) data.getClass();
			Optional<MessageWriter<D, M>> registeredWriter = messageWriters.getWriterFor(type);
			if (data instanceof Map) {
				// if we have a map we can use the default writer
				writer = registeredWriter.orElseGet(() -> {
					LOGGER.trace("Will send messages using the default map writer for {}", type);
					return MapMessageWriter.instance();
				});
			} else if (data instanceof Serializable) {
				// if we have a serializable we can use the default writer
				writer = registeredWriter.orElseGet(() -> {
					LOGGER.trace("Will send messages using the default object writer for {}", type);
					return ObjectMessageWriter.instance();
				});
			} else if (data instanceof InputStream) {
				writer = registeredWriter.orElseGet(() -> {
					LOGGER.trace("Will send messages using the default stream writer for {}", type);
					return BytesMessageWriter.instance();
				});
			} else {
				writer = registeredWriter.orElseThrow(() -> new MissingMessageWriterException(
						"No writer is defined to convert " + type));
			}
		}

		return writer;
	}

	@Override
	public <D> void sendObject(D data, ObjectMessageWriter<D> writer, SendOptions options) {
		send(data, writer, options);
	}

	@Override
	public void sendObject(JmsMessageInitializer<ObjectMessage> initializer, SendOptions options) {
		try {
			ObjectMessage message = getContext().createObjectMessage();
			initializer.initialize(message);
			doSend(message, options);
		} catch (JMSException e) {
			throw new JmsRuntimeException(e);
		}
	}

	@Override
	public void sendText(String data, SendOptions options) {
		TextMessage message = getContext().createTextMessage(data);
		doSend(message, options);
	}

	@Override
	public void sendText(JmsMessageInitializer<TextMessage> initializer, SendOptions options) {
		try {
			TextMessage message = getContext().createTextMessage();
			initializer.initialize(message);
			doSend(message, options);
		} catch (JMSException e) {
			throw new JmsRuntimeException(e);
		}
	}

	@Override
	public void sendMap(JmsMessageInitializer<MapMessage> initializer, SendOptions options) {
		try {
			MapMessage message = getContext().createMapMessage();
			initializer.initialize(message);
			doSend(message, options);
		} catch (JMSException e) {
			throw new JmsRuntimeException(e);
		}
	}

	@Override
	public <D> void sendMap(D data, MapMessageWriter<D> messageWriter, SendOptions options) {
		send(data, messageWriter, options);
	}

	@Override
	public SendOptions getDefaultSendOptions() {
		// return a copy of the local send options to prevent tempering
		return SendOptions.from(sendOptions);
	}

	@Override
	public <T> void registerWriter(Class<T> dataType, Class<? extends MessageWriter> writerClass) {
		messageWriters.register(dataType, writerClass);
	}

	@Override
	public <T> void registerWriter(Class<T> dataType, MessageWriter<T, ? extends Message> writer) {
		messageWriters.register(dataType, writer);
	}

	@Override
	public <T> void registerMapWriter(Class<T> dataType, MapMessageWriter<T> writer) {
		messageWriters.register(dataType, writer);
	}

	@Override
	public <T extends Serializable> void registerObjectWriter(Class<T> dataType, ObjectMessageWriter<T> writer) {
		messageWriters.register(dataType, writer);
	}

	@Override
	public boolean isActive() {
		return !isClosed;
	}

	@Override
	public void close() {
		isClosed = true;
	}

	private void doSend(Message message, SendOptions options) {
		try {
			createProducer(options).send(resolveDestination(), enrichMessage(message, options));
		} catch (JMSException e) {
			throw new JmsRuntimeException(e);
		}
	}

	private Message enrichMessage(Message message, SendOptions options) throws JMSException {
		setSecurityContext(message, options);

		Map<String, Serializable> properties = options.getProperties();
		if (isNotEmpty(properties)) {
			for (Map.Entry<String, Serializable> entry : properties.entrySet()) {
				message.setObjectProperty(entry.getKey(), entry.getValue());
			}
		}
		return message;
	}

	private void setSecurityContext(Message message, SendOptions options) throws JMSException {
		message.setObjectProperty(REQUEST_ID_KEY, securityContext.getRequestId());
		if (options.getSecurityMode() == SecurityMode.TENANT_ADMIN
				|| options.getSecurityMode() == SecurityMode.DEFAULT) {
			message.setObjectProperty(TENANT_ID_KEY, securityContext.getCurrentTenantId());
		}
		// add the current logged in user only if not the system itself.
		// the system user fails to login so we will send only tenant id which can be used to initialize the security
		// context as system again
		if (options.getSecurityMode() == SecurityMode.DEFAULT && !securityContextManager.isCurrentUserSystem()) {
			message.setObjectProperty(AUTHENTICATED_USER_KEY, securityContext.getAuthenticated().getSystemId());
			message.setObjectProperty(EFFECTIVE_USER_KEY, securityContext.getEffectiveAuthentication().getSystemId());
		}
	}

	private JMSProducer createProducer(SendOptions options) {
		String correlationId = null;
		if (options.isCorrelationIdSendAllowed()) {
			correlationId = getOrDefault(options.getCorrelationId(), securityContext.getRequestId());
		}
		Destination replyTo = null;
		if (options.getReplyTo() != null) {
			replyTo = destinationResolver.resolve(options.getReplyTo());
		}

		return getProducer().setAsync(options.getCompletionListener())
				.setDeliveryDelay(options.getDeliveryDelay())
				.setDeliveryMode(options.getDeliveryMode())
				.setJMSType(options.getJmsType())
				.setPriority(options.getPriority())
				.setTimeToLive(options.getTimeToLive())
				.setJMSCorrelationID(correlationId)
				.setJMSReplyTo(replyTo);
	}

	private JMSProducer getProducer() {
		return producer.get();
	}

	private JMSContext getContext() {
		return context.get();
	}

	private Destination resolveDestination() {
		return destinationSupplier.get();
	}

	@Override
	public String getDestination() {
		Destination destination = resolveDestination();
		try {
			if (destination instanceof Queue) {
				return ((Queue) destination).getQueueName();
			} else if (destination instanceof Topic) {
				return ((Topic) destination).getTopicName();
			}
		} catch (JMSException e) {
			LOGGER.warn("Could not read destination name: {}", destination, e);
		}
		return destination.toString();
	}

	@Override
	public String toString() {
		return new StringBuilder(64)
				.append("DefaultMessageSender{")
				.append("destination='")
				.append(resolveDestination())
				.append('\'')
				.append(", active=")
				.append(isActive())
				.append('}')
				.toString();
	}
}
