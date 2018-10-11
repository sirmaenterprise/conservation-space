package com.sirma.sep.content.descriptor;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.InputStream;

import com.sirma.itt.seip.io.FileDescriptor;

/**
 * @author y.yordanov
 */
public class RemoteFileDescriptor implements FileDescriptor {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -2730182502454744150L;
	/** The path. */
	private final String remoteNode;

	/**
	 * Instantiates a new local file descriptor.
	 *
	 * @param uri
	 *            the path
	 */
	public RemoteFileDescriptor(String uri) {
		remoteNode = uri;
		if (remoteNode == null) {
			throw new IllegalArgumentException("Cannot create a NULL RemoteFileDescriptor");
		}
	}

	@Override
	public String getId() {
		return remoteNode;
	}

	@Override
	public InputStream getInputStream() {
		throw new UnsupportedOperationException("Use uri: '" + remoteNode + "' to access content");
	}

	@Override
	public String toString() {
		return "RemoteFileDescriptor [path=" + remoteNode + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (remoteNode == null ? 0 : remoteNode.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (!(object instanceof FileDescriptor)) {
			return false;
		}
		FileDescriptor other = (FileDescriptor) object;
		return nullSafeEquals(remoteNode, other.getId());
	}

	@Override
	public String getContainerId() {
		return null;
	}

	@Override
	public void close() {
		// nothing to do
	}

}
