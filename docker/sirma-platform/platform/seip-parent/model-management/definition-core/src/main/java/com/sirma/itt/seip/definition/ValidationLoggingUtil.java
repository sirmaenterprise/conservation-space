package com.sirma.itt.seip.definition;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.MessageType;
import com.sirma.itt.seip.Message;
import com.sirma.itt.seip.context.RuntimeContext;

/**
 * Helper class to collect validation messages into the local thread stack.
 *
 * @author BBonev
 */
public class ValidationLoggingUtil {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ValidationLoggingUtil.class);

	/** The Constant ERRORS_CONFIG. */
	private static final String ERRORS_CONFIG = "ERRORS_CONFIG";

	private static final Map<String, ValidationMessageHolder> MAP = new ConcurrentHashMap<>();

	/**
	 * Instantiates a new validation logging util.
	 */
	private ValidationLoggingUtil() {
		// utility class
	}

	/**
	 * Initialize logging by using the given message holder. The method could not be called from the same thread that
	 * produced the message holder.
	 *
	 * @param holder
	 *            the holder
	 */
	public static void initialize(ValidationMessageHolder holder) {
		if (holder.isFromCurrentThread()) {
			return;
		}
		RuntimeContext.setConfiguration(ERRORS_CONFIG, (Serializable) holder.getMessages());
	}

	/**
	 * Start error collecting.
	 */
	public static void startErrorCollecting() {
		stopErrorCollecting();
		RuntimeContext.setConfiguration(ERRORS_CONFIG,
				(Serializable) Collections.synchronizedList(new LinkedList<String>()));
	}

	/**
	 * Stop error collecting.
	 */
	public static void stopErrorCollecting() {
		RuntimeContext.clearConfiguration(ERRORS_CONFIG);
	}

	/**
	 * Stops error collecting if the current thread is not the same as the one that produced the holder, otherwise the
	 * method does nothing.
	 *
	 * @param holder
	 *            the holder
	 */
	public static void stopErrorCollecting(ValidationMessageHolder holder) {
		if (holder != null && holder.isFromCurrentThread()) {
			return;
		}
		stopErrorCollecting();
	}

	/**
	 * Adds the error message for the current thread.
	 *
	 * @param message
	 *            the message
	 */
	public static void addErrorMessage(String message) {
		addMessage(MessageType.ERROR, message);
	}

	/**
	 * Adds the info message for the current thread.
	 *
	 * @param message
	 *            the message
	 */
	public static void addInfoMessage(String message) {
		addMessage(MessageType.INFO, message);
	}

	/**
	 * Adds the warning message for the current thread.
	 *
	 * @param message
	 *            the message
	 */
	public static void addWarningMessage(String message) {
		addMessage(MessageType.WARNING, message);
	}

	/**
	 * Adds a message of the given type to the list of messages for the current thread.
	 *
	 * @param messageType
	 *            the error type
	 * @param message
	 *            the message
	 */
	@SuppressWarnings("unchecked")
	public static void addMessage(MessageType messageType, String message) {
		Serializable configuration = RuntimeContext.getConfiguration(ERRORS_CONFIG);
		if (!(configuration instanceof List)) {
			return;
		}
		addMessage(messageType, message, (List<Message>) configuration);
	}

	/**
	 * Adds a message of the given type to the given list.
	 *
	 * @param messageType
	 *            the error type
	 * @param message
	 *            the message
	 * @param messages
	 *            the target messages
	 */
	public static void addMessage(MessageType messageType, String message, List<Message> messages) {
		messages.add(new Message(messageType, message));
	}

	/**
	 * Gets the current messages for the local thread.
	 *
	 * @return the messages
	 */
	@SuppressWarnings("unchecked")
	public static List<Message> getMessages() {
		Serializable configuration = RuntimeContext.getConfiguration(ERRORS_CONFIG);
		if (!(configuration instanceof List)) {
			// need to enable error collecting first
			startErrorCollecting();
			return (List<Message>) RuntimeContext.getConfiguration(ERRORS_CONFIG);
		}
		return (List<Message>) configuration;
	}

	/**
	 * Checks for errors in the current thread.
	 *
	 * @return true, if found errors
	 */
	public static boolean hasErrors() {
		List<Message> list = getMessages();
		for (Message verificationMessage : list) {
			if (verificationMessage.getErrorType() == MessageType.ERROR) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Copy messages from the source to target list that complain the {@link MessageType} level.
	 *
	 * @param source
	 *            the source list
	 * @param target
	 *            the target list
	 * @param level
	 *            the level to copy
	 */
	public static void copyMessages(List<Message> source, List<Message> target,
			MessageType... level) {
		Set<MessageType> levels = new HashSet<>(Arrays.asList(level));
		for (Message verificationMessage : source) {
			if (levels.contains(verificationMessage.getErrorType())) {
				target.add(verificationMessage);
			}
		}
	}

	/**
	 * Acquire new message holder for the current thread. It could have only one instance per source thread. The holder
	 * could be used to call the methods {@link #initialize(ValidationMessageHolder)},
	 * {@link #stopErrorCollecting(ValidationMessageHolder)} and {@link #releaseMessageHolder(ValidationMessageHolder)}.
	 * The object could be used to transfer the message collection over multiple threads.
	 *
	 * @return the validation message holder
	 */
	public static ValidationMessageHolder acquireMessageHolder() {
		ValidationMessageHolder holder = MAP.get(Thread.currentThread().getName());
		if (holder == null) {
			holder = new ValidationMessageHolder(getMessages());
			MAP.put(holder.threadName, holder);
		}
		return holder;
	}

	/**
	 * Release message holder when the using thread finishes it's logging. This method should be called if received a
	 * holder object after finishing work
	 *
	 * @param holder
	 *            the holder
	 */
	public static void releaseMessageHolder(ValidationMessageHolder holder) {
		if (holder == null) {
			return;
		}
		if (holder.isFromCurrentThread()) {
			return;
		}
		ValidationMessageHolder messageHolder = MAP.remove(holder.threadName);
		stopErrorCollecting(holder);
		messageHolder.notifyForDone();
	}

	/**
	 * Wait for leased message holder. This method should be called on the thread that initiated message collecting
	 * before getting the latest {@link #getMessages()}.
	 */
	public static void waitForLeasedMessageHolder() {
		ValidationMessageHolder holder = MAP.get(Thread.currentThread().getName());
		if (holder == null) {
			return;
		}
		holder.waitForDone();
	}

	/**
	 * Prints the messages as string.
	 *
	 * @param source
	 *            the source list of messages
	 * @return the string
	 */
	public static String printMessages(List<Message> source) {
		if (source == null || source.isEmpty()) {
			return "";
		}
		StringBuilder builder = new StringBuilder(source.size() * 200);
		int index = 1;
		for (Message verificationMessage : source) {
			builder
					.append("\n")
						.append(index++)
						.append(". ")
						.append(verificationMessage.getErrorType())
						.append(": ")
						.append(verificationMessage.getMessage());
		}
		return builder.toString();
	}

	/**
	 * The Class ValidationMessageHolder.
	 */
	public static class ValidationMessageHolder {

		private final List<Message> messages;
		private final String threadName;
		private Object lock = new Object();

		/**
		 * Instantiates a new validation message holder.
		 *
		 * @param messages
		 *            the messages
		 */
		private ValidationMessageHolder(List<Message> messages) {
			threadName = Thread.currentThread().getName();
			this.messages = messages;
		}

		/**
		 * Gets the messages.
		 *
		 * @return the messages
		 */
		public List<Message> getMessages() {
			return messages;
		}

		/**
		 * Checks if is from current thread.
		 *
		 * @return true, if is from current thread
		 */
		private boolean isFromCurrentThread() {
			return Thread.currentThread().getName().equals(threadName);
		}

		/**
		 * Notify for done.
		 */
		private void notifyForDone() {
			synchronized (lock) {
				lock.notifyAll();
			}
		}

		/**
		 * Wait for done.
		 */
		private void waitForDone() {
			synchronized (lock) {
				try {
					lock.wait(15000);
				} catch (InterruptedException e) {
					LOGGER.trace("Timeout when waiting for leased validation messages. Will continue without them", e);
				}
			}
		}
	}

}
