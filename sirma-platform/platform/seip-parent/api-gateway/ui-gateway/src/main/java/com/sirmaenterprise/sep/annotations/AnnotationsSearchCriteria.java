package com.sirmaenterprise.sep.annotations;

import java.util.Date;
import java.util.List;

import com.sirma.itt.seip.domain.search.tree.Condition;

/**
 * Contains the filter criteria for retrieving {@link Annotation} data via the API gateway.
 *
 * @author Vilizar Tsonev
 */
public class AnnotationsSearchCriteria {

	private List<String> manuallySelectedObjects;

	private Condition searchTree;

	private List<String> userIds;

	private Date createdFrom;

	private Date createdTo;

	private Integer offset;

	private Integer limit;

	private String status;

	private String text;

	private boolean isHistoricalVersion;

	/**
	 * @return the manuallySelectedObjects
	 */
	public List<String> getManuallySelectedObjects() {
		return manuallySelectedObjects;
	}

	/**
	 * @param manuallySelectedObjects
	 *            the manuallySelectedObjects to set
	 * @return the current instance to allow method chaining
	 */
	public AnnotationsSearchCriteria setManuallySelectedObjects(List<String> manuallySelectedObjects) {
		this.manuallySelectedObjects = manuallySelectedObjects;
		return this;
	}

	/**
	 * @return the searchTree
	 */
	public Condition getSearchTree() {
		return searchTree;
	}

	/**
	 * @param searchTree
	 *            the searchTree to set
	 * @return the current instance to allow method chaining
	 */
	public AnnotationsSearchCriteria setSearchTree(Condition searchTree) {
		this.searchTree = searchTree;
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
	public AnnotationsSearchCriteria setUserIds(List<String> userIds) {
		this.userIds = userIds;
		return this;
	}

	/**
	 * @return the createdFrom
	 */
	public Date getCreatedFrom() {
		return createdFrom;
	}

	/**
	 * @param createdFrom
	 *            the createdFrom to set
	 * @return the current instance to allow method chaining
	 */
	public AnnotationsSearchCriteria setCreatedFrom(Date createdFrom) {
		this.createdFrom = createdFrom;
		return this;
	}

	/**
	 * @return the createdTo
	 */
	public Date getCreatedTo() {
		return createdTo;
	}

	/**
	 * @param createdTo
	 *            the createdTo to set
	 * @return the current instance to allow method chaining
	 */
	public AnnotationsSearchCriteria setCreatedTo(Date createdTo) {
		this.createdTo = createdTo;
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
	public AnnotationsSearchCriteria setOffset(Integer offset) {
		this.offset = offset;
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
	public AnnotationsSearchCriteria setLimit(Integer limit) {
		this.limit = limit;
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
	public AnnotationsSearchCriteria setStatus(String status) {
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
	public AnnotationsSearchCriteria setText(String text) {
		this.text = text;
		return this;
	}

	public boolean isHistoricalVersion() {
		return isHistoricalVersion;
	}

	public void setIsHistoricalVersion(boolean isHistoricalVersion) {
		this.isHistoricalVersion = isHistoricalVersion;
	}

}