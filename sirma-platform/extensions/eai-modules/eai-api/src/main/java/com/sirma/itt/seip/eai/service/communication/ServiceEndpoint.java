package com.sirma.itt.seip.eai.service.communication;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import com.sirma.itt.seip.eai.model.SealedModel;

/**
 * Describe a service endpoint with a set of specific methods supported. For each method there is dynamic model for
 * service configurations.
 *
 * @author bbanchev
 */
public class ServiceEndpoint extends SealedModel {

	private EAIServiceIdentifier serviceId;
	private Map<String, Map<String, Serializable>> methods;

	/**
	 * Instantiates a new service endpoint.
	 *
	 * @param serviceId
	 *            the service id to describe
	 * @param methods
	 *            the methods to use. The map model is like
	 *            <code>{"GET":{"uri":" service/art/objects.json","timeout":12000}}</code>. The first key is the method
	 *            type to a complex values for method configuration
	 */
	public ServiceEndpoint(EAIServiceIdentifier serviceId, Map<String, Map<String, Serializable>> methods) {
		this.serviceId = serviceId;
		this.methods = methods
				.entrySet()
					.stream()
					.collect(Collectors.toMap(e -> e.getKey().toUpperCase(), e -> e.getValue()));
	}

	/**
	 * Gets the method service relative uri based on method type. If method is not configured it returns null;
	 *
	 * @param method
	 *            the method type (GET,POST)
	 * @return the method endpoint service relative uri
	 */
	public String getMethodEndpoint(String method) {
		Map<String, Serializable> methodConfiguration = methods.get(method.toUpperCase());
		if (methodConfiguration == null) {
			return null;
		}
		return (String) methodConfiguration.get("uri");
	}

	/**
	 * Gets the service id.
	 *
	 * @return the service id
	 */
	public EAIServiceIdentifier getServiceId() {
		return serviceId;
	}

	/**
	 * Gets the method service timeout. If method is not configured it returns null;
	 * 
	 * @param method
	 *            the method type
	 * @return the method endpoint timeout.
	 */
	public Integer getMethodTimeout(String method) {
		Map<String, Serializable> methodConfiguration = methods.get(method.toUpperCase());
		if (methodConfiguration == null) {
			return null;
		}
		return (Integer) methodConfiguration.get("timeout");
	}

	@Override
	public void seal() {
		methods = Collections.unmodifiableMap(methods);
		super.seal();
	}

}
