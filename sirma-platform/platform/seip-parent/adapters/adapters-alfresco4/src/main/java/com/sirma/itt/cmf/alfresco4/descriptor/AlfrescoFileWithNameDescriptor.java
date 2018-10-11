package com.sirma.itt.cmf.alfresco4.descriptor;

import java.util.Objects;

import com.sirma.itt.seip.adapters.remote.RESTClient;

/**
 * Implementation for alfresco file descriptor that additionally holds as id the desired name for the file. Use
 * {@link #getId()} to get the name
 *
 * @author borislav banchev
 */
public class AlfrescoFileWithNameDescriptor extends AlfrescoFileDescriptor {

	private static final long serialVersionUID = -2533715395123634141L;

	private String name;

	/**
	 * Instantiates a new alfresco file descriptor.
	 *
	 * @param id
	 *            the id
	 * @param name
	 *            is the name to represent file with
	 * @param containerId
	 *            is the site id
	 * @param restClient
	 *            the rest client
	 */
	public AlfrescoFileWithNameDescriptor(String id, String name, String containerId, String fileName, RESTClient restClient) {
		super(id, containerId, fileName, restClient);
		this.name = name;
	}

	@Override
	public String getId() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!super.equals(o)) {
			return false;
		}
		if (!(o instanceof AlfrescoFileWithNameDescriptor)) {
			return false;
		}
		AlfrescoFileWithNameDescriptor that = (AlfrescoFileWithNameDescriptor) o;
		return Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), name);
	}
}
