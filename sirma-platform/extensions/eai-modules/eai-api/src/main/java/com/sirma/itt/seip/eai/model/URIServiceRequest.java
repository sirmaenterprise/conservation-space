package com.sirma.itt.seip.eai.model;

import java.net.URI;

/**
 * The URIServiceReques containing the direct full {@link URI} to remote service. Method could be set by
 * {@link #setMethod(String)} and as param could be used on of the constant in the class. By default {@link #GET_METHOD}
 * is used
 * 
 * @author bbanchev
 */
public class URIServiceRequest implements ServiceRequest {
	private static final long serialVersionUID = 2500408733434345876L;
	/** The GET method method param. */
	public static final String GET_METHOD = "GET";
	private final URI uri;
	private String method = GET_METHOD;

	/**
	 * Instantiates a new URI service request.
	 *
	 * @param uri
	 *            the uri to use
	 */
	public URIServiceRequest(URI uri) {
		this.uri = uri;
	}

	/**
	 * Gets the uri.
	 *
	 * @return the uri
	 */
	public URI getUri() {
		return uri;
	}

	/**
	 * Gets the method at that URI.
	 *
	 * @return the method id - GET, POST, PUT, etc.
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * Sets the method GET, POST, PUT, etc.
	 * 
	 * @param method
	 *            is the http method for that uri
	 */
	public void setMethod(String method) {
		this.method = method;
	}

	@Override
	public String toString() {
		if (uri != null) {
			return uri.toString();
		}
		return "!Missing " + this.getClass().getSimpleName() + " URI argument!";
	}
}
