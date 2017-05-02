package com.sirma.itt.emf.semantic.exception;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * {@link RuntimeException} thrown to indicate that an error has occurred while storing data in the underlying semantic
 * repository
 *
 * @author Valeri Tishev
 */
public class SemanticPersistenceException extends EmfRuntimeException {

	private static final long serialVersionUID = 8023837369829285502L;

	/**
	 * Instantiates a new semantic persistence exception.
	 */
	public SemanticPersistenceException() {
		super();
	}

	/**
	 * Instantiates a new semantic persistence exception.
	 *
	 * @param message
	 *            the message
	 */
	public SemanticPersistenceException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new semantic persistence exception.
	 *
	 * @param cause
	 *            the cause
	 */
	public SemanticPersistenceException(Throwable cause) {
		super(cause);
	}

	/**
	 * Instantiates a new semantic persistence exception.
	 *
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public SemanticPersistenceException(String message, Throwable cause) {
		super(message, cause);
	}

}
