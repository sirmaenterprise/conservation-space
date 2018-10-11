package com.sirma.itt.seip.domain.exceptions;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Thrown when validating the definitions model.
 *
 * @author BBonev
 */
public class DefinitionValidationException extends EmfRuntimeException {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 7510675472677763684L;

	/**
	 * Instantiates a new cmf definition validation exception.
	 */
	public DefinitionValidationException() {
		// nothing to do here
	}

	/**
	 * Instantiates a new cmf definition validation exception.
	 *
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public DefinitionValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new cmf definition validation exception.
	 *
	 * @param message
	 *            the message
	 */
	public DefinitionValidationException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new cmf definition validation exception.
	 *
	 * @param cause
	 *            the cause
	 */
	public DefinitionValidationException(Throwable cause) {
		super(cause);
	}

}
