package com.sirma.itt.seip.eai.model.mapping.search;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link EntitySearchOrderCriterion} is representation of search order by criterion. Contains the position in the
 * order by list of criteria.
 * 
 * @author bbanchev
 */
public class EntitySearchOrderCriterion extends EntitySearchCriterion {
	private Integer orderPosition;

	/**
	 * Gets the order position.
	 *
	 * @return the order position
	 */
	@JsonProperty
	public Integer getOrderPosition() {
		return orderPosition;
	}

	/**
	 * Sets the order position.
	 *
	 * @param orderPosition
	 *            the new order position
	 */
	public void setOrderPosition(Integer orderPosition) {
		if (isSealed()) {
			return;
		}
		this.orderPosition = orderPosition;
	}

}