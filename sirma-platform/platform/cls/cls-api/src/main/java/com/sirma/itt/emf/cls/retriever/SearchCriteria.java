package com.sirma.itt.emf.cls.retriever;

import java.util.Date;
import java.util.List;

/**
 * Base class for common search criteria in the code list server.
 *
 * @author Nikolay Velkov
 */
public abstract class SearchCriteria {

	/** Code IDs (values). */
	private List<String> ids;

	/** Code descriptions. */
	private List<String> descriptions;

	/** Code comments **/
	private List<String> comments;

	/** Code master values. */
	private List<String> masterValue;

	/** Code validity start date. */
	private Date fromDate;

	/** Code validity end date. */
	private Date toDate;

	/** Code extra. */
	private List<String> extra1;

	/** Code extra2. */
	private List<String> extra2;

	/** Code extra3. */
	private List<String> extra3;

	/** Code extra4. */
	private List<String> extra4;

	/** Code extra5. */
	private List<String> extra5;

	/** Limit for paginating. */
	private int limit;

	/** Offset for paginating. */
	private int offset;

	/**
	 * Gets the code IDs.
	 *
	 * @return the code IDs
	 */
	public List<String> getIds() {
		return ids;
	}

	/**
	 * Sets the code IDs.
	 *
	 * @param ids
	 *            the new code IDs
	 */
	public void setIds(List<String> ids) {
		this.ids = ids;
	}

	/**
	 * Gets the code descriptions.
	 *
	 * @return the code descriptions
	 */
	public List<String> getDescriptions() {
		return descriptions;
	}

	/**
	 * Sets the code descriptions.
	 *
	 * @param descriptions
	 *            the new code descriptions
	 */
	public void setDescriptions(List<String> descriptions) {
		this.descriptions = descriptions;
	}

	/**
	 * Getter method for comments.
	 *
	 * @return the comments
	 */
	public List<String> getComments() {
		return comments;
	}

	/**
	 * Setter method for comments.
	 *
	 * @param comments
	 *            the comments to set
	 */
	public void setComments(List<String> comments) {
		this.comments = comments;
	}

	/**
	 * Gets the code master values.
	 *
	 * @return the code master values
	 */
	public List<String> getMasterValue() {
		return masterValue;
	}

	/**
	 * Sets the code master values.
	 *
	 * @param masterValue
	 *            the new code master values
	 */
	public void setMasterValue(List<String> masterValue) {
		this.masterValue = masterValue;
	}

	/**
	 * Gets the code validity start date.
	 *
	 * @return the code validity start date
	 */
	public Date getFromDate() {
		return fromDate;
	}

	/**
	 * Sets the code validity start date.
	 *
	 * @param fromDate
	 *            the new code validity start date
	 */
	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	/**
	 * Gets the code validity end date.
	 *
	 * @return the code validity end date
	 */
	public Date getToDate() {
		return toDate;
	}

	/**
	 * Sets the code validity end date.
	 *
	 * @param toDate
	 *            the new code validity end date
	 */
	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}

	/**
	 * Gets the code extra1.
	 *
	 * @return the code extra1
	 */
	public List<String> getExtra1() {
		return extra1;
	}

	/**
	 * Sets the code extra1.
	 *
	 * @param extra1
	 *            the new code extra1
	 */
	public void setExtra1(List<String> extra1) {
		this.extra1 = extra1;
	}

	/**
	 * Gets the code extra2.
	 *
	 * @return the code extra2
	 */
	public List<String> getExtra2() {
		return extra2;
	}

	/**
	 * Sets the code extra2.
	 *
	 * @param extra2
	 *            the new code extra2
	 */
	public void setExtra2(List<String> extra2) {
		this.extra2 = extra2;
	}

	/**
	 * Gets the code extra3.
	 *
	 * @return the code extra3
	 */
	public List<String> getExtra3() {
		return extra3;
	}

	/**
	 * Sets the code extra3.
	 *
	 * @param extra3
	 *            the new code extra3
	 */
	public void setExtra3(List<String> extra3) {
		this.extra3 = extra3;
	}

	/**
	 * Gets the code extra4.
	 *
	 * @return the code extra4
	 */
	public List<String> getExtra4() {
		return extra4;
	}

	/**
	 * Sets the code extra4.
	 *
	 * @param extra4
	 *            the new code extra4
	 */
	public void setExtra4(List<String> extra4) {
		this.extra4 = extra4;
	}

	/**
	 * Gets the code extra5.
	 *
	 * @return the code extra5
	 */
	public List<String> getExtra5() {
		return extra5;
	}

	/**
	 * Sets the code extra5.
	 *
	 * @param extra5
	 *            the new code extra5
	 */
	public void setExtra5(List<String> extra5) {
		this.extra5 = extra5;
	}

	/**
	 * Gets the limit for paginating.
	 *
	 * @return the limit for paginating
	 */
	public int getLimit() {
		return limit;
	}

	/**
	 * Sets the limit for paginating.
	 *
	 * @param limit
	 *            the new limit for paginating
	 */
	public void setLimit(int limit) {
		this.limit = limit;
	}

	/**
	 * Gets the offset for paginating.
	 *
	 * @return the offset for paginating
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * Sets the offset for paginating.
	 *
	 * @param offset
	 *            the new offset for paginating
	 */
	public void setOffset(int offset) {
		this.offset = offset;
	}

}
