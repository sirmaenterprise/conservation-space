package com.sirma.sep.content.descriptor;

import java.io.InputStream;

import com.sirma.itt.seip.io.FileDescriptor;

/**
 * Resource file descriptor for files located into the current class loader and can be retrieved by
 * {@link Class#getResourceAsStream(String)}.
 *
 * @author BBonev
 */
public class ResourceFileDescriptor implements FileDescriptor {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 171100842306559856L;

	/** The name. */
	private String name;

	/** The clazz. */
	private Class<?> clazz;

	/**
	 * Instantiates a new resource file descriptor.
	 *
	 * @param name
	 *            the name of the resource
	 * @param relativeClass
	 *            the relative class to that resource
	 */
	public ResourceFileDescriptor(String name, Class<?> relativeClass) {
		this.name = name;
		clazz = relativeClass;
		if (name == null || relativeClass == null) {
			throw new IllegalArgumentException("Cannot create NULL ResourceFileDescriptor");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getId() {
		return name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InputStream getInputStream() {
		return clazz.getResourceAsStream(getId());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (clazz == null ? 0 : clazz.hashCode());
		result = prime * result + (name == null ? 0 : name.hashCode());
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
		ResourceFileDescriptor other = (ResourceFileDescriptor) obj;
		if (clazz == null) {
			if (other.clazz != null) {
				return false;
			}
		} else if (!clazz.equals(other.clazz)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "ResourceFileDescriptor [name=" + name + ", clazz=" + clazz + "]";
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
