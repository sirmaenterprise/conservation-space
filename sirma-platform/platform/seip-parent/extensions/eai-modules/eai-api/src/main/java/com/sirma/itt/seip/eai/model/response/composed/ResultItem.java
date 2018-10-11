package com.sirma.itt.seip.eai.model.response.composed;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sirma.itt.seip.eai.model.request.DynamicProperties;

/**
 * Represent a single item in the response. It has a properties and relations mappings
 */
public class ResultItem {
	@JsonProperty(value = "properties", required = true)
	private DynamicProperties properties;
	@JsonProperty(value = "relations", required = true)
	private List<RelatedItem> relations;

	/**
	 * Getter method for properties.
	 *
	 * @return the properties
	 */
	public DynamicProperties getProperties() {
		return properties;
	}

	/**
	 * Setter method for properties.
	 *
	 * @param properties
	 *            the properties to set
	 */
	public void setProperties(DynamicProperties properties) {
		this.properties = properties;
	}

	/**
	 * Getter method for relations.
	 *
	 * @return the relations
	 */
	public List<RelatedItem> getRelations() {
		return relations;
	}

	/**
	 * Setter method for relations.
	 *
	 * @param relations
	 *            the relations to set
	 */
	public void setRelations(List<RelatedItem> relations) {
		this.relations = relations;
	}

}
