package com.sirma.itt.seip.rest;

import java.util.Map;

/**
 * The ResourceInstance class is wrapper for seip user/group
 */
public class ResourceInstance {

	/** The resource id. */
	private String resourceId;

	/** The properties. */
	private Map<String, Object> properties;

	/**
	 * Instantiates a new resource instance.
	 * 
	 * @param resourceId
	 *            the resource id
	 * @param properties
	 *            the properties
	 */
	public ResourceInstance(String resourceId, Map<String, Object> properties) {
		super();
		this.resourceId = resourceId;
		this.properties = properties;
	}

	/**
	 * Getter method for resourceId.
	 * 
	 * @return the resourceId
	 */
	public String getResourceId() {
		return resourceId;
	}

	/**
	 * Setter method for resourceId.
	 * 
	 * @param resourceId
	 *            the resourceId to set
	 */
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	/**
	 * Getter method for properties.
	 * 
	 * @return the properties
	 */
	public Map<String, Object> getProperties() {
		return properties;
	}

	/**
	 * Setter method for properties.
	 * 
	 * @param properties
	 *            the properties to set
	 */
	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	@Override
	public String toString() {
		return "ResourceInstance [resourceId=" + resourceId + ", properties=" + properties + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((resourceId == null) ? 0 : resourceId.hashCode());
		return result;
	}

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
		ResourceInstance other = (ResourceInstance) obj;
		if (resourceId == null) {
			if (other.resourceId != null) {
				return false;
			}
		} else if (!resourceId.equals(other.resourceId)) {
			return false;
		}
		return true;
	}

}
