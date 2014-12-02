/**
 *
 */
package com.sirma.itt.cmf.alfresco4.descriptor;

import java.io.InputStream;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.multipart.Part;

import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.remote.DMSClientException;
import com.sirma.itt.emf.remote.RESTClient;

/**
 * Implementation for alfresco file descriptor.
 *
 * @author borislav banchev
 */
public class AlfrescoFileDescriptor implements FileDescriptor {

	/**
	 *
	 */
	private static final long serialVersionUID = -2533715395123634141L;

	/** The rest client. */
	private RESTClient restClient;

	/** The id. */
	private String id;

	private String containerId;

	/** The Constant DOWNLOAD_URL_SCRIPT. */
	private static final String DOWNLOAD_URL_SCRIPT = "api/node/content/";

	/**
	 * Instantiates a new alfresco file descriptor.
	 *
	 * @param id
	 *            the id
	 * @param containerId
	 *            is the site id
	 * @param restClient
	 *            the rest client
	 */
	public AlfrescoFileDescriptor(String id, String containerId, RESTClient restClient) {
		this.id = id;
		this.containerId = containerId;
		this.restClient = restClient;
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
	 *
	 * @throws DMSClientException
	 */
	@Override
	public InputStream getInputStream() {
		HttpMethod createMethod = restClient.createMethod(new GetMethod(), new Part[0], true);
		try {
			return restClient.request(createMethod, DOWNLOAD_URL_SCRIPT + id.replace(":/", ""));
		} catch (DMSClientException e) {
			throw new EmfRuntimeException("Error during remote download invocation from dms!", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((id == null) ? 0 : id.hashCode());
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
		AlfrescoFileDescriptor other = (AlfrescoFileDescriptor) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "AlfrescoFileDescriptor [id=" + id + "]";
	}

	@Override
	public String getContainerId() {
		return containerId;
	}

}
