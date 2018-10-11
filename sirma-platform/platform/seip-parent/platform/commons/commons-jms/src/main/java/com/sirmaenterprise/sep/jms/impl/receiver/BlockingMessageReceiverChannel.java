package com.sirmaenterprise.sep.jms.impl.receiver;

import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jms.IllegalStateRuntimeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.util.EqualsHelper;
import com.sirmaenterprise.sep.jms.api.MessageConsumer;
import com.sirmaenterprise.sep.jms.api.MessageReceiver;
import com.sirmaenterprise.sep.jms.api.MessageReceiverResponse;
import com.sirmaenterprise.sep.jms.api.ReceiverDefinition;

/**
 * Receiver channel that links message receiving with message dispatching. <p>The class implements the
 * {@link Runnable}  interfaces to so it can be run or scheduled in a thread pool. <br>Upon run the code will call
 * the given {@link MessageReceiver} and on a message arrival will call the given {@link MessageConsumer}</p>
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 12/05/2017
 */
class BlockingMessageReceiverChannel implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	static final long WAIT_TIMEOUT = 2000;
	static final long TIMEOUT_REATTEMPT_WAIT_TIME = 30000;
	
	private final MessageReceiver messageReceiver;
	private final ReceiverDefinition receiverDefinition;
	private final MessageConsumer messageConsumer;
	private final Statistics statistics;
	private final String statisticsName;
	private final CountDownLatch shutdownRequest = new CountDownLatch(1);

	/*
	 * can be used to stop the receiver channel
	 */
	private volatile boolean isActive;
	private volatile boolean shouldStop = false;
	private volatile Status status = Status.NOT_STARTED;

	/**
	 * Initialize new receiver that should read from the given receiver definition
	 *
	 * @param receiverDefinition the receiver definition instance
	 * @param messageReceiver the message receiver that will read the destination queue and produces the messages
	 * @param messageConsumer the message consumer that accepts the read messages
	 * @param statistics statistics instance
	 */
	BlockingMessageReceiverChannel(ReceiverDefinition receiverDefinition, MessageReceiver messageReceiver,
			MessageConsumer messageConsumer, Statistics statistics) {
		this.receiverDefinition = Objects.requireNonNull(receiverDefinition, "Receiver definition is required");
		this.messageReceiver = Objects.requireNonNull(messageReceiver, "Message receiver is required");
		this.messageConsumer = Objects.requireNonNull(messageConsumer, "Message consumer is required");
		this.statistics = EqualsHelper.getOrDefault(statistics, Statistics.NO_OP);
		this.statisticsName = escape(receiverDefinition.getDestinationJndi());
	}

	private static String escape(String name) {
		return name.replaceAll("[:/\\\\.]+", "_").toLowerCase();
	}

	@Override
	public void run() {
		try {
			isActive = true;

			while (isActive && !shouldStop && !Thread.interrupted()) {
				status = Status.WAITING_FOR_MESSAGE;
				MessageReceiverResponse response = messageReceiver.waitMessage(onMessage(), WAIT_TIMEOUT);
				if (MessageReceiverResponse.FAILED_RECEIVING == response) {
					// Message receiving usually fails when the jms destination is unreachable. In
					// that case we want to give the destination some time to recover and then we
					// can try to get the message again.
					status = Status.WAITING_TO_RECCONNECT;
					LOGGER.warn(
							"Timeout while waiting for jms message for destination {}! Will re-attempt waiting in {} seconds.",
							receiverDefinition.getDestinationJndi(),
							Long.valueOf(TimeUnit.MILLISECONDS.toSeconds(TIMEOUT_REATTEMPT_WAIT_TIME)));
					Thread.sleep(TIMEOUT_REATTEMPT_WAIT_TIME);
				}
			}
			status = Status.STOPPED;
		} catch (IllegalStateRuntimeException e) {
			// this could be thrown if the connection factory is closed for example
			LOGGER.debug("Receiver interrupted for destination {}", receiverDefinition.getDestinationJndi(), e);
			status = Status.FAILED;
		} catch (InterruptedException e) {
			LOGGER.warn("JMS message receiver interrupted for destination {}! Most likely server shutting down.",
					receiverDefinition.getDestinationJndi());
			LOGGER.trace("JMS message receiver interrupted for destination {}! Most likely server shutting down.",
					receiverDefinition.getDestinationJndi(), e);
			status = Status.STOPPED;
			Thread.currentThread().interrupt();
		} catch (Exception e) {
			LOGGER.warn("Receiver problem for destination {}", receiverDefinition.getDestinationJndi(), e);
			status = Status.FAILED;
		} finally {
			shutdownRequest.countDown();
			isActive = false;
			LOGGER.warn("Shutdown message processing from {}", receiverDefinition.getDestinationJndi());
		}
	}

	private MessageConsumer onMessage() {
		return (message, context) -> {
			status = Status.PROCESSING;
			statistics.updateMeter(null, statisticsName);
			messageConsumer.accept(message, context);
		};
	}

	boolean isActive() {
		return isActive;
	}

	/**
	 * Gracefully stop the receiver channel
	 */
	void shutdown() {
		shouldStop = true;
		try {
			shutdownRequest.await(1, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			LOGGER.trace("Interrupted while waiting for tasks to stop", e);
		}
		LOGGER.trace("JMS session for {} disconnected", receiverDefinition.getDestinationJndi());
	}

	/**
	 * Gets the original {@link ReceiverDefinition} that was used for initializing the current receiver channel
	 *
	 * @return the receiver definition
	 */
	public ReceiverDefinition getReceiverDefinition() {
		return receiverDefinition;
	}

	/**
	 * Get the current status of the receiver channel.<br>Possible values are:<ul>
	 * <li>WAITING_FOR_MESSAGE - idle state while waiting for messages</li>
	 * <li>PROCESSING - active state while processing a message</li>
	 * <li>FAILED - end state that when closed abnormally (closed the inbound receiver)</li>
	 * <li>STOPPED - end state when closed gracefully by calling {@link #shutdown()} method.</li>
	 * </ul>
	 *
	 * @return the channel status
	 */
	public String getStatus() {
		return status.toString();
	}

	/**
	 * Possible channel statuses
	 */
	private enum Status {
		NOT_STARTED,
		WAITING_FOR_MESSAGE,
		WAITING_TO_RECCONNECT,
		PROCESSING,
		FAILED,
		STOPPED
	}
}
