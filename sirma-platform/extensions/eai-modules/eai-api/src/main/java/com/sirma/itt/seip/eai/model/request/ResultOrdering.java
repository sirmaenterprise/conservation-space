package com.sirma.itt.seip.eai.model.request;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The page ordering json<->java binding.
 * 
 * @author bbanchev
 */
public class ResultOrdering implements Serializable {
	private static final long serialVersionUID = 6011144993284020899L;
	@JsonProperty(value = "asc", defaultValue = "false")
	private boolean asc;
	@JsonProperty(value = "by", required = true)
	private String orderBy;

	/**
	 * Default constructor
	 */
	public ResultOrdering() {
		// used by mapper
	}

	/**
	 * @param asc
	 * @param orderBy
	 */
	public ResultOrdering(boolean asc, String orderBy) {
		this.asc = asc;
		this.orderBy = orderBy;
	}

	/**
	 * Getter method for asc.
	 *
	 * @return the asc
	 */
	public boolean isAsc() {
		return asc;
	}

	/**
	 * Setter method for asc.
	 *
	 * @param asc
	 *            the asc to set
	 */
	public void setAsc(boolean asc) {
		this.asc = asc;
	}

	/**
	 * Getter method for orderBy.
	 *
	 * @return the orderBy
	 */
	public String getOrderBy() {
		return orderBy;
	}

	/**
	 * Setter method for orderBy.
	 *
	 * @param orderBy
	 *            the orderBy to set
	 */
	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (asc ? 1231 : 1237);
		result = prime * result + ((orderBy == null) ? 0 : orderBy.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ResultOrdering))
			return false;
		ResultOrdering other = (ResultOrdering) obj;
		if (asc != other.asc)
			return false;
		if (orderBy == null) {
			if (other.orderBy != null)
				return false;
		} else if (!orderBy.equals(other.orderBy))
			return false;
		return true;
	}

}
