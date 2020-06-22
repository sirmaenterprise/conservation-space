package com.sirma.itt.seip.eai.content.tool.service.net;

import java.net.URI;

/**
 * Builder to simplify building of composite {@link URI} objects
 * 
 * @author bbanchev
 */
public class URIBuilder {
	private StringBuilder builder = new StringBuilder(100);

	/**
	 * Instantiate a new {@link URIBuilder}
	 * 
	 * @param baseURI
	 *            is the root uri - in most cases absolute uri with the protocol
	 */
	public URIBuilder(URI baseURI) {
		String path = baseURI.toString();
		// remove last /
		if (baseURI.getPath().endsWith("/")) {
			builder.append(path.substring(0, path.length() - 1));
		} else {
			builder.append(path);
		}
	}

	/**
	 * Appends uri part to current uri as suffix
	 * 
	 * @param uri
	 *            the uri part to append
	 * @return the current builder
	 */
	public URIBuilder append(String uri) {
		String localURI = uri;
		char lastURIChar = builder.charAt(builder.length() - 1);
		if (!localURI.startsWith("/") && lastURIChar != '/') {
			builder.append("/");
		} else if (localURI.startsWith("/") && lastURIChar == '/') {
			builder.replace(builder.length() - 1, builder.length(), "");
		}
		builder.append(localURI);
		return this;
	}

	/**
	 * Appends query param
	 *
	 * @param name parameter name
	 * @param value parameter value
	 * @return the current builder
	 */
	public URIBuilder addParam(String name, String value) {
		char separator = builder.indexOf("?") >= 0 ? '&' : '?';
		builder.append(separator).append(name).append('=').append(value);
		return this;
	}

	/**
	 * Builds a URI based on the provided data using {@link #append(String)}
	 * 
	 * @return the uri built from the current state
	 */
	public URI build() {
		return URI.create(builder.toString());
	}

}
