package com.sirma.itt.seip.eai.cs.model.request;

import java.util.List;

import com.sirma.itt.seip.eai.cs.EAIServicesConstants;
import com.sirma.itt.seip.eai.model.ResultPaging;
import com.sirma.itt.seip.eai.model.ServiceRequest;
import com.sirma.itt.seip.eai.model.request.ResultOrdering;
import com.sirma.itt.seip.eai.model.request.query.RawQuery;

/**
 * Light request model for CS query services. Contains the query, paging, ordering and cs specific properties
 * 
 * @author bbanchev
 */
public class CSSearchRequest implements ServiceRequest {
	private static final long serialVersionUID = -7715989848626944665L;

	private RawQuery query;
	private ResultPaging paging;
	private List<ResultOrdering> ordering;
	private boolean includeReferences = true;
	private boolean instantiateMissing = true;
	private boolean includeThumbnails = true;

	/**
	 * Gets the ordering information.
	 *
	 * @return the ordering
	 */
	public List<ResultOrdering> getOrdering() {
		return ordering;
	}

	/**
	 * Gets the paging information to be requested.
	 *
	 * @return the paging
	 */
	public ResultPaging getPaging() {
		return paging;
	}

	/**
	 * Gets the query assigned to the request.
	 *
	 * @return the query
	 */
	public RawQuery getQuery() {
		return query;
	}

	/**
	 * Sets the paging request.
	 *
	 * @param paging
	 *            the new paging
	 */
	public void setPaging(ResultPaging paging) {
		this.paging = paging;

	}

	/**
	 * Sets the ordering.
	 *
	 * @param ordering
	 *            the new ordering
	 */
	public void setOrdering(List<ResultOrdering> ordering) {
		this.ordering = ordering;

	}

	/**
	 * Sets the {@link RawQuery} to be executed as search.
	 *
	 * @param query
	 *            the query build - might be empty
	 */
	public void setQuery(RawQuery query) {
		this.query = query;

	}

	/**
	 * Checks if include references is activated.
	 *
	 * @return true if to include references
	 */
	public boolean isIncludeReferences() {
		return includeReferences;
	}

	/**
	 * Sets the flag to include references.
	 *
	 * @param includeReferences
	 *            if true the request would include for references resolving
	 */
	public void setIncludeReferences(boolean includeReferences) {
		this.includeReferences = includeReferences;
	}

	/**
	 * Checks if include thumbnails is activated.
	 * 
	 * @return true if to include thumbnails
	 */
	public boolean isIncludeThumbnails() {
		return includeThumbnails;
	}

	/**
	 * Sets the flag to include thumbnails
	 * 
	 * @param includeThumbnails
	 *            if true the request would include thumbnails
	 */
	public void setIncludeThumbnails(boolean includeThumbnails) {
		this.includeThumbnails = includeThumbnails;
	}

	/**
	 * Check whether to instantiate missing objects
	 * 
	 * @return the true if yes, false to skip those instances
	 */
	public boolean isInstantiateMissing() {
		return instantiateMissing;
	}

	/**
	 * Sets the flag to instantiate missing instances -
	 * {@link EAIServicesConstants#SEARCH_INSTANTIATE_MISSING_INSTANCES}
	 * 
	 * @param instantiateMissing
	 *            the value to set - default is true
	 */
	public void setInstantiateMissing(boolean instantiateMissing) {
		this.instantiateMissing = instantiateMissing;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder
				.append("CSSearchRequest [query=")
					.append(query)
					.append(", paging=")
					.append(paging)
					.append(", ordering=")
					.append(ordering)
					.append(", includeReferences=")
					.append(includeReferences)
					.append(", includeThumbnails=")
					.append(includeThumbnails)
					.append(", instantiateMissing=")
					.append(instantiateMissing)
					.append("]");
		return builder.toString();
	}

}
