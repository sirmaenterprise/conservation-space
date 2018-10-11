package com.sirma.itt.seip.eai.dam.service.communication.response;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.seip.eai.cs.model.internal.CSExternalInstanceId;
import com.sirma.itt.seip.eai.model.ServiceRequest;

/**
 * Request for content download, having single property - the record id to find image for
 * 
 * @author bbanchev
 */
public class ContentDownloadServiceRequest implements ServiceRequest {
	private static final long serialVersionUID = -2789781518526691993L;
	@Tag(1)
	private CSExternalInstanceId instanceId;
	@Tag(2)
	private String classification;

	/**
	 * Default constructor.
	 */
	ContentDownloadServiceRequest() {
		// no arg constructor for kryo
	}

	/**
	 * Instantiates a new content download service request.
	 *
	 * @param instanceId
	 *            the record id to search image for
	 * @param classification
	 */
	ContentDownloadServiceRequest(CSExternalInstanceId instanceId, String classification) {
		this.instanceId = instanceId;
		this.classification = classification;
	}

	/**
	 * Gets the instance id for the request
	 *
	 * @return the instance id
	 */
	public CSExternalInstanceId getInstanceId() {
		return instanceId;
	}

	/**
	 * Getter method for classification.
	 * 
	 * @return the classification
	 */
	public String getClassification() {
		return classification;
	}

	@Override
	public String toString() {
		return "Content download for: " + instanceId;
	}
}