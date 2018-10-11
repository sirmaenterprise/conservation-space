package com.sirma.itt.seip.eai.model.communication;

import com.sirma.itt.seip.eai.model.ServiceRequest;
import com.sirma.itt.seip.eai.model.ServiceResponse;
import com.sirma.itt.seip.eai.service.communication.EAIServiceIdentifier;

/**
 * The Response wrapper holding the service id - {@link EAIServiceIdentifier}, the request entity -
 * {@link ServiceRequest} initiated the response and the system id for the request/response, and the response -
 * {@link ServiceResponse} itself.
 * 
 * @author bbanchev
 */
public class ResponseInfo {
	protected final ServiceRequest request;
	protected final ServiceResponse response;
	protected final EAIServiceIdentifier serviceId;
	protected final String systemId;

	/**
	 * Instantiates a new response info.
	 *
	 * @param request
	 *            the request wrapper - required
	 * @param response
	 *            the service response
	 */
	public ResponseInfo(RequestInfo request, ServiceResponse response) {
		this.systemId = request.getSystemId();
		this.serviceId = request.getServiceId();
		this.request = request.getRequest();
		this.response = response;
	}

	/**
	 * Getter method for the service request.
	 *
	 * @return the request
	 */
	public ServiceRequest getRequest() {
		return request;
	}

	/**
	 * Get the serviceId of the service request/response.
	 *
	 * @return the serviceId
	 */
	public EAIServiceIdentifier getServiceId() {
		return serviceId;
	}

	/**
	 * Getter method for associated external system.
	 *
	 * @return the systemId
	 */
	public String getSystemId() {
		return systemId;
	}

	/**
	 * Gets the service response received and parsed.
	 *
	 * @return the response
	 */
	public ServiceResponse getResponse() {
		return response;
	}

}
