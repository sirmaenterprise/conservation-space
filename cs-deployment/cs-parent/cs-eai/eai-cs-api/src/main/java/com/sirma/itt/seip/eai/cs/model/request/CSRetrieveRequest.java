package com.sirma.itt.seip.eai.cs.model.request;

import java.util.Collection;

import com.sirma.itt.seip.eai.cs.model.internal.CSExternalInstanceId;
import com.sirma.itt.seip.eai.cs.model.response.CSRetrieveItemsResponse;
import com.sirma.itt.seip.eai.model.ServiceRequest;

/**
 * Request model for CS import services that support multiple id to be retrieved with a single request. The response
 * would be {@link CSRetrieveItemsResponse}
 * 
 * @author bbanchev
 */
public class CSRetrieveRequest implements ServiceRequest {

	/** serial version uid. */
	private static final long serialVersionUID = 1351398315380844796L;
	private Collection<CSExternalInstanceId> externalIds;

	/**
	 * Gets the external ids to import.
	 *
	 * @return the external ids
	 */
	public Collection<CSExternalInstanceId> getExternalIds() {
		return externalIds;
	}

	/**
	 * Sets the external ids.
	 *
	 * @param externalIds
	 *            the new external ids
	 */
	public void setExternalIds(Collection<CSExternalInstanceId> externalIds) {
		this.externalIds = externalIds;
	}

	@Override
	public String toString() {
		return "Retrieve request for: " + externalIds;
	}

}
