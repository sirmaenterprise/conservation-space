package com.sirmaenterprise.sep.jms.impl.sender;

import static com.sirma.itt.seip.collections.CollectionUtils.isNotEmpty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import javax.transaction.TransactionScoped;

import com.sirma.itt.seip.CachingSupplier;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirmaenterprise.sep.jms.api.JmsContextProvider;
import com.sirmaenterprise.sep.jms.api.JmsDestinationResolver;
import com.sirmaenterprise.sep.jms.api.JmsMessageInitializer;
import com.sirmaenterprise.sep.jms.api.MessageSender;
import com.sirmaenterprise.sep.jms.api.SendOptions;
import com.sirmaenterprise.sep.jms.api.SenderService;
import com.sirmaenterprise.sep.jms.convert.MapMessageWriter;
import com.sirmaenterprise.sep.jms.convert.MessageWriter;
import com.sirmaenterprise.sep.jms.convert.MessageWriters;
import com.sirmaenterprise.sep.jms.convert.ObjectMessageWriter;

/**
 * Default implementation of {@link SenderService}. The implementation is transaction scoped and is closed/destroyed
 * at the end of the transaction where it's used. This closes the underlying {@link JMSContext} used for sending
 * messages so no additional work is needed when injected in transaction scope. Note that if no transaction is active
 * when method to this service is called will result in {@link javax.enterprise.context.ContextNotActiveException} to
 * be thrown.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 20/05/2017
 */
@TransactionScoped
public class TransactionalSenderService implements SenderService, Serializable {

	private transient SecurityContextManager securityContextManager;
	private transient JmsDestinationResolver destinationResolver;
	private transient Supplier<JMSContext> contextSupplier;
	private transient MessageWriters messageWriters;
	/**
	 * holds any senders used via the local send methods. The caching is done by destination name
	 */
	private transient Map<String, MessageSender> cachedSenders = new ConcurrentHashMap<>(32, 0.8f, 8);
	/**
	 * holds any created senders via the methods {@link #createSender}
	 */
	private transient List<MessageSender> producesSenders = new ArrayList<>(5);

	/**
	 * Default constructor. Note this constructor does not initialize service. Use the other constructor
	 */
	public TransactionalSenderService() {
		// default constructor to allow CDI to proxy the class because of the scope
	}

	/**
	 * Instantiate and and initialize the service
	 *
	 * @param contextProvider the context provider to use when building {@link JMSContext} if needed
	 * @param destinationResolver the destination resolver to use to resolve the send and replyTo destinations
	 * @param securityContextManager the security context to use to send the security info over
	 * @param messageWriters the writers registry to use for data conversion
	 */
	@Inject
	public TransactionalSenderService(JmsContextProvider contextProvider, JmsDestinationResolver destinationResolver,
			SecurityContextManager securityContextManager, MessageWriters messageWriters) {
		this.destinationResolver = destinationResolver;
		this.securityContextManager = securityContextManager;
		contextSupplier = CachingSupplier.of(contextProvider::provide);
		this.messageWriters = messageWriters;
	}

	@Override
	public void send(String destination, JmsMessageInitializer<Message> initializer) {
		getOrCreateSender(destination).send(initializer);
	}

	@Override
	public void send(String destination, JmsMessageInitializer<Message> initializer, SendOptions options) {
		getOrCreateSender(destination).send(initializer, options);
	}

	@Override
	public <D> void send(Destination destination, D data) {
		getOrCreateSender(destination).send(data);
	}

	@Override
	public <D> void send(Destination destination, D data, SendOptions options) {
		getOrCreateSender(destination).send(data, options);
	}

	@Override
	public <D> void send(String destination, D data) {
		getOrCreateSender(destination).send(data);
	}

	@Override
	public <D> void send(String destination, D data, SendOptions options) {
		getOrCreateSender(destination).send(data, options);
	}

	@Override
	public <D> void send(String destination, D data, MessageWriter<D, Message> messageWriter, SendOptions options) {
		getOrCreateSender(destination).send(data, messageWriter, options);
	}

	@Override
	public <D> void sendObject(String destination, D data, ObjectMessageWriter<D> writer) {
		getOrCreateSender(destination).sendObject(data, writer);
	}

