package com.sirma.itt.seip.help.exception;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * The HelpGenerationException indicates exceptions during help generation.
 */
public class HelpGenerationException extends EmfRuntimeException {

	/** Comment for serialVersionUID. */
	private static final long serialVersionUID = -9061422019926675225L;

	/**
	 * Instantiates a new help generation exception.
	 */
	public HelpGenerationException() {
		super();
	}

	/**
	 * Instantiates a new help generation exception.
	 *
	 * @param message
	 *            the message
	 * @param causedBy
	 *            the caused by
	 */
	public HelpGenerationException(String message, Throwable causedBy) {
		super(message, causedBy);
	}

	/**
	 * Instantiates a new help generation exception.
	 *
	 * @param message
	 *            the message
	 */
	public HelpGenerationException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new help generation exception.
	 *
	 * @param causedBy
	 *            the caused by
	 */
	public HelpGenerationException(Throwable causedBy) {
		super(causedBy);
	}

}
