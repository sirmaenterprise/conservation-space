package com.sirma.itt.emf.solr.exception;

/**
 * The SolrClientException wrap all the exceptions that are caught during remote solr invocations
 */
public class SolrClientException extends Exception {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 3007111310510799919L;

	/**
	 * Instantiates a new solr client exception.
	 *
	 * @param throwable
	 *            the exception
	 */
	public SolrClientException(Exception throwable) {
		super(throwable);
	}

	/**
	 * Instantiates a new solr client exception.
	 *
	 * @param message
	 *            is the error message
	 * @param throwable
	 *            the exception
	 */
	public SolrClientException(String message, Exception throwable) {
		super(message, throwable);
	}

}
