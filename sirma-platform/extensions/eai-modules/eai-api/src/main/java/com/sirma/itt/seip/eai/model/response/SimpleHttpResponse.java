package com.sirma.itt.seip.eai.model.response;

import com.sirma.itt.seip.eai.model.ServiceResponse;

/**
 * A simple http based response containing the status and status description.
 */
public class SimpleHttpResponse implements ServiceResponse {

	private int statusCode;
	private String statusLine;

	/**
	 * Instantiates a new simple http response.
	 *
	 * @param statusCode
	 *            the status code
	 * @param statusLine
	 *            the status line
	 */
	public SimpleHttpResponse(int statusCode, String statusLine) {
		this.statusCode = statusCode;
		this.statusLine = statusLine;
	}

	/**
	 * Gets the status code.
	 *
	 * @return the status code
	 */
	public int getStatusCode() {
		return statusCode;
	}

	/**
	 * Gets the status line.
	 *
	 * @return the status line
	 */
	public String getStatusLine() {
		return statusLine;
	}

}
