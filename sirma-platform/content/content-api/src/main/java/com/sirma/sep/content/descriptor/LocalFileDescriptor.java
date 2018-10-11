package com.sirma.sep.content.descriptor;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.io.WritableFileDescriptor;

/**
 * File descriptor for local files.
 *
 * @author BBonev
 */
public class LocalFileDescriptor implements WritableFileDescriptor {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final long serialVersionUID = -2730182502454744150L;
	/** The path. */
	protected String path;

	/**
	 * Instantiates a new local file descriptor.
	 */
	protected LocalFileDescriptor() {
		// default constructor to allow serialization util to work with the class
	}

	/**
	 * Instantiates a new local file descriptor.
	 *
	 * @param path
	 *            the path
	 */
	public LocalFileDescriptor(String path) {
		this.path = path;
		if (this.path == null) {
			throw new IllegalArgumentException("Cannot create a LocalFileDescriptior for NULL path");
		}
	}

	/**
	 * Instantiates a new local file descriptor.
	 *
	 * @param file
	 *            the source file
	 */
	public LocalFileDescriptor(File file) {
		if (file == null) {
			throw new IllegalArgumentException("Cannot create a LocalFileDescriptior for NULL file");
		}
		path = file.getAbsolutePath();
	}

	@Override
	public String getId() {
		return path;
	}

	@Override
	public InputStream getInputStream() {
		try {
			return new FileInputStream(getId());
		} catch (FileNotFoundException e) {
			LOGGER.warn("Failed to locate the file {}", getId(), e);
		}
		return null;
	}

	@Override
	public void write(InputStream inputStream) {
		if (getId() == null || inputStream == null) {
			return;
		}
		try (InputStream input = inputStream; OutputStream output = new FileOutputStream(getId())) {
			IOUtils.copyLarge(inputStream, output);
		} catch (IOException e) {
			LOGGER.warn("Failed to write to {}", getId(), e);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof FileDescriptor)) {
			return false;
		}
		FileDescriptor other = (FileDescriptor) obj;
		return nullSafeEquals(getId(), other.getId());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (path == null ? 0 : path.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "LocalFileDescriptior [path=" + path + "]";
	}

	@Override
	public String getContainerId() {
		return null;
	}

	@Override
	public void close() {
		if (path != null && new File(path).delete()) {
			// cleaned
		}
	}

	@Override
	public long length() {
		return new File(getId()).length();
	}

}
