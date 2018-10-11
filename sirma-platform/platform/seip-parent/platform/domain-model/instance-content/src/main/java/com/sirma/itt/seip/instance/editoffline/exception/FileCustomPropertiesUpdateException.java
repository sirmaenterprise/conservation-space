package com.sirma.itt.seip.instance.editoffline.exception;

import java.io.Serializable;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Exception thrown to identify an error while performing edit offline file custom properties update operations.
 *
 * @author T. Dossev
 */
public class FileCustomPropertiesUpdateException extends EmfRuntimeException {

	private static final long serialVersionUID = -6980406906671813773L;

	private final Serializable fileName;

	/**
	 * Instantiates a new FileCustomPropertiesUpdateException.
	 *
	 * @param fileName
	 *            the file name
	 * @param message
	 *            the message
	 */
	public FileCustomPropertiesUpdateException(Serializable fileName, String message) {
		super(message);
		this.fileName = fileName;
	}


	/**
	 * Instantiates a new FileCustomPropertiesUpdateException.
	 *
	 * @param fileName
	 *            the file name
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public FileCustomPropertiesUpdateException(Serializable fileName, String message, Throwable cause) {
		super(message, cause);
		this.fileName = fileName;
	}


	/**
	 * Gets the file name that triggered the exception.
	 *
	 * @return the file name
	 */
	public Serializable getFileName() {
		return fileName;
	}

}
