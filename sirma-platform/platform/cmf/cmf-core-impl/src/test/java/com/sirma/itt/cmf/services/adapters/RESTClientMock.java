package com.sirma.itt.cmf.services.adapters;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.multipart.Part;

import com.sirma.itt.seip.adapters.remote.DMSClientException;
import com.sirma.itt.seip.adapters.remote.RESTClient;

/**
 * The needed rest client impl. It does nothing, all dms adapters should be mocked
 */
public class RESTClientMock implements RESTClient {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -6756998137509780179L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String request(String uri, HttpMethod method) throws DMSClientException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InputStream request(HttpMethod method, String uri) throws DMSClientException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public HttpMethod rawRequest(HttpMethod method, String uri) throws DMSClientException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public HttpMethod createMethod(HttpMethod method, Part[] parts, boolean authentication) {
		// Not used method
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public HttpMethod createMethod(HttpMethod method, String content, boolean authentication)
			throws UnsupportedEncodingException {
		// Not used method
		return null;
	}

	@Override
	public URI buildFullURL(final String relativeURL) throws URIException {
		return null;
	}

	/**
	 * Simulates checks auth for dms rest invokes. If no authentication is available exception is thrown.
	 */
	public static void checkAuthenticationInfo() {
		// FIXME this method should be reimplemented
		throw new IllegalStateException("Must provide username and password for authenticating in the DMS");
	}

	@Override
	public HttpMethod rawRequest(HttpMethod method, URI uri) throws DMSClientException {
		// TODO Auto-generated method stub
		return null;
	}
}
