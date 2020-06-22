package com.sirma.itt.seip.eai.service.communication;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.seip.eai.exception.EAIException;

/**
 * The {@link RemoteCommunicationConfiguration} holds system+tenant specific communication configurations. The set of
 * supported services are stored and uri to the desired service could be obtained by
 * {@link #getRequestServiceURI(EAIServiceIdentifier, String)}
 *
 * @author bbanchev
 */
public class RemoteCommunicationConfiguration extends CommunicationConfiguration {
	private URI baseURI;
	private Map<String, String> requestHeaders;
	private Map<EAIServiceIdentifier, ServiceEndpoint> serviceEndpoints;
	private boolean trustAllCertificates = true;

	/**
	 * Instantiates a new communication configuration.
	 *
	 * @param baseURI
	 *            the base uri to use with all service endpoints
	 */
	public RemoteCommunicationConfiguration(URI baseURI) {
		this.baseURI = baseURI;
		this.requestHeaders = new HashMap<>(10);
		this.serviceEndpoints = new HashMap<>(10);
	}

	/**
	 * Gets the base uri used for the services.
	 *
	 * @return the base uri
	 */
	public URI getBaseURI() {
		return baseURI;
	}

	/**
	 * Gets all request headers.
	 *
	 * @return the request headers
	 */
	public Map<String, String> getRequestHeaders() {
		return requestHeaders;
	}

	/**
	 * Adds the http header to be added with each http request.
	 *
	 * @param key
	 *            the key of header
	 * @param value
	 *            the value of header
	 */
	public void addHttpHeader(String key, String value) {
		requestHeaders.put(key, value);
	}

	/**
	 * Adds the service endpoint available at the external system.
	 *
	 * @param serviceEndpoint
	 *            the service endpoint configuration
	 */
	public void addServiceEndpoint(ServiceEndpoint serviceEndpoint) {
		if (isSealed()) {
			return;
		}
		serviceEndpoints.put(serviceEndpoint.getServiceId(), serviceEndpoint);
	}

	/**
	 * Gets the request service uri for the given service for GET method.
	 *
	 * @param serviceId
	 *            the service id to get for
	 * @return the request service uri for GET method
	 * @throws EAIException
	 *             on failure during service endpoint construction or mising configuration
	 */
	public URIBuilder getRequestServiceURI(EAIServiceIdentifier serviceId) throws EAIException {
		return getRequestServiceURI(serviceId, HttpGet.METHOD_NAME);
	}

	/**
	 * Gets the request service uri for the given service and the given http method.
	 *
	 * @param serviceId
	 *            the service id to get for
	 * @param method
	 *            the method to check
	 * @return the request service uri
	 * @throws EAIException
	 *             on failure during service endpoint construction or mising configuration
	 */
	public URIBuilder getRequestServiceURI(EAIServiceIdentifier serviceId, String method) throws EAIException {
		return constructPath(new URIBuilder(baseURI), getServiceEndpoint(serviceId).getMethodEndpoint(method));
	}

	private ServiceEndpoint getServiceEndpoint(EAIServiceIdentifier serviceId) throws EAIException {
		if (serviceEndpoints.get(serviceId) != null) {
			return serviceEndpoints.get(serviceId);
		}
		throw new EAIException("Missing service endpoint configuration for " + serviceId.getServiceId());
	}

	private static URIBuilder constructPath(URIBuilder uriBuilder, String subPath) {// NOSONAR
		if (StringUtils.isBlank(subPath) || "/".equals(subPath)) {
			return uriBuilder;
		}
		String oldPath = uriBuilder.getPath();
		StringBuilder stringBuilder = new StringBuilder(oldPath.length() + subPath.length() + 1);
		stringBuilder.append(oldPath);
		if (oldPath.lastIndexOf('/') == oldPath.length() - 1) {
			if (subPath.indexOf('/') == 0) {
				stringBuilder.append(subPath.substring(1));
			} else {
				stringBuilder.append(subPath);
			}
		} else if (subPath.indexOf('/') == 0) {
			stringBuilder.append(subPath);
		} else {
			stringBuilder.append('/').append(subPath);
		}
		return uriBuilder.setPath(stringBuilder.toString());
	}

	/**
	 * Getter method for trustAllCertificates.
	 *
	 * @return the trustAllCertificates
	 */
	public boolean isTrustAllCertificates() {
		return trustAllCertificates;
	}

	/**
	 * Setter method for trustAllCertificates.
	 *
	 * @param trustAllCertificates
	 *            the trustAllCertificates to set
	 */
	public void setTrustAllCertificates(boolean trustAllCertificates) {
		this.trustAllCertificates = trustAllCertificates;
	}

	/**
	 * Getter method for serviceEndpoints.
	 * 
	 * @return the serviceEndpoints
	 */
	public Map<EAIServiceIdentifier, ServiceEndpoint> getServiceEndpoints() {
		return serviceEndpoints;
	}

	@Override
	public void seal() {
		serviceEndpoints = Collections.unmodifiableMap(serviceEndpoints);
		super.seal();
	}
}
