package com.sirmaenterprise.sep.models;

import java.util.List;

/**
 * Thrown when there are errors during model import process.
 *
 * @author Adrian Mitev
 */
public class ModelImportException extends RuntimeException {

	private static final long serialVersionUID = -3928641510891595194L;

	private final List<String> messages;

	/**
	 * Initializes the messages list.
	 *
	 * @param messages
	 */
	public ModelImportException(List<String> messages) {
		this.messages = messages;
	}

	public List<String> getMessages() {
		return messages;
	}

}
