package com.sirma.itt.seip.instance.version.compare;

/**
 * * Thrown to indicate that there is a problem with the versions compare.
 *
 * @author A. Kunchev
 */
public class VersionCompareException extends RuntimeException {

	private static final long serialVersionUID = -8075494065060559184L;

	/**
	 * Instantiates a new compare versions exception.
	 */
	public VersionCompareException() {
		super();
	}

	/**
	 * Instantiates a new compare versions exception.
	 *
	 * @param message
	 *            the exception message
	 */
	public VersionCompareException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new compare versions exception.
	 *
	 * @param message
	 *            the exception message
	 * @param cause
	 *            the cause for the exception
	 */
	public VersionCompareException(String message, Throwable cause) {
		super(message, cause);
	}

}
