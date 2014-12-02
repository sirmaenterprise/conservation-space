package com.sirma.itt.cmf.beans;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.apache.log4j.lf5.util.StreamUtils;

import com.sirma.itt.emf.adapter.WritableFileDescriptor;

/**
 * File descriptor for local files.
 *
 * @author BBonev
 */
public class LocalFileDescriptor implements WritableFileDescriptor {

	private static final Logger LOGGER = Logger.getLogger(LocalFileDescriptor.class);

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -2730182502454744150L;
	/** The path. */
	protected final String path;

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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getId() {
		return path;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InputStream getInputStream() {
		try {
			return new FileInputStream(getId());
		} catch (FileNotFoundException e) {
			LOGGER.warn("Failed to locate the file " + getId(), e);
		}
		return null;
	}

	@Override
	public void write(InputStream inputStream) {
		if ((getId() == null) || (inputStream == null)) {
			return;
		}
		try {
			StreamUtils.copyThenClose(inputStream, new FileOutputStream(getId()));
		} catch (IOException e) {
			LOGGER.warn("Failed to write to " + getId(), e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		LocalFileDescriptor other = (LocalFileDescriptor) obj;
		if (path == null) {
			if (other.path != null) {
				return false;
			}
		} else if (!path.equals(other.path)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "LocalFileDescriptior [path=" + path + "]";
	}

	@Override
	public String getContainerId() {
		return null;
	}

}
