package com.sirma.itt.cmf.services.adapters;

import java.io.IOException;
import java.io.InputStream;

import org.jboss.vfs.VirtualFile;

import com.sirma.itt.seip.io.FileDescriptor;

/**
 * File descriptor for local files.
 *
 * @author BBonev
 */
public class VirtualFileDescriptor implements FileDescriptor {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -2730182502454744150L;
	/** The path. */
	protected final VirtualFile path;
	private String id;
	private String containerId;

	/**
	 * Instantiates a new local file descriptor.
	 *
	 * @param path
	 *            the path
	 * @param containerId
	 *            is the container
	 */
	public VirtualFileDescriptor(VirtualFile path, String containerId) {
		this.path = path;
		this.containerId = containerId;
		if (this.path == null) {
			throw new IllegalArgumentException("Cannot create a NULL VirtualFileDescriptor");
		}
		id = path.getName();
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
	public InputStream getInputStream() {
		try {
			return path.openStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "VirtualFileDescriptor [path=" + path + "]";
	}

	@Override
	public String getContainerId() {
		return containerId;
	}

	@Override
	public void close() {
		// nothing to do
	}

}