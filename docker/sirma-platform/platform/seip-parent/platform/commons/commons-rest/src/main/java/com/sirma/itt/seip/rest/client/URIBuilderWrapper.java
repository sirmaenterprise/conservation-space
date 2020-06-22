package com.sirma.itt.seip.rest.client;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.utils.URIBuilder;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Builder to simplify building of composite {@link URI} objects by adapting {@link URIBuilder}
 * 
 * @author bbanchev
 */
public class URIBuilderWrapper extends URIBuilder {

	/**
	 * Instantiate a new {@link URIBuilderWrapper}
	 * 
	 * @param baseURI
	 *            is the root {@link URI} as {@link String} - in most cases absolute uri with the protocol
	 */
	public URIBuilderWrapper(String... baseURI) {
		super(createURIByPaths((Object[]) baseURI));
	}

	/**
	 * Instantiate a new {@link URIBuilderWrapper}
	 * 
	 * @param baseURI
	 *            is the root {@link URI} - in most cases absolute uri with the protocol
	 */
	public URIBuilderWrapper(URI baseURI) {
		super(createURIByPaths(baseURI));
	}

	/**
	 * Builds a URI based on the provided path data using {@link #append(StringBuilder, String)}. Algorithm takes care
	 * to append all needed /. No additional processing or validation is done.
	 * 
	 * @param baseURI
	 *            are the set of paths to append.
	 * @return the uri built from the current state
	 */
	public static URI createURIByPaths(Object... baseURI) {
		if (baseURI == null || baseURI.length == 0) {
			return URI.create("/");
		}
		StringBuilder uriBuilder = new StringBuilder(100);
		for (Object uriPath : baseURI) {
			append(uriBuilder, uriPath);
		}
		return URI.create(uriBuilder.toString());
	}

	/**
	 * Appends uri part to current uri as suffix
	 * 
	 * @param uriBuilder
	 *            is the builder to use
	 * @param uri
	 *            the uri part to append
	 */
	private static void append(StringBuilder uriBuilder, Object uri) {
		if (uriBuilder.length() == 0) {
			uriBuilder.append(uri);
			return;
		}
		String localURI;
		if (uri instanceof URI) {
			localURI = ((URI) uri).toASCIIString();
		} else if (uri instanceof String) {
			localURI = (String) uri;
		} else if (uri instanceof URIBuilderWrapper) {
			localURI = ((URIBuilderWrapper) uri).build().toASCIIString();
		} else {
			throw new EmfRuntimeException("Unsupported URI component: " + uri);
		}
		char lastURIChar = uriBuilder.charAt(uriBuilder.length() - 1);
		if (localURI.indexOf('/') != 0 && lastURIChar != '/') {
			uriBuilder.append('/');
		} else if (localURI.indexOf('/') == 0 && lastURIChar == '/') {
			uriBuilder.replace(uriBuilder.length() - 1, uriBuilder.length(), "");
		}
		uriBuilder.append(localURI);
	}

	/**
	 * {@inheritDoc}.<br>
	 * Wraps the {@link URISyntaxException} and throws {@link EmfRuntimeException} in that case.
	 */
	@Override
	public URI build() {
		try {
			return super.build();
		} catch (URISyntaxException e) {
			throw new EmfRuntimeException("Could not build URI!", e);
		}
	}
}
