package com.sirma.itt.seip.eai.content.tool.model;

import java.io.InputStream;
import java.net.URI;
import java.util.Objects;

/**
 * Represents an information about a saved content in the system. Provides all known information about it and means to
 * read/access it.
 *
 * @author gshevkedov
 * @author bbanchev
 */
public class ContentInfo {
	private InputStream inputStream;
	private String mimetype;
	private URI path;
	private String name;

	/**
	 * Instantiates new content info.
	 *
	 * @param mimetype
	 *            the mimetype. Required argument
	 * @param path
	 *            is the path of the request
	 * @param inputStream
	 *            the input stream. Required argument
	 */
	public ContentInfo(String mimetype, URI path, InputStream inputStream) {
		Objects.requireNonNull(inputStream);
		this.mimetype = mimetype;
		this.path = path;
		this.inputStream = inputStream;
	}

	/**
	 * Gets the input stream.
	 *
	 * @return the input stream
	 */
	public InputStream getInputStream() {
		return inputStream;
	}

	/**
	 * Gets the mimetype.
	 *
	 * @return the mimetype
	 */
	public String getMimetype() {
		return mimetype;
	}

	/**
	 * Returns the {@link URI} for this content.
	 *
	 * @return the uri
	 */
	public URI getPath() {
		return path;
	}

	/**
	 * Gets the name for the content data.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name
	 *            the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

}
