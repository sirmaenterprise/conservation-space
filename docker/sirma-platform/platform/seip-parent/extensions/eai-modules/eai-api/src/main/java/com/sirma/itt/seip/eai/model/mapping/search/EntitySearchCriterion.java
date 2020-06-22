package com.sirma.itt.seip.eai.model.mapping.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sirma.itt.seip.eai.model.SealedModel;
import com.sirma.itt.seip.eai.model.mapping.EntityProperty;

/**
 * The {@link EntitySearchCriterion} is representation of search criteria(either form or order by) that is available
 * during communication process with an external system and in user search. It is specific for a particular system and tenant.
 * 
 * @author bbanchev
 */
public abstract class EntitySearchCriterion extends SealedModel {
	private String propertyId;

	/**
	 * Gets the property id.
	 *
	 * @return the property id
	 */
	@JsonProperty
	public String getPropertyId() {
		return propertyId;
	}

	/**
	 * Sets the property id for given criteria. The property id should match any of the
	 * {@link EntityProperty#getPropertyId()} value
	 *
	 * @param propertyId
	 *            the new property id
	 */
	public void setPropertyId(String propertyId) {
		if (isSealed()) {
			return;
		}
		this.propertyId = propertyId;
	}

}