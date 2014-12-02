/**
 *
 */
package com.sirma.itt.cmf.alfresco4.descriptor;

import com.sirma.itt.emf.remote.RESTClient;

/**
 * Implementation for alfresco file descriptor that additionally holds as id the desired name for
 * the file. Use {@link #getId()} to get the name
 *
 * @author borislav banchev
 */
public class AlfrescoFileWithNameDescriptor extends AlfrescoFileDescriptor {

	/**
	 *
	 */
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
	public AlfrescoFileWithNameDescriptor(String id, String name, String containerId,
			RESTClient restClient) {
		super(id, containerId, restClient);
		this.name = name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getId() {
		return name;
	}

}
