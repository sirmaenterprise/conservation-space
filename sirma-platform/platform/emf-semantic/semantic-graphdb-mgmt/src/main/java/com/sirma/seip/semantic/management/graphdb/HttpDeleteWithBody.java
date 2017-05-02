package com.sirma.seip.semantic.management.graphdb;

import java.net.URI;

import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

/**
 * Delete method with context.
 *
 * @author KPenev
 */
@NotThreadSafe
public class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {

	public static final String METHOD_NAME = "DELETE";

	/**
	 * Gets the method.
	 *
	 * @return the method
	 */
	@Override
	public String getMethod() {
		return METHOD_NAME;
	}

	/**
	 * Instantiates a new http delete with body.
	 *
	 * @param uri
	 *            the uri
	 */
	public HttpDeleteWithBody(final String uri) {
		super();
		setURI(URI.create(uri));
	}

	/**
	 * Instantiates a new http delete with body.
	 *
	 * @param uri
	 *            the uri
	 */
	public HttpDeleteWithBody(final URI uri) {
		super();
		setURI(uri);
	}

	/**
	 * Instantiates a new http delete with body.
	 */
	public HttpDeleteWithBody() {
		super();
	}
}