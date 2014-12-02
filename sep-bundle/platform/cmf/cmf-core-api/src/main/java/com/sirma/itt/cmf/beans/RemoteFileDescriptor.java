package com.sirma.itt.cmf.beans;

import java.io.InputStream;

import com.sirma.itt.emf.adapter.FileDescriptor;

/**
 * @author y.yordanov
 */
public class RemoteFileDescriptor implements FileDescriptor {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -2730182502454744150L;
	/** The path. */
	private final String path;

	/**
	 * Instantiates a new local file descriptor.
	 * 
	 * @param uri
	 *            the path
	 */
	public RemoteFileDescriptor(String uri) {
		this.path = uri;
		if (this.path == null) {
			throw new IllegalArgumentException("Cannot create a NULL RemoteFileDescriptor");
		}
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
		// try {
		// Object content = new URL(path).openConnection().getContent();
		// } catch (FileNotFoundException e) {
		// e.printStackTrace();
		// } catch (MalformedURLException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// return null;
		throw new UnsupportedOperationException("Use uri: '" + path + "' to access content");
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RemoteFileDescriptor [path=" + path + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RemoteFileDescriptor other = (RemoteFileDescriptor) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}

	@Override
	public String getContainerId() {
		return null;
	}

}
