package com.sirma.itt.seip.instance.editoffline.exception;

import java.io.Serializable;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Exception thrown to identify an unsupported or non MSOffice mime type while performing edit offline file custom
 * properties update operations.
 *
 * @author T. Dossev
 */
public class FileNotSupportedException extends EmfRuntimeException {

	private static final long serialVersionUID = -8470903436371224018L;

	private final Serializable mimeType;

	/**
	 * Instantiates a new FileNotSupportedException.
	 *
	 * @param mimeType
	 *            the mime type
	 * @param message
	 *            the message
	 */
	public FileNotSupportedException(Serializable mimeType, String message) {
		super(message);
		this.mimeType = mimeType;
	}

	/**
	 * Instantiates a new FileNotSupportedException.
	 *
	 * @param mimeType
	 *            the mime type
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public FileNotSupportedException(Serializable mimeType, String message, Throwable cause) {
		super(message, cause);
		this.mimeType = mimeType;
	}


	/**
	 * Gets the mime type that triggered the exception.
	 *
	 * @return the mime type
	 */
	public Serializable getmimeType() {
		return mimeType;
	}

}
