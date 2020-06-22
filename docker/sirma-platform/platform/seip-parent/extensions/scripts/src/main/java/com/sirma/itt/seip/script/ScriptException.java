package com.sirma.itt.seip.script;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Exception thrown when executing an invalid script in {@link ScriptEvaluator}.
 *
 * @author BBonev
 */
public class ScriptException extends EmfRuntimeException {
	private static final long serialVersionUID = -3996520543221828729L;

	/**
	 * Instantiates a new script exception.
	 */
	public ScriptException() {
		super();
	}

	/**
	 * Instantiates a new script exception.
	 *
	 * @param message
	 *            the message
	 * @param causedBy
	 *            the caused by
	 */
	public ScriptException(String message, Throwable causedBy) {
		super(message, causedBy);
	}

	/**
	 * Instantiates a new script exception.
	 *
	 * @param message
	 *            the message
	 */
	public ScriptException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new script exception.
	 *
	 * @param causedBy
	 *            the caused by
	 */
	public ScriptException(Throwable causedBy) {
		super(causedBy);
	}

}
