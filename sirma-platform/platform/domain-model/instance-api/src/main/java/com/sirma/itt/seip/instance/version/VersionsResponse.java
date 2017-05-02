package com.sirma.itt.seip.instance.version;

import java.util.Collection;
import java.util.Collections;

import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Used for instance version retrieving. It contains result instances(the versions) and the total number of the versions
 * created for specific instance. This is needed, because we have option for pagination, where we return specific number
 * of version instance, but we still need the total number of the versions. There are some additional methods, when
 * there are no versions for the given instance.
 *
 * @author A. Kunchev
 */
public class VersionsResponse {

	/**
	 * Stores versions converted to original instances.
	 */
	private Collection<Instance> results;

	/**
	 * Stores the total count of the stored versions for specific instance.
	 */
	private int totalCount;

	/**
	 * Builds empty response object. Primary used, when there are no version for specific instance.
	 *
	 * @return {@link VersionsResponse} with empty results collection and <code>0</code> for total count
	 */
	public static VersionsResponse emptyResponse() {
		VersionsResponse response = new VersionsResponse();
		response.setResults(Collections.emptyList());
		response.setTotalCount(0);
		return response;
	}

	/**
	 * Checks if the current response object is empty.
	 *
	 * @return <code>true</code> if the total count is <code>0</code>, false otherwise
	 */
	public boolean isEmpty() {
		return totalCount == 0;
	}

	/**
	 * Getter for the result instances.
	 *
	 * @return the results
	 */
	public Collection<Instance> getResults() {
		return results;
	}

	/**
	 * Setter for the result instances.
	 *
	 * @param results
	 *            the results to set
	 */
	public void setResults(Collection<Instance> results) {
		this.results = results;
	}

	/**
	 * Getter for the total count of the versions for the specific instance. Needed for the pagination.
	 *
	 * @return the totalCount of the versions for the instance
	 */
	public int getTotalCount() {
		return totalCount;
	}

	/**
	 * Setter for the total count of the versions for the specific instance. Needed for the pagination.
	 *
	 * @param totalCount
	 *            the totalCount to set
	 */
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

}
