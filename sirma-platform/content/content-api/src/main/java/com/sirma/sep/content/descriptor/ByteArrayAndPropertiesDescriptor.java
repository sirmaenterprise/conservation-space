package com.sirma.sep.content.descriptor;

import java.io.Serializable;
import java.util.Map;

import com.sirma.itt.seip.io.FileAndPropertiesDescriptor;

/**
 * File descriptor initialized by byte array and possible additional properties.
 *
 * @author BBonev
 */
public class ByteArrayAndPropertiesDescriptor extends ByteArrayFileDescriptor implements FileAndPropertiesDescriptor {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 4247430374588744920L;

	/** The properties. */
	private Map<String, Serializable> properties;

	/**
	 * Instantiates a new byte array and properties descriptor.
	 *
	 * @param id
	 *            the id
	 * @param data
	 *            the data
	 * @param containerId
	 *            the container id
	 * @param properties
	 *            the properties
	 */
	public ByteArrayAndPropertiesDescriptor(String id, byte[] data, String containerId,
			Map<String, Serializable> properties) {
		super(id, containerId, data);
		this.properties = properties;
	}

	@Override
	public Map<String, Serializable> getProperties() {
		return properties;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		return obj instanceof ByteArrayAndPropertiesDescriptor;
	}

}
