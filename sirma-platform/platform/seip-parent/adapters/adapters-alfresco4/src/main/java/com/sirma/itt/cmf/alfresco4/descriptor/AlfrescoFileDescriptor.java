package com.sirma.itt.cmf.alfresco4.descriptor;

import java.io.InputStream;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.multipart.Part;

import com.sirma.itt.cmf.alfresco4.ServiceURIRegistry;
import com.sirma.itt.seip.adapters.remote.DMSClientException;
import com.sirma.itt.seip.adapters.remote.RESTClient;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Implementation for alfresco file descriptor that connects through {@link RESTClient} to download a remote content
 *
 * @author borislav banchev
 */
public class AlfrescoFileDescriptor implements FileDescriptor {

	private static final long serialVersionUID = -2533715395123634141L;
	private static final String DOWNLOAD_URL_SCRIPT = "api/node/content/";

	/** The rest client. */
	private RESTClient restClient;
	/** The node id. */
	private String id;

	private String containerId;

	private String fileName;

	/**
	 * Instantiates a new alfresco file descriptor.
	 *
	 * @param id
	 *            the node id
	 * @param containerId
	 *            is the site id
	 * @param restClient
	 *            the rest client
	 */
	public AlfrescoFileDescriptor(String id, String containerId, String fileName, RESTClient restClient) {
		this.id = id;
		this.containerId = containerId;
		this.restClient = restClient;
		this.fileName = fileName;
	}

	@Override
	public String getId() {
		return id;
	}

	/**
	 * Reads the data from the remote system stream
	 *
	 * @throws EmfRuntimeException
	 *             on download error
	 */
	@Override
	public InputStream getInputStream() {
		HttpMethod createMethod = restClient.createMethod(new GetMethod(), new Part[0], true);
		try {
			String remoteLink = id;
			if (!remoteLink.startsWith(ServiceURIRegistry.SERVICE_ROOT)) {
				remoteLink = DOWNLOAD_URL_SCRIPT + id.replace(":/", "");
			}
			return restClient.request(createMethod, remoteLink);
		} catch (DMSClientException e) {
			throw new EmfRuntimeException("Error during remote download invocation from dms!", e);
		}
	}

	@Override
	public int hashCode() {
		return 31 + (id == null ? 0 : id.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof AlfrescoFileDescriptor)) {
			return false;
		}
		return EqualsHelper.nullSafeEquals(this.id, ((AlfrescoFileDescriptor) obj).id);
	}

	@Override
	public String toString() {
		return "AlfrescoFileDescriptor [id=" + id + "]";
	}

	@Override
	public String getContainerId() {
		return containerId;
	}

	@Override
	public String getFileName() {
		return fileName;
	}

	@Override
	public void close() {
		restClient = null;
	}

}
