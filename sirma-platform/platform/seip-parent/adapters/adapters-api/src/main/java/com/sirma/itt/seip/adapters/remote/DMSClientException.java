package com.sirma.itt.seip.adapters.remote;

/**
 * The DMSClientException is thrown on communication or on dms error.
 */
public class DMSClientException extends Exception {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -6872734097364449693L;
	/** the http status. */
	private final int statusCode;

	/**
	 * Instantiates a new dMS client exception.
	 *
	 * @param message
	 *            the message
	 * @param statusCode
	 *            is the http status
	 */
	public DMSClientException(String message, int statusCode) {
		super(message);
		this.statusCode = statusCode;
	}

	/**
	 * Instantiates a new dMS client exception.
	 *
	 * @param string
	 *            the string
	 * @param e
	 *            the caught exception
	 * @param statusCode
	 *            is the http status
	 */
	public DMSClientException(String string, Exception e, int statusCode) {
		super(string, e);
		this.statusCode = statusCode;
	}

	/**
	 * Gets the http status code.
	 *
	 * @return the status code
	 */
	public int getStatusCode() {
		return statusCode;
	}

}
