package com.sirma.itt.seip.adapters.iiif;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.multipart.Part;

import com.sirma.itt.seip.adapters.remote.DMSClientException;
import com.sirma.itt.seip.adapters.remote.RESTClient;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.io.FileDescriptor;

/**
 * Implementation for image file descriptor.
 *
 * @author Nikolay Ch
 */
public class ImageFileDescriptor implements FileDescriptor {

	private static final long serialVersionUID = 1L;

	private RESTClient restClient;
	/** The id. */
	private URI id;

	private String containerId;

	/**
	 * Constructor for the descriptor.
	 *
	 * @param id
	 *            the uri of the uploaded image
	 * @param containerId
	 *            the container of the image if present
	 * @param restClient
	 *            the rest client with which the stream of the image can be
	 *            retrieved
	 */
	public ImageFileDescriptor(URI id, String containerId, RESTClient restClient) {
		this.id = id;
		this.containerId = containerId;
		this.restClient = restClient;
	}

	@Override
	public String getId() {
		return id.toString();
	}

	@Override
	public String getContainerId() {
		return containerId;
	}

	@Override
	public InputStream getInputStream() {
		HttpMethod createMethod = restClient.createMethod(new GetMethod(), new Part[0], true);
		try {
			 return restClient.rawRequest(createMethod, id).getResponseBodyAsStream();
		} catch (DMSClientException | IOException e) {
			throw new EmfRuntimeException("Error during remote download invocation from dms!", e);
		}
	}

	@Override
	public void close() {
		// nothing to do
	}

}
