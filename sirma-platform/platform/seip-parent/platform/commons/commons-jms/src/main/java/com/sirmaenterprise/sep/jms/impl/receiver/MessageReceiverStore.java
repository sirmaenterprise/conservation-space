package com.sirmaenterprise.sep.jms.impl.receiver;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirmaenterprise.sep.jms.api.JmsContextProvider;
import com.sirmaenterprise.sep.jms.api.JmsDestinationResolver;
import com.sirmaenterprise.sep.jms.api.MessageConsumer;
import com.sirmaenterprise.sep.jms.api.MessageConsumerListener;
import com.sirmaenterprise.sep.jms.api.MessageReceiver;
import com.sirmaenterprise.sep.jms.api.MessageReceiverResponse;
import com.sirmaenterprise.sep.jms.api.ReceiverDefinition;
import com.sirmaenterprise.sep.jms.exception.DestinationNotFoundException;
import com.sirmaenterprise.sep.jms.exception.MessageReceiverException;

/**
 * Store for all build {@link MessageReceiver} instances
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 13/05/2017
 */
@Singleton
class MessageReceiverStore {
	private Map<ReceiverDefinition, MessageReceiver> entries = new ConcurrentHashMap<>();

	@Inject
	private JmsContextProvider contextProvider;

	@Inject
	private JmsDestinationResolver destinationResolver;

	@Inject
	private TransactionSupport transactionSupport;

	@Inject
	private Instance<MessageConsumerListener> messageListener;

	/**
	 * Create message receiver for the given destination name and optional message selector. Upon calling this method
	 * the destination should be resolvable otherwise {@link DestinationNotFoundException} will be thrown. Calling
	 * this method multiple times with the same arguments will result of returning the same receiver instance.
	 *
	 * @param receiverDefinition the receiver definition describing the message origin like destination name and selector
	 * @return a message receiver instance.
	 */
	public MessageReceiver getReceiver(ReceiverDefinition receiverDefinition) {
		return entries.computeIfAbsent(receiverDefinition, createDestinationReceiver());
	}

	private Function<ReceiverDefinition, MessageReceiver> createDestinationReceiver() {
		return definition -> {
			Destination destination = destinationResolver.resolve(definition.getDestinationJndi());
			return new DestinationReceiverSession(destination, definition, contextProvider, transactionSupport,
					messageListener::get);
		};
	}

	/**
	 * Base receiver implementation that reads queue and topic destinations
	 *
	 * @author BBonev
	 */
	private static class DestinationReceiverSession implements MessageReceiver {
		private static final Logger RECEIVER_LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
		
		private final ReceiverDefinition receiverDefinition;
		private final JmsContextProvider contextProvider;
		private final TransactionSupport transactionSupport;
		private final Destination destination;
		private final Supplier<MessageConsumerListener> listenerSupplier;

		DestinationReceiverSession(Destination destination, ReceiverDefinition receiverDefinition, JmsContextProvider contextProvider,
				TransactionSupport transactionSupport, Supplier<MessageConsumerListener> listenerSupplier) {
			this.destination = destination;
			this.receiverDefinition = receiverDefinition;
			this.contextProvider = contextProvider;
			this.transactionSupport = transactionSupport;
			this.listenerSupplier = listenerSupplier;
		}

		@Override
		public MessageReceiverResponse readMessage(MessageConsumer messageConsumer) {
			return fetchMessage(messageConsumer, JMSConsumer::receiveNoWait);
		}

		@Override
		public MessageReceiverResponse waitMessage(MessageConsumer messageConsumer) {
			MessageConsumerListener listener = createListener();
			try {
				MessageReceiverResponse response = readMessageInternal(messageConsumer, JMSConsumer::receive, listener);
				listener.onSuccess();
				return response;
			} catch (MessageReceiverException e) {
				listener.onError(e);
				return e.getResponse();
			}
		}

		@Override
		public MessageReceiverResponse waitMessage(MessageConsumer messageConsumer, long timeoutInMilliseconds) {
			// when we wait for a message in a transaction, we cannot wait too long otherwise the transaction
			// will timeout and when the message arrives it will throw an exception for aborted transaction.
			// this should be balanced not to use too much resources while waiting for messages
			return fetchMessage(messageConsumer, consumer -> consumer.receive(timeoutInMilliseconds));
		}

		private MessageReceiverResponse fetchMessage(MessageConsumer messageConsumer,
				Function<JMSConsumer, Message> reader) {
			MessageConsumerListener listener = createListener();
			try {
				int transactionTimeout = receiverDefinition.getTransactionTimeout();
				// allow long transactions for message processing
				MessageReceiverResponse response = transactionSupport
						.invokeInNewTx(() -> readMessageInternal(messageConsumer, reader, listener), transactionTimeout,
								TimeUnit.SECONDS);
				listener.onSuccess();
				return response;
			} catch (MessageReceiverException e) {
				listener.onError(e);
				return e.getResponse();
			} catch (Exception e) {
				listener.onError(e);
				if (e.getCause() instanceof MessageReceiverException) {
					return ((MessageReceiverException) e.getCause()).getResponse();
				}
			}
			return MessageReceiverResponse.NO_OP;
		}

		private MessageConsumerListener createListener() {
			return new CachingListener(listenerSupplier);
		}

		private MessageReceiverResponse readMessageInternal(MessageConsumer messageConsumer,
				Function<JMSConsumer, Message> reader, MessageConsumerListener listener) {
			try (JMSContext jmsContext = contextProvider.provide();
					JMSConsumer consumer = receiverDefinition.createConsumer(destination, jmsContext)) {
				Message message = reader.apply(consumer);
				return handleMessage(message, messageConsumer, jmsContext, listener);
			} catch (MessageReceiverException e) {
				throw e;
			} catch (Exception e) {
				RECEIVER_LOGGER.trace("Failed to receive jms message due to", e);
				throw new MessageReceiverException(MessageReceiverResponse.FAILED_RECEIVING);
			}
		}

		private MessageReceiverResponse handleMessage(Message message, MessageConsumer messageConsumer,
				JMSContext jmsContext, MessageConsumerListener listener) {
			if (message == null) {
				return MessageReceiverResponse.NO_OP;
			}
			try {
				RECEIVER_LOGGER.trace("Received new message at {}", destination);
				// TODO: pass contextProvider proxy to forbid calling lifecycle methods
				listener.beforeMessage(message);
				messageConsumer.accept(message, jmsContext);
				return MessageReceiverResponse.SUCCESS;
			} catch (Exception e) {
				RECEIVER_LOGGER.warn("Couldn't consume message due to:", e);
				throw new MessageReceiverException(MessageReceiverResponse.FAILED_CONSUMING, e);
			}
		}
	}

	private static class CachingListener implements MessageConsumerListener {
		private final Supplier<MessageConsumerListener> listenerSupplier;
		private MessageConsumerListener listener;
		private Message message;

		private CachingListener(Supplier<MessageConsumerListener> listenerSupplier) {
			this.listenerSupplier = listenerSupplier;
		}

		@Override
		public void beforeMessage(Message message) {
			this.message = message;
			if (message != null) {
				getListener().beforeMessage(message);
			}
		}

		@Override
		public void onSuccess() {
			if (message != null) {
				getListener().onSuccess();
			}
		}

		@Override
		public void onError(Exception e) {
			if (message != null) {
				getListener().onError(e);
			}
		}

		private MessageConsumerListener getListener() {
			if (listener == null) {
				listener = listenerSupplier.get();
			}
			return listener;
		}
	}
}
