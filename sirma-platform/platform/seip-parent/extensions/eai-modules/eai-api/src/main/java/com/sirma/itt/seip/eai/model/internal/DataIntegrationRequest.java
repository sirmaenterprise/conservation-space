package com.sirma.itt.seip.eai.model.internal;

import java.util.Collection;
import java.util.Collections;

import com.sirma.itt.seip.instance.actions.ActionRequest;

/**
 * The {@link DataIntegrationRequest} is wrapper for data integration request. Contains a set of request data objects
 * to be imported. Also contains additional parameters to control import behavior.
 *
 * @param <T>
 *            the request data type
 */
public abstract class DataIntegrationRequest<T> extends ActionRequest {
	private static final long serialVersionUID = 744249286294022393L;

	private String systemId;
	private boolean linkInstances;
	private boolean resolveLinks;
	private Collection<T> requestData = Collections.emptyList();

	/**
	 * Whether to create relations between instances if applicable and relation data is available.
	 *
	 * @return true if so
	 */
	public boolean isLinkInstances() {
		return linkInstances;
	}

	/**
	 * Setter method for linkInstances.
	 *
	 * @param linkInstances
	 *            the linkInstances to set
	 */
	public void setLinkInstances(boolean linkInstances) {
		this.linkInstances = linkInstances;
	}

	/**
	 * Whether to resolve missing relation dependencies.
	 *
	 * @return true if so
	 */
	public boolean isResolveLinks() {
		return resolveLinks;
	}

	/**
	 * Setter method for resolveLinks.
	 *
	 * @param resolveLinks
	 *            the resolveLinks to set
	 */
	public void setResolveLinks(boolean resolveLinks) {
		this.resolveLinks = resolveLinks;
	}

	/**
	 * Gets the requestData collection.
	 *
	 * @return the requestData
	 */
	public Collection<T> getRequestData() {
		return requestData;
	}

	/**
	 * Setter method for requestData.
	 *
	 * @param requestData
	 *            the requestData to set
	 */
	public void setRequestData(Collection<T> requestData) {
		this.requestData = requestData;
	}

	/**
	 * Gets the system id.
	 *
	 * @return the system id
	 */
	public String getSystemId() {
		return systemId;
	}

	/**
	 * Sets the system id.
	 *
	 * @param systemId
	 *            the new system id
	 */
	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

}
