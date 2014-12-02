package com.sirma.itt.emf.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.domain.MessageType;
import com.sirma.itt.emf.domain.VerificationMessage;

/**
 * Helper class to collect validation messages into the local thread stack.
 * 
 * @author BBonev
 */
public class ValidationLoggingUtil {

	/** The Constant ERRORS_CONFIG. */
	private static final String ERRORS_CONFIG = "ERRORS_CONFIG";

	/**
	 * Start error collecting.
	 */
	public static void startErrorCollecting() {
		stopErrorCollecting();
		RuntimeConfiguration.setConfiguration(ERRORS_CONFIG, new LinkedList<String>());
	}

	/**
	 * Stop error collecting.
	 */
	public static void stopErrorCollecting() {
		RuntimeConfiguration.clearConfiguration(ERRORS_CONFIG);
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
		Serializable configuration = RuntimeConfiguration.getConfiguration(ERRORS_CONFIG);
		if ((configuration == null) || !(configuration instanceof List)) {
			return;
		}
		addMessage(messageType, message, (List<VerificationMessage>) configuration);
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
	public static void addMessage(MessageType messageType, String message,
			List<VerificationMessage> messages) {
		messages.add(new ValidationMessageImpl(messageType, message));
	}

	/**
	 * Gets the current messages for the local thread
	 * 
	 * @return the messages
	 */
	@SuppressWarnings("unchecked")
	public static List<VerificationMessage> getMessages() {
		Serializable configuration = RuntimeConfiguration.getConfiguration(ERRORS_CONFIG);
		if ((configuration == null) || !(configuration instanceof List)) {
			// need to enable error collecting first
			startErrorCollecting();
			return (List<VerificationMessage>) RuntimeConfiguration.getConfiguration(ERRORS_CONFIG);
		}
		return (List<VerificationMessage>) configuration;
	}

	/**
	 * Checks for errors in the current thread.
	 * 
	 * @return true, if found errors
	 */
	public static boolean hasErrors() {
		List<VerificationMessage> list = getMessages();
		for (VerificationMessage verificationMessage : list) {
			if (verificationMessage.getErrorType() == MessageType.ERROR) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Copy messages from the source to target list that complain the {@link MessageType} level
	 * 
	 * @param source
	 *            the source list
	 * @param target
	 *            the target list
	 * @param level
	 *            the level to copy
	 */
	public static void copyMessages(List<VerificationMessage> source,
			List<VerificationMessage> target, MessageType... level) {
		Set<MessageType> levels = new HashSet<MessageType>(Arrays.asList(level));
		for (VerificationMessage verificationMessage : source) {
			if (levels.contains(verificationMessage.getErrorType())) {
				target.add(verificationMessage);
			}
		}
	}

	/**
	 * Prints the messages as string
	 * 
	 * @param source
	 *            the source list of messages
	 * @return the string
	 */
	public static String printMessages(List<VerificationMessage> source) {
		if ((source == null) || source.isEmpty()) {
			return "";
		}
		StringBuilder builder = new StringBuilder(source.size() * 200);
		int index = 1;
		for (VerificationMessage verificationMessage : source) {
			builder.append("\n").append(index++).append(". ")
					.append(verificationMessage.getErrorType()).append(": ")
					.append(verificationMessage.getMessage());
		}
		return builder.toString();
	}

	/**
	 * Default implementation for {@link VerificationMessage}
	 * 
	 * @author BBonev
	 */
	public static class ValidationMessageImpl implements VerificationMessage, Serializable {
		/**
		 * Comment for serialVersionUID.
		 */
		private static final long serialVersionUID = -5927544466015218646L;

		/** The error type. */
		private MessageType messageType;

		/** The message. */
		private String message;

		/**
		 * Instantiates a new validation error impl.
		 *
		 * @param messageType
		 *            the error type
		 * @param message
		 *            the message
		 */
		private ValidationMessageImpl(MessageType messageType, String message) {
			this.messageType = messageType;
			this.message = message;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public MessageType getErrorType() {
			return messageType;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getMessage() {
			return message;
		}

	}
}
