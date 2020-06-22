package com.sirma.itt.emf.solr.exception;

import com.sirma.itt.seip.exception.EmfException;

/**
 * The SolrSchemaException indicated errors during solr model updates/read.
 */
public class SolrSchemaException extends EmfException {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -4779212839369152168L;

	/**
	 * Instantiates a new solr schema exception.
	 *
	 * @param message
	 *            the message
	 */
	public SolrSchemaException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new solr schema exception.
	 */
	public SolrSchemaException() {
		super();
	}

	/**
	 * Instantiates a new solr schema exception.
	 *
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public SolrSchemaException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new solr schema exception.
	 *
	 * @param cause
	 *            the cause
	 */
	public SolrSchemaException(Throwable cause) {
		super(cause);
	}

}
