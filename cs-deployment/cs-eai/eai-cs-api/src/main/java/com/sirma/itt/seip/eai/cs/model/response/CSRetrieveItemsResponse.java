package com.sirma.itt.seip.eai.cs.model.response;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sirma.itt.seip.eai.cs.model.CSItemRecord;
import com.sirma.itt.seip.eai.cs.model.internal.CSExternalInstanceId;
import com.sirma.itt.seip.eai.exception.EAIReportableException;
import com.sirma.itt.seip.eai.model.ServiceResponse;

/**
 * The {@link CSRetrieveItemsResponse} json<->java binding of external id to retrieved {@link CSItemRecord}.
 */
public class CSRetrieveItemsResponse implements ServiceResponse {
	@JsonProperty(value = "items")
	private Map<CSExternalInstanceId, CSItemRecord> retrieved = new LinkedHashMap<>();

	private EAIReportableException error;

	/**
	 * Getter method for the retrieve items.
	 *
	 * @return the retrieved
	 */
	public synchronized Map<CSExternalInstanceId, CSItemRecord> getRetrieved() {
		return retrieved;
	}

	/**
	 * Add a retrieved {@link CSItemRecord} keyed with its external id. Method is synchronized to provide parallel
	 * stream support
	 *
	 * @param externalId
	 *            to key to the record
	 * @param record
	 *            is the associated item
	 * @throws EAIReportableException
	 *             on duplicate key provided
	 */
	public synchronized void addRetrieved(CSExternalInstanceId externalId, CSItemRecord record)
			throws EAIReportableException {
		CSItemRecord existing = this.retrieved.put(externalId, record);
		if (existing != null) {
			throw new EAIReportableException(
					"Detected duplicate entry " + externalId + " with already existing instance: " + existing);
		}
	}

	/**
	 * Gets the error that might occur during partial instance retrieval.
	 *
	 * @return the error. Might be null
	 */
	public EAIReportableException getError() {
		return error;
	}

	/**
	 * Sets the error that might occur during partial instance retrieval.
	 *
	 * @param error
	 *            the error to set
	 */
	public void setError(EAIReportableException error) {
		this.error = error;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((retrieved == null) ? 0 : retrieved.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof CSRetrieveItemsResponse)) {
			return false;
		}
		CSRetrieveItemsResponse other = (CSRetrieveItemsResponse) obj;
		if (retrieved == null) {
			if (other.retrieved != null) {
				return false;
			}
		} else if (!retrieved.equals(other.retrieved)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getSimpleName());
		builder.append("[items=");
		builder.append(retrieved);
		builder.append("]");
		return builder.toString();
	}

}
