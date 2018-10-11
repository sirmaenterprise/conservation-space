package com.sirmaenterprise.sep.jms.impl.receiver;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirmaenterprise.sep.jms.api.MessageConsumer;
import com.sirmaenterprise.sep.jms.api.MessageReceiver;
import com.sirmaenterprise.sep.jms.api.ReceiverDefinition;
import com.sirmaenterprise.sep.jms.api.event.StartingMessageReceiverEvent;

/**
 * Manager class that starts and monitors active receivers registered via the
 * {@link #registerJmsListener(ReceiverDefinition)} method. <br>For each registred receiver definition are started up
 * to {@link ReceiverDefinition#getConcurrenceLevel()} threads to listen for messages. There is a maximum allowed
 * listeners per definition that is configured via the configuration api.
 * <p>After all listeners are registered the {@link #start()} method should be called to trigger the listening</p>
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 12/05/2017
 */
@Singleton
public class JmsReceiverManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "system.jms.maxConcurrentReceiverThreads", defaultValue = "5", type =
			Integer.class, system = true, subSystem = "jms", sensitive = true, label = "The maximum allowed concurrent "
			+ "threads per destination.Higher values may hinder performance. Note this is global setting for all "
			+ "destinations.")
	private ConfigurationProperty<Integer> maxConcurrentThreads;

	@Inject
	private MessageReceiverStore messageReceiverStore;
	@Inject
	private Statistics statistics;
	@Inject
	private EventService eventService;

	private ThreadFactory threadFactory;

	private List<JMSReaderEntry> receivers = new ArrayList<>();

	// TODO add monitoring thread
	// TODO add receivers pause and resume

	/**
	 * Register the given receiver definition and start all listening for messages.
	 *
	 * @param definition the receiver definition to register
	 * @return the started listeners count
	 */
	public int registerJmsListener(ReceiverDefinition definition) {
		Objects.requireNonNull(definition, "Receiver definition cannot be null");

		int concurrent = Math.min(maxConcurrentThreads.get().intValue(), Math.max(1, definition.getConcurrenceLevel()));

		LOGGER.debug("Starting {} receiver/s to consume messages from {}[{}]", concurrent, definition
				.getDestinationJndi(), definition.getSelector());
		StartingMessageReceiverEvent event = new StartingMessageReceiverEvent(definition);
		eventService.fire(event);

		for (int i = 0; i < concurrent; i++) {
			receivers.add(createReceiverEntry(definition, event.getMessageConsumer()));
		}
		return concurrent;
	}

	/**
	 * Start message receiving. Calling this method again will do nothing if no new listeners are registered.
	 */
	public void start() {
		for (JMSReaderEntry entry : receivers) {
			entry.start(getThreadFactory());
		}
	}

	/**
	 * Restart all listeners. The method will stop the previous receivers and will start them again.
	 */
	public void restart() {
		LOGGER.info("Initiated receivers restart");
		for (JMSReaderEntry entry : receivers) {
			entry.restart(getThreadFactory());
		}
	}

	private JMSReaderEntry createReceiverEntry(ReceiverDefinition def, MessageConsumer messageConsumer) {
		MessageReceiver session = messageReceiverStore.getReceiver(def);
		BlockingMessageReceiverChannel receiver = new BlockingMessageReceiverChannel(def, session, messageConsumer,
				statistics);
		return new JMSReaderEntry(def, receiver);
	}

	private ThreadFactory getThreadFactory() {
		if (threadFactory == null) {
			// store the number of threads produced per queue
			Map<String, AtomicInteger> threadCounts = new ConcurrentHashMap<>();
			threadFactory = runnable -> {
				String name = "JMSReader-$s-Thread";
				if (runnable instanceof BlockingMessageReceiverChannel) {
					// builds a thread name based on the queue it listens to and serial number
					// for example for queue of the format java:/jms.queue.TestQueue or java:/jms/queue/TestQueue
					// will produce a name: JMSReader-1-TestQueue, JMSReader-2-TestQueue and so on.
					name = ((BlockingMessageReceiverChannel) runnable).getReceiverDefinition().getDestinationJndi();
					name = name.replaceAll(".+[./]", "JMSReader-%s-");
				}
				int count = threadCounts.computeIfAbsent(name, k -> new AtomicInteger()).incrementAndGet();
				Thread thread = new Thread(runnable, String.format(name, count));
				thread.setDaemon(true);
				return thread;
			};
		}
		return threadFactory;
	}

	/**
	 * Gracefully shutdown all receivers. Will be called automatically on application shutdown
	 */
	@PreDestroy
	public void shutdown() {
		LOGGER.info("Shutting down JMS listeners");
		receivers.forEach(JMSReaderEntry::shutdown);
	}

	/**
	 * Get receivers status snapshot. This status is not 100 % accurate as it represents a snapshot of the status at
	 * the moment of calling the method.
	 *
	 * @return receiver status snapshot
	 */
	public ReceiversInfo getInfo() {
		ReceiversInfo status = new ReceiversInfo();
		receivers.forEach(
				entry -> status.add(entry.definition.getDestinationJndi(), entry.definition.getSelector(), entry
						.receiver.getStatus()));
		return status;
	}

	/**
	 * Wrapper object for the receiver definition and it's thread
	 */
	private static class JMSReaderEntry {
		final ReceiverDefinition definition;
		final BlockingMessageReceiverChannel receiver;
		Thread thread;

		JMSReaderEntry(ReceiverDefinition def, BlockingMessageReceiverChannel receiver) {
			this.definition = def;
			this.receiver = receiver;
		}

		/**
		 * Shutdown receiver channel and interrupt thread
		 */
		synchronized void shutdown() {
			if (interruptActiveThread()) {
				// if the thread was not active in the first place do not trigger shutdown
				receiver.shutdown();
			}
		}

		/**
		 * Start message receiving
		 *
		 * @param factory the thread factory to use for thread building.
		 */
		synchronized void start(ThreadFactory factory) {
			if (thread != null) {
				return;
			}
			thread = factory.newThread(receiver);
			thread.start();
		}

		/**
		 * Stops and starts the thread listener
		 *
		 * @param factory the thread factory to use for thread building.
		 */
		synchronized void restart(ThreadFactory factory) {
			shutdown();
			start(factory);
		}

		private boolean interruptActiveThread() {
			if (thread != null) {
				thread.interrupt();
				thread = null;
				return true;
			}
			return false;
		}
	}
}
