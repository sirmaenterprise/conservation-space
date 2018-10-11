package com.sirma.itt.seip.eai.model.request.composed;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sirma.itt.seip.eai.model.ServiceRequest;
import com.sirma.itt.seip.eai.model.request.query.RawQuery;

/**
 * The ExternalSearchRequest json<->java binding.
 * 
 * @author bbanchev
 */
public class ExternalSearchRequest implements ServiceRequest {
	private static final long serialVersionUID = -596701852086325857L;
	@JsonProperty(value = "query", required = true)
	private RawQuery query;
	@JsonProperty(value = "configuration", required = true)
	private Configuration configuration;

	/**
	 * Getter method for query.
	 *
	 * @return the query
	 */
	public RawQuery getQuery() {
		return query;
	}

	/**
	 * Setter method for query.
	 *
	 * @param query
	 *            the query to set
	 */
	public void setQuery(RawQuery query) {
		this.query = query;
	}

	/**
	 * Getter method for configuration.
	 *
	 * @return the configuration
	 */
	public Configuration getConfiguration() {
		return configuration;
	}

	/**
	 * Setter method for configuration.
	 *
	 * @param configuration
	 *            the configuration to set
	 */
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((configuration == null) ? 0 : configuration.hashCode());
		result = prime * result + ((query == null) ? 0 : query.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ExternalSearchRequest))
			return false;
		ExternalSearchRequest other = (ExternalSearchRequest) obj;
		if (configuration == null) {
			if (other.configuration != null)
				return false;
		} else if (!configuration.equals(other.configuration))
			return false;
		if (query == null) {
			if (other.query != null)
				return false;
		} else if (!query.equals(other.query))
			return false;
		return true;
	}

}