	@Override
	public <D> void sendObject(String destination, D data, ObjectMessageWriter<D> writer, SendOptions options) {
		getOrCreateSender(destination).sendObject(data, writer, options);
	}

	@Override
	public void sendObject(String destination, JmsMessageInitializer<ObjectMessage> initializer) {
		getOrCreateSender(destination).sendObject(initializer);
	}

	@Override
	public void sendObject(String destination, JmsMessageInitializer<ObjectMessage> initializer, SendOptions options) {
		getOrCreateSender(destination).sendObject(initializer, options);
	}

	@Override
	public void sendText(String destination, String data) {
		getOrCreateSender(destination).sendText(data);
	}

	@Override
	public void sendText(String destination, JmsMessageInitializer<TextMessage> initializer) {
		getOrCreateSender(destination).sendText(initializer);
	}

	@Override
	public void sendText(String destination, String data, SendOptions options) {
		getOrCreateSender(destination).sendText(data, options);
	}

	@Override
	public void sendText(String destination, JmsMessageInitializer<TextMessage> initializer, SendOptions options) {
		getOrCreateSender(destination).sendText(initializer, options);
	}

	@Override
	public <D> void sendMap(String destination, D data, MapMessageWriter<D> writer) {
		getOrCreateSender(destination).sendMap(data, writer);
	}

	@Override
	public <D> void sendMap(String destination, D data, MapMessageWriter<D> writer, SendOptions options) {
		getOrCreateSender(destination).sendMap(data, writer, options);
	}

	@Override
	public void sendMap(String destination, JmsMessageInitializer<MapMessage> initializer) {
		getOrCreateSender(destination).sendMap(initializer);
	}

	@Override
	public void sendMap(String destination, JmsMessageInitializer<MapMessage> initializer, SendOptions options) {
		getOrCreateSender(destination).sendMap(initializer, options);
	}

	private MessageSender getOrCreateSender(String destination) {
		return cachedSenders.computeIfAbsent(destination,
				destinationJndi -> new DefaultMessageSender(destinationJndi, contextSupplier, null,
						securityContextManager, destinationResolver, messageWriters));
	}

	private MessageSender getOrCreateSender(Destination destination) {
		return cachedSenders.computeIfAbsent(destination.toString(),
				destinationName -> new DefaultMessageSender(() -> destination, contextSupplier, null,
						securityContextManager, destinationResolver, messageWriters));
	}

	@Override
	public MessageSender createSender(String destination, SendOptions options) {
		// initialize the sender with options copy so prevent state change during the use of the sender
		// as the returned object is long lived
		DefaultMessageSender sender = new DefaultMessageSender(destination, contextSupplier, SendOptions.from(options),
				securityContextManager, destinationResolver, messageWriters);
		producesSenders.add(sender);
		return sender;
	}

	@Override
	public JMSContext getCurrentContext() {
		return contextSupplier.get();
	}

	@Override
	public <T> void registerWriter(Class<T> dataType, MessageWriter<T, ? extends Message> writer) {
		messageWriters.register(dataType, writer);
	}

	@Override
	public <T> void registerWriter(Class<T> dataType, Class<? extends MessageWriter> writerClass) {
		messageWriters.register(dataType, writerClass);
	}

	@Override
	public <T extends Serializable> void registerObjectWriter(Class<T> dataType, ObjectMessageWriter<T> writer) {
		messageWriters.register(dataType, writer);
	}

	@Override
	public <T> void registerMapWriter(Class<T> dataType, MapMessageWriter<T> writer) {
		messageWriters.register(dataType, writer);
	}

	/**
	 * Close the active {@link JMSContext} and close all of the created {@link MessageSender}s
	 */
	@PreDestroy
	void onTransactionCommit() {
		contextSupplier.get().close();
		// any message senders produces using this supplier will be closed
		producesSenders.forEach(MessageSender::close);
		if (isNotEmpty(producesSenders)) {
			producesSenders.clear();
			// this will prevent further sender creation and message sending on committed transaction
			producesSenders = Collections.unmodifiableList(producesSenders);
		}
		cachedSenders.values().forEach(MessageSender::close);
		if (isNotEmpty(cachedSenders)) {
			cachedSenders.clear();
			// this will prevent further sender creation and message sending on committed transaction
			cachedSenders = Collections.unmodifiableMap(cachedSenders);
		}
	}
}
