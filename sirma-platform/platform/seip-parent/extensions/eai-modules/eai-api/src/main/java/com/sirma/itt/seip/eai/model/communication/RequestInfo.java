package com.sirma.itt.seip.eai.model.communication;

import java.io.Serializable;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.seip.eai.model.ServiceRequest;
import com.sirma.itt.seip.eai.service.communication.EAIServiceIdentifier;

/**
 * The Request wrapper holding the service id - {@link EAIServiceIdentifier}, the request entity -
 * {@link ServiceRequest} and the system id for the request. Request could be serialized by kryo.
 * 
 * @author bbanchev
 */
public class RequestInfo implements Serializable {
	private static final long serialVersionUID = -7871229464408572261L;
	@Tag(1)
	private String systemId;
	@Tag(2)
	private EAIServiceIdentifier serviceId;
	@Tag(3)
	private ServiceRequest request;

	/**
	 * Default empty constructor
	 */
	public RequestInfo() {
		// need for serialization
	}

	/**
	 * Instantiates a new request info.
	 *
	 * @param systemId
	 *            the system to be invoked
	 * @param serviceId
	 *            the service to be invoked
	 * @param request
	 *            the request to be sent
	 */
	public RequestInfo(String systemId, EAIServiceIdentifier serviceId, ServiceRequest request) {
		this.systemId = systemId.toUpperCase();
		this.serviceId = serviceId;
		this.request = request;
	}

	/**
	 * Getter method for request.
	 *
	 * @return the request
	 */
	public ServiceRequest getRequest() {
		return request;
	}

	/**
	 * Getter method for serviceId.
	 *
	 * @return the serviceId
	 */
	public EAIServiceIdentifier getServiceId() {
		return serviceId;
	}

	/**
	 * Getter method for systemId.
	 *
	 * @return the systemId
	 */
	public String getSystemId() {
		return systemId;
	}

	/**
	 * @param request
	 *            the request to set
	 */
	public void setRequest(ServiceRequest request) {
		this.request = request;
	}

	/**
	 * @param serviceId
	 *            the serviceId to set
	 */
	public void setServiceId(EAIServiceIdentifier serviceId) {
		this.serviceId = serviceId;
	}

	/**
	 * @param systemId
	 *            the systemId to set
	 */
	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RequestInfo [");
		if (systemId != null) {
			builder.append("systemId=");
			builder.append(systemId);
			builder.append(", ");
		}
		if (serviceId != null) {
			builder.append("serviceId=");
			builder.append(serviceId);
			builder.append(", ");
		}
		if (request != null) {
			builder.append("request=");
			builder.append(request);
		}
		builder.append("]");
		return builder.toString();
	}

}
