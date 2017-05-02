package com.sirma.itt.seip.eai.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The page size and other pagination information json<->java binding.
 * 
 * @author bbanchev
 */
public class ResultPaging implements Serializable {
	private static final long serialVersionUID = -3546244053449447709L;
	@JsonProperty(value = "limit")
	private int limit;
	@JsonProperty(value = "total")
	private int total;
	@JsonProperty(value = "skip")
	private int skip;

	/**
	 * Getter method for limit.
	 *
	 * @return the limit
	 */
	public int getLimit() {
		return limit;
	}

	/**
	 * Setter method for limit.
	 *
	 * @param limit
	 *            the limit to set
	 */
	public void setLimit(int limit) {
		this.limit = limit;
	}

	/**
	 * Getter method for total.
	 *
	 * @return the total
	 */
	public int getTotal() {
		return total;
	}

	/**
	 * Setter method for total.
	 *
	 * @param total
	 *            the total to set
	 */
	public void setTotal(int total) {
		this.total = total;
	}

	/**
	 * Getter method for skip.
	 *
	 * @return the skip
	 */
	public int getSkip() {
		return skip;
	}

	/**
	 * Setter method for skip.
	 *
	 * @param skip
	 *            the skip to set
	 */
	public void setSkip(int skip) {
		this.skip = skip;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + limit;
		result = prime * result + skip;
		result = prime * result + total;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ResultPaging))
			return false;
		ResultPaging other = (ResultPaging) obj;
		if (limit != other.limit)
			return false;
		if (skip != other.skip)
			return false;
		if (total != other.total)
			return false;
		return true;
	}

}
