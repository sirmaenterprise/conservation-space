package com.sirma.itt.faces.resources;

import com.google.common.base.Strings;

/**
 * Represents a resource identifier. Resources are identified by library and
 * 
 * @author Adrian Mitev
 */
public class ResourceId {

	private final String library;
	private final String name;

	/**
	 * Initializing constructor.
	 */
	public ResourceId(String library, String name) {
		this.library = library;
		this.name = name;
	}

	/**
	 * Getter method for name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Getter method for library.
	 * 
	 * @return the library
	 */
	public String getLibrary() {
		return library;
	}

	@Override
	public String toString() {
		String result = name;
		if (!Strings.isNullOrEmpty(library)) {
			result = library + "/" + result;
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((library == null) ? 0 : library.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass()) {
			return false;
		}
		ResourceId other = (ResourceId) obj;
		if (library == null) {
			if (other.library != null) {
				return false;
			}
		} else if (!library.equals(other.library)) {
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

}
