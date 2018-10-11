/**
 * Copyright (c) 2010 09.04.2010 , Sirma ITT.
 */
package com.sirma.sep.xml;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Thrown when a xml validator detects unrecoverable error.
 *
 * @author B.Bonev
 */
public class XmlValidatorError extends EmfRuntimeException {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -24881726215631847L;

	/**
	 * Constructs a new error with <code>null</code> as its detail message. The cause is not initialized, and may
	 * subsequently be initialized by a call to {@link #initCause}.
	 */
	public XmlValidatorError() {
		super();
	}

	/**
	 * Constructs a new error with the specified detail message and cause.
	 * <p>
	 * Note that the detail message associated with <code>cause</code> is <i>not</i> automatically incorporated in this
	 * error's detail message.
	 *
	 * @param message
	 *            the detail message (which is saved for later retrieval by the {@link #getMessage()} method).
	 * @param cause
	 *            the cause (which is saved for later retrieval by the {@link #getCause()} method). (A <tt>null</tt>
	 *            value is permitted, and indicates that the cause is nonexistent or unknown.)
	 */
	public XmlValidatorError(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new error with the specified detail message. The cause is not initialized, and may subsequently be
	 * initialized by a call to {@link #initCause}.
	 *
	 * @param message
	 *            the detail message. The detail message is saved for later retrieval by the {@link #getMessage()}
	 *            method.
	 */
	public XmlValidatorError(String message) {
		super(message);
	}

	/**
	 * Constructs a new error with the specified cause and a detail message of
	 * <tt>(cause==null ? null : cause.toString())</tt> (which typically contains the class and detail message of
	 * <tt>cause</tt>). This constructor is useful for errors that are little more than wrappers for other throwables.
	 *
	 * @param cause
	 *            the cause (which is saved for later retrieval by the {@link #getCause()} method). (A <tt>null</tt>
	 *            value is permitted, and indicates that the cause is nonexistent or unknown.)
	 */
	public XmlValidatorError(Throwable cause) {
		super(cause);
	}

}
