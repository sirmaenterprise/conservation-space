package com.sirma.itt.emf.audit.solr.service;

/**
 * Exception for problems with Solr.
 *
 * @author Nikolay Velkov
 */
public class SolrServiceException extends Exception {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 6670750343102645669L;

	/**
	 * Overrides super class constructor.
	 */
	public SolrServiceException() {
	}

	/**
	 * Overrides super class constructor.
	 *
	 * @param message
	 *            exception's message
	 */
	public SolrServiceException(String message) {
		super(message);
	}

	/**
	 * Overrides super class constructor.
	 *
	 * @param cause
	 *            exception's cause
	 */
	public SolrServiceException(Throwable cause) {
		super(cause);
	}

	/**
	 * Overrides super class constructor.
	 *
	 * @param message
	 *            exception's message
	 * @param cause
	 *            exception's cause
	 */
	public SolrServiceException(String message, Throwable cause) {
		super(message, cause);
	}
}
