package com.sirma.itt.emf.script;

import com.sirma.itt.emf.exceptions.EmfRuntimeException;

/**
 * Exception thrown when executing an invalid script in {@link ScriptEvaluator}.
 * 
 * @author BBonev
 */
public class ScriptException extends EmfRuntimeException {

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
