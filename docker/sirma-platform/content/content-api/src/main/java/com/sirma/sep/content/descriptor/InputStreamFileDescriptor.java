package com.sirma.sep.content.descriptor;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.sirma.itt.seip.io.FileDescriptor;

/**
 * File descriptor for input stream.
 *
 * @author BBonev
 */
public class InputStreamFileDescriptor implements FileDescriptor {

	private static final long serialVersionUID = -8400409459380868147L;

	private InputStream inputStream;

	private String id;

	/**
	 * Instantiates a new input stream file descriptor.
	 *
	 * @param id
	 *            the id
	 * @param inputStream
	 *            the input stream
	 */
	public InputStreamFileDescriptor(String id, InputStream inputStream) {
		this.id = id;
		this.inputStream = inputStream;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getContainerId() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InputStream getInputStream() {
		return inputStream;
	}

	@Override
	public void close() {
		if (inputStream != null) {
			IOUtils.closeQuietly(inputStream);
			inputStream = null;
		}
	}
}
