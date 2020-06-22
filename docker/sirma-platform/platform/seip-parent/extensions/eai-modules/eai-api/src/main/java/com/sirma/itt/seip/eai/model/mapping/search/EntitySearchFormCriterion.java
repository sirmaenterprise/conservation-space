package com.sirma.itt.seip.eai.model.mapping.search;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link EntitySearchFormCriterion} is representation of search form criterion. Contains the value mapping for
 * search criteria in an external system and the supported operator for that criterion.
 *
 * @author bbanchev
 */
public class EntitySearchFormCriterion extends EntitySearchCriterion {

	private String mapping;
	private String operator;
	private boolean visible;

	/**
	 * Sets the mapping.
	 *
	 * @param mapping
	 *            the new mapping
	 */
	public void setMapping(String mapping) {
		if (isSealed()) {
			return;
		}
		this.mapping = mapping;
	}

	/**
	 * Gets the mapping.
	 *
	 * @return the mapping
	 */
	@JsonProperty
	public String getMapping() {
		return mapping;
	}

	/**
	 * Gets the operator.
	 *
	 * @return the operator
	 */
	@JsonProperty
	public String getOperator() {
		return operator;
	}

	/**
	 * Sets the operator.
	 *
	 * @param operator
	 *            the new operator
	 */
	public void setOperator(String operator) {
		if (isSealed()) {
			return;
		}
		this.operator = operator;
	}

	/**
	 * Sets the visibility.
	 *
	 * @param visible
	 *            the visibility
	 */
	public void setVisible(boolean visible) {
		if (isSealed()) {
			return;
		}
		this.visible = visible;
	}

	/**
	 * Gets the visibility.
	 *
	 * @return the visibility
	 */
	public boolean isVisible() {
		return visible;
	}
}