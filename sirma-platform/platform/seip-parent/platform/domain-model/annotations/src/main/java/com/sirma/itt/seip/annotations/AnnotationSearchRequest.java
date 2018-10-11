package com.sirma.itt.seip.annotations;

import java.util.List;

import com.sirma.itt.seip.time.DateRange;

/**
 * Request object for annotations search. Wraps all used parameters supported for the search.
 *
 * @author BBonev
 */
public class AnnotationSearchRequest {
	private DateRange dateRange;
	private List<String> instanceIds;
	private List<String> userIds;
	private Integer limit;
	private Integer offset;
	private String status;
	private String text;

	/**
	 * Instantiates a new annotation search request.
	 */
	public AnnotationSearchRequest() {
		// nothing to do
	}

	/**
	 * Instantiates a new annotation search request.
	 *
	 * @param dateRange
	 *            the time boundaries for the modifiedOn of the annotation
	 * @param instanceIds
	 *            the IDs of the target instances for which annotations have to be loaded
	 * @param userIds
	 *            the IDs of the users for which createdBy filter will be applied
	 * @param status
	 *            the status
	 * @param text
	 *            the text
	 */
	public AnnotationSearchRequest(DateRange dateRange, List<String> instanceIds, List<String> userIds, String status,
			String text) {
		this.dateRange = dateRange;
		this.instanceIds = instanceIds;
		this.userIds = userIds;
		this.status = status;
		this.text = text;
	}

	/**
	 * @return the dateRange
	 */
	public DateRange getDateRange() {
		return dateRange;
	}

	/**
	 * @param dateRange
	 *            the dateRange to set
	 * @return the current instance to allow method chaining
	 */
	public AnnotationSearchRequest setDateRange(DateRange dateRange) {
		this.dateRange = dateRange;
		return this;
	}

	/**
	 * @return the instanceIds
	 */
	public List<String> getInstanceIds() {
		return instanceIds;
	}

	/**
	 * @param instanceIds
	 *            the instanceIds to set
	 * @return the current instance to allow method chaining
	 */
	public AnnotationSearchRequest setInstanceIds(List<String> instanceIds) {
		this.instanceIds = instanceIds;
		return this;
	}

	/**
	 * @return the userIds
	 */
	public List<String> getUserIds() {
		return userIds;
	}

	/**
	 * @param userIds
	 *            the userIds to set
	 * @return the current instance to allow method chaining
	 */
	public AnnotationSearchRequest setUserIds(List<String> userIds) {
		this.userIds = userIds;
		return this;
	}

	/**
	 * @return the limit
	 */
	public Integer getLimit() {
		return limit;
	}

	/**
	 * @param limit
	 *            the limit to set
	 * @return the current instance to allow method chaining
	 */
	public AnnotationSearchRequest setLimit(Integer limit) {
		this.limit = limit;
		return this;
	}

	/**
	 * @return the offset
	 */
	public Integer getOffset() {
		return offset;
	}

	/**
	 * @param offset
	 *            the offset to set
	 * @return the current instance to allow method chaining
	 */
	public AnnotationSearchRequest setOffset(Integer offset) {
		this.offset = offset;
		return this;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 * @return the current instance to allow method chaining
	 */
	public AnnotationSearchRequest setStatus(String status) {
		this.status = status;
		return this;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text
	 *            the text to set
	 * @return the current instance to allow method chaining
	 */
	public AnnotationSearchRequest setText(String text) {
		this.text = text;
		return this;
	}

}
