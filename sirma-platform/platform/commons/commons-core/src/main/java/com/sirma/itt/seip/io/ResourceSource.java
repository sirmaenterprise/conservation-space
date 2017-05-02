package com.sirma.itt.seip.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Represent a resource by name. Supported formats are are absolute files or classpath resources
 * 
 * @author bbanchev
 */
public class ResourceSource {
	private String name;
	private boolean isAbsolute;
	private File asFile;

	/**
	 * Instantiates a new file resource.
	 *
	 * @param name
	 *            the name
	 */
	public ResourceSource(String name) {
		if (name == null) {
			throw new EmfRuntimeException("Resource name is required argument!");
		}
		this.name = name;
		this.asFile = new File(name);
		if (asFile.isAbsolute()) {
			isAbsolute = true;
		}
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Checks if is accessible.
	 *
	 * @return true, if is accessible
	 */
	public boolean isAccessible() {
		if (isAbsolute) {
			return asFile.canRead();
		}
		return this.getClass().getClassLoader().getResource(name) != null;
	}

	/**
	 * Load as stream the resource. A client responsibility is to close the stream
	 *
	 * @return the input stream or null or might throw exception.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public InputStream load() throws IOException {
		if (isAbsolute) {
			return new FileInputStream(asFile);
		}
		return this.getClass().getClassLoader().getResourceAsStream(name);
	}

	@Override
	public String toString() {
		return name;
	}
}
