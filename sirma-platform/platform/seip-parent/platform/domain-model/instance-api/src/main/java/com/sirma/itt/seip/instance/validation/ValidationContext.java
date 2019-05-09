package com.sirma.itt.seip.instance.validation;

import java.util.LinkedList;
import java.util.List;

import com.sirma.itt.seip.Message;
import com.sirma.itt.seip.MessageType;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.state.Operation;

/**
 * Stores contextual information about any running instance validations. May collect any number of verification messages
 * while the chaining validator is running.
 *
 * @author nvelkov
 */
public class ValidationContext {

	private final Instance instance;
	private final Operation operation;
	private final List<Message> messages;

	/**
	 * Creates a validation context out of the provided instance and operation.
	 *
	 * @param instance
	 *            the instance
	 * @param operation
	 *            the operation
	 */
	public ValidationContext(Instance instance, Operation operation) {
		this.instance = instance;
		this.operation = operation;
		messages = new LinkedList<>();
	}

	/**
	 * Add a message to the list of {@link Message} objects.
	 *
	 * @param messageType
	 *            type of the message
	 * @param message
	 *            text of the message
	 */
	public void addMessage(MessageType messageType, String message) {
		messages.add(new Message(messageType, message));
	}

	/**
	 * Add error type message to the list of {@link Message} objects.
	 * 
	 * @param message text of the message
	 */
	public void addErrorMessage(String message) {
		messages.add(new Message(MessageType.ERROR, message));
	}

	public Instance getInstance() {
		return instance;
	}

	public Operation getOperation() {
		return operation;
	}

	public List<Message> getMessages() {
		return messages;
	}
}